package com.advice.array.api

import com.advice.array.api.config.ConfigManager
import com.advice.array.api.converters.DeviceConverter
import com.advice.array.api.converters.MemoryUsageConverter
import com.advice.array.api.converters.NetworkInterfaceConverter
import com.advice.array.api.converters.ParityConverter
import com.advice.array.api.converters.ParityStatusConverter
import com.advice.array.api.response.ActionResponse
import com.advice.array.api.response.LoginResponse
import com.advice.array.api.response.Response
import com.advice.array.api.sockets.SocketFactory
import com.advice.array.models.DashboardResponse
import com.advice.array.models.Device
import com.advice.array.models.Directory
import com.advice.array.models.DockerContainer
import com.advice.array.models.Interface
import com.advice.array.models.Notification
import com.advice.array.models.Parity
import com.advice.array.models.ParityData
import com.advice.array.models.ParityStatus
import com.advice.array.models.Share
import com.advice.array.models.UPSStatus
import com.advice.array.models.Usage
import com.advice.array.models.VirtualMachine
import com.advice.array.models.createMockDashboardResponse
import com.advice.array.models.createMockDockerContainer
import com.advice.array.models.createMockShare
import com.advice.array.models.createMockUPSStatus
import com.advice.array.models.createMockVirtualMachine
import com.advice.array.models.getTestDevice
import com.advice.array.models.setAddress
import com.advice.array.utils.DateComparator
import com.advice.array.utils.Storage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.neovisionaries.ws.client.WebSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.Date
import java.util.Random
import kotlin.math.max
import kotlin.math.min


class UnraidRepository(
    private val service: UnraidService,
    private val socketFactory: SocketFactory,
    private val config: ConfigManager,
    private val storage: Storage,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val firebaseAnalytics: FirebaseAnalytics
) : KoinComponent {

    init {
        setAnalyticsUUID()
    }

    suspend fun login(
        address: String,
        username: String,
        password: String?
    ): Response<LoginResponse> =
        withContext(Dispatchers.IO) {

            // to allow testing the app without Unraid instance
            if (username == "test" && password == "test") {
                val map = hashMapOf(
                    "version" to "7.0.0-debug",
                    "csrf_token" to "debug_token",
                    "developer" to "true"
                )
                val data = LoginResponse(
                    map, "test", "0.0.0.0", "test server", "PRO", Date()
                )
                config.setAddress("https://0.0.0.0")
                config.updateConfig(map)
                return@withContext Response.Success(data)
            }

            try {
                firebaseCrashlytics.log("Logging in...")
                config.setAddress(address)

                val password = if (password?.isBlank() == true) null else password
                val data = service.login(username, password)
                config.updateConfig(data.config)
                updateAnalytics(data.config)
                Response.Success(data)
            } catch (ex: Exception) {
                onError(ex, "Could not login")
                Response.Error(ex)
            }
        }

    suspend fun relog(): Response<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            firebaseCrashlytics.log("relog")
            val username = storage.username ?: error("cannot login with null username")
            val password = storage.password
            val data = service.login(username, password)
            config.updateConfig(data.config)
            updateAnalytics(data.config)
            Response.Success(data)
        } catch (ex: Exception) {
            onError(ex, "Could not login")
            Response.Error(ex)
        }
    }

    suspend fun getDashboard(): Response<DashboardResponse> =
        withContext(Dispatchers.IO) {
            try {
                if (isTestServer) {
                    return@withContext Response.Success(createMockDashboardResponse())
                }

                Response.Success(service.getDashboard())
            } catch (ex: Exception) {
                onError(ex, "Could not getDashboard")
                Response.Error(ex)
            }
        }

    suspend fun getCpuUsage(onMessageListener: (List<Usage>) -> Unit): WebSocket =
        withContext(Dispatchers.IO) {
            // randomly generated cpu usage on test server
            if (isTestServer) {
                coroutineScope {
                    var previous = 1
                    while (isActive) {
                        val change: Int =
                            Random().nextInt(10) * (if (Random().nextBoolean()) -1 else 1)
                        previous += change
                        previous = min(100, max(1, previous))
                        val usage = listOf(Usage(-1, previous))
                        onMessageListener.invoke(usage)
                        delay(1_000)
                    }
                }
            }

            socketFactory.create("/cpuload") { text ->
                val usage = text
                    ?.split("\n")
                    ?.windowed(3, 3)
                    ?.mapIndexed { index, list ->
                        Usage(
                            index - 1,
                            list[1].split("=")[1].toInt()
                        )
                    } ?: listOf(Usage(-1, 0))

                onMessageListener.invoke(usage)
            }
        }

    suspend fun getVariableSocket(): WebSocket =
        withContext(Dispatchers.IO) {
            socketFactory.create("/var") { text ->
                config.updateConfig(text)
            }
        }

    suspend fun getMemorySocket(onMessageListener: (List<Usage>) -> Unit): WebSocket =
        withContext(Dispatchers.IO) {
            // showing memory for the test server
            if (isTestServer) {
                onMessageListener.invoke(
                    listOf(
                        Usage(-1, 15),
                        Usage(-1, 8),
                        Usage(-1, 12),
                        Usage(-1, 70)
                    )
                )
            }

            val converter = MemoryUsageConverter()
            socketFactory.create("/update1") { text ->
                val values = converter.convert("", text ?: "") ?: emptyList()
                onMessageListener.invoke(values)
            }
        }

    suspend fun getNetworkSocket(onMessageListener: (List<Interface>) -> Unit): WebSocket =
        withContext(Dispatchers.IO) {
            val converter = NetworkInterfaceConverter()
            socketFactory.create("/update3") { text ->
                val networkInterface = converter.convert("", text ?: "")
                onMessageListener.invoke(networkInterface)
            }
        }

    suspend fun getParitySocket(onMessageListener: (Parity) -> Unit): WebSocket =
        withContext(Dispatchers.IO) {
            // showing mo≈œœck parity data for the test server
            if (isTestServer) {
                val data = ParityData.Valid(Date(), "0", "12 hours", "35 MB/S", Date())
                onMessageListener.invoke(Parity(ParityStatus.Valid, data))
            }

            val parityConverter = ParityConverter()
            val parityStatusConverter = ParityStatusConverter()
            socketFactory.create("/update2") { text ->
                if (text == null)
                    return@create

                val replace = text.replace("\u0010", "")
                    .replace("\u0000", "")
                    .replace("\u0001", "")
                    .replace("0010", "")
                    .replace("0000", "")
                    .replace("\r", "")
                    .trim()

                val index = replace.lastIndexOf("</tr>") + "</tr>".length

                val status = parityStatusConverter.getParityStatus("", text) ?: ParityStatus.Invalid
                val data = parityConverter.getParityData("", replace.substring(index))

                onMessageListener.invoke(Parity(status, data))
            }
        }

    suspend fun getDevicesSocket(onMessageListener: (List<Device>, List<Device>, List<Device>) -> Unit): WebSocket =
        withContext(Dispatchers.IO) {
            // showing test devices for the test server
            if (isTestServer) {
                onMessageListener.invoke(
                    listOf(
                        getTestDevice("parity"),
                        getTestDevice("disk 1"),
                        getTestDevice("disk 2")
                    ), listOf(getTestDevice("cache")), listOf(getTestDevice("flash"))
                )
            }

            val converter = DeviceConverter()

            socketFactory.create("/devices") { text ->
                val devices = converter.convert("", text ?: "")
                val array =
                    devices.filter {
                        it.device.contains(
                            "Disk",
                            ignoreCase = true
                        ) || it.device.contains("Parity", ignoreCase = true)
                    }
                val cache = devices.filter { it.device.contains("Cache", ignoreCase = true) }
                val flash = devices.filter { it.device.contains("Flash", ignoreCase = true) }
                onMessageListener.invoke(array, cache, flash)
            }
        }


    suspend fun getDockerContainers(): Response<List<DockerContainer>> =
        withContext(Dispatchers.IO) {
            try {
                if (isTestServer) {
                    return@withContext Response.Success(listOf(createMockDockerContainer()))
                }

                val address = config.getAddress() ?: ""
                val data = service.getDockerContainers().map {
                    it.setAddress(address)
                }
                Response.Success(data)
            } catch (ex: Exception) {
                onError(ex, "Could not getDockerContainers")
                Response.Error(ex)
            }
        }

    suspend fun getVirtualMachines(): Response<List<VirtualMachine>> = withContext(Dispatchers.IO) {
        try {
            if (isTestServer) {
                return@withContext Response.Success(listOf(createMockVirtualMachine()))
            }

            val address = config.getAddress() ?: ""
            val data = service.getVirtualMachines().map {
                it.setAddress(address)
            }
            Response.Success(data)
        } catch (ex: Exception) {
            onError(ex, "Could not getVirtualMachines")
            Response.Error(ex)
        }
    }

    suspend fun getDetailedDevices(device: String): Response<List<Device>> =
        withContext(Dispatchers.IO) {
            try {
                Response.Success(service.getDiskList("Main", device, config.getConfig().csrf))
            } catch (ex: Exception) {
                onError(ex, "Could not getDetailedDevices")
                Response.Error(ex)
            }
        }

    suspend fun getDevices(device: String): Response<List<Device>> = withContext(Dispatchers.IO) {
        try {
            Response.Success(service.getDevices(device, config.getConfig().csrf))
        } catch (ex: Exception) {
            onError(ex, "Could not getDevices")
            Response.Error(ex)
        }
    }

    suspend fun getMemoryUsage(): Response<List<Usage>> = withContext(Dispatchers.IO) {
        try {
            Response.Success(service.getMemoryUsage(token = config.getConfig().csrf))
        } catch (ex: Exception) {
            onError(ex, "Could not getMemoryUsage")
            Response.Error(ex)
        }
    }

    // Parity

    suspend fun getParityStatus(): Response<Parity> = withContext(Dispatchers.IO) {
        try {
            val status = service.getParityStatus(token = config.getConfig().csrf)
            val data = service.getParity(token = config.getConfig().csrf)
            Response.Success(Parity(status, data))
        } catch (ex: Exception) {
            onError(ex, "Could not getParityStatus")
            Response.Error(ex)
        }
    }

    suspend fun startParityCheck(): Response<Boolean> = withContext(Dispatchers.IO) {
        try {
            // todo: pass in boolean flag
            val optionCorrect = "correct"
            service.startParityCheck(optionCorrect = optionCorrect, token = config.getConfig().csrf)
            Response.Success(true)
        } catch (ex: Exception) {
            onError(ex, "Could not start parity check")
            Response.Error(ex)
        }
    }

    suspend fun resumeParityCheck(): Response<Boolean> = withContext(Dispatchers.IO) {
        try {
            service.resumeParityCheck(token = config.getConfig().csrf)
            Response.Success(true)
        } catch (ex: Exception) {
            onError(ex, "Could not resume parity check")
            Response.Error(ex)
        }
    }

    suspend fun pauseParityCheck(): Response<Boolean> = withContext(Dispatchers.IO) {
        try {
            service.pauseParityCheck(token = config.getConfig().csrf)
            Response.Success(true)
        } catch (ex: Exception) {
            onError(ex, "Could not pause parity check")
            Response.Error(ex)
        }
    }

    suspend fun getShareStreams(): Response<String> = withContext(Dispatchers.IO) {
        try {
            // todo: get this list from users
            val names = "appdata,domain,isos,media,system,torrents"
            Response.Success(service.getShareStreams(names, config.getConfig().csrf))
        } catch (ex: Exception) {
            onError(ex, "Could not getShareStreams")
            Response.Error(ex)
        }
    }

    suspend fun getNotifications(filter: Boolean = true): Response<List<Notification>> =
        withContext(Dispatchers.IO) {
            try {
                val token = config.getConfig().csrf
                service.initNotifications(token)
                delay(1_000)
                val data = service.getNotifications(token)

                val lastNotification: String? = storage.lastNotification

                // Filtering out any old notifications
                val list = data.filter {
                    !filter || DateComparator.isAfter(
                        lastNotification,
                        it.timestamp
                    )
                }
                if (list.isNotEmpty()) {
                    storage.lastNotification = data.last().timestamp
                }

                Response.Success(list)
            } catch (ex: Exception) {
                onError(ex, "Could not getNotifications")
                Response.Error(ex)
            }
        }

    suspend fun dismissNotification(id: String): Response<Unit> = withContext(Dispatchers.IO) {
        try {
            Response.Success(service.dismissNotification(id, config.getConfig().csrf))
        } catch (ex: Exception) {
            onError(ex, "Could not dismissNotification")
            Response.Error(ex)
        }
    }

    suspend fun getShares(): Response<List<Share>> = withContext(Dispatchers.IO) {
        try {
            if (isTestServer) {
                return@withContext Response.Success(listOf(createMockShare()))
            }

            Response.Success(service.getShareList())
        } catch (ex: Exception) {
            onError(ex, "Could not getShares")
            Response.Error(ex)
        }
    }

    suspend fun getShareDirectory(dir: String): Response<List<Directory>> =
        withContext(Dispatchers.IO) {
            try {
                Response.Success(service.getShareDirectory(dir))
            } catch (ex: Exception) {
                onError(ex, "Could not getShareDirectory")
                Response.Error(ex)
            }
        }

    suspend fun sendDockerContainerAction(
        id: String,
        name: String,
        action: String
    ): Response<ActionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val data = if (action == "remove_image") {
                    service.removeImage(action, id, config.getConfig().csrf)
                } else {
                    service.sendAction(action, id, name, config.getConfig().csrf)
                }

                if (!data.success.toBoolean()) {
                    val ex = IllegalStateException(data.success)
                    onError(ex, "Could not sendDockerContainerAction")
                    Response.Error(ex)
                } else {
                    Response.Success(data)
                }
            } catch (ex: Exception) {
                onError(ex, "Could not sendDockerContainerAction")
                Response.Error(ex)
            }
        }

    suspend fun sendVMAction(
        id: String,
        name: String,
        action: String
    ): Response<ActionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val data =
                    service.sendVirtualMachineAction(action, id, name, config.getConfig().csrf)
                if (!data.success.toBoolean()) {
                    val ex = IllegalStateException(data.error ?: data.success)
                    onError(ex, "Could not sendVMAction")
                    Response.Error(ex)
                } else {
                    Response.Success(data)
                }
            } catch (ex: Exception) {
                onError(ex, "Could not sendVMAction")
                Response.Error(ex)
            }
        }

    suspend fun updateDockerContainer(container: String): Response<Any> =
        withContext(Dispatchers.IO) {
            try {
                Response.Success(service.updateDockerContainer(container))
            } catch (ex: Exception) {
                onError(ex, "Could not updateDockerContainer")
                Response.Error(ex)
            }
        }

    suspend fun getDockerContainerLogs(container: String): Response<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val data = service.getDockerContainerLogs(container)
                Response.Success(listOf(data))
            } catch (ex: Exception) {
                onError(ex, "Could not getDockerContainerLogs")
                Response.Error(ex)
            }
        }

    suspend fun getUPSStatus(): Response<UPSStatus> = withContext(Dispatchers.IO) {
        try {
            if (isTestServer) {
                return@withContext Response.Success(createMockUPSStatus())
            }

            val data = service.getUPSStatus() ?: throw error("ups is null")
            Response.Success(data)
        } catch (ex: Exception) {
            onError(ex, "Could not getUPSStatus")
            Response.Error(ex)
        }
    }

    // Array Commands

    suspend fun startArray(): Response<Boolean> = withContext(Dispatchers.IO) {
        try {
            service.startArray(token = config.getConfig().csrf)
            Response.Success(true)
        } catch (ex: Exception) {
            onError(ex, "Could not start array")
            Response.Error(ex)
        }
    }

    suspend fun stopArray(): Response<Boolean> = withContext(Dispatchers.IO) {
        try {
            service.stopArray(token = config.getConfig().csrf)
            Response.Success(true)
        } catch (ex: Exception) {
            onError(ex, "Could not stop array")
            Response.Error(ex)
        }
    }

    suspend fun startMover(): Response<Boolean> = withContext(Dispatchers.IO) {
        try {
            service.startMover(token = config.getConfig().csrf)
            Response.Success(true)
        } catch (ex: Exception) {
            onError(ex, "Could not start mover")
            Response.Error(ex)
        }
    }

    // System Commands

    suspend fun rebootSystem(): Response<Boolean> = withContext(Dispatchers.IO) {
        try {
            service.reboot("reboot", config.getConfig().csrf)
            Response.Success(true)
        } catch (ex: Exception) {
            onError(ex, "Could not reboot system")
            Response.Error(ex)
        }
    }

    suspend fun shutdownSystem(): Response<Boolean> = withContext(Dispatchers.IO) {
        try {
            service.reboot("shutdown", config.getConfig().csrf)
            Response.Success(true)
        } catch (ex: Exception) {
            onError(ex, "Could not shutdown system")
            Response.Error(ex)
        }
    }

    private fun setAnalyticsUUID() {
        val uuid = storage.uuid
        firebaseCrashlytics.setUserId(uuid)
        firebaseAnalytics.setUserId(uuid)
    }

    private fun updateAnalytics(config: HashMap<String, String>) {
        firebaseCrashlytics.log("setting version to: " + config["version"])
        firebaseAnalytics.setUserProperty("version", config["version"])
        firebaseCrashlytics.setCustomKey("version", config["version"] ?: "0.0.0")
    }

    private fun onError(ex: Exception, msg: String) {
        Timber.e(msg)
        Timber.e(ex)

        // invalid login credentials
        if (ex.message?.contains("invalid credentials") == true)
            return

        // unable to resolve host
        if (ex is IOException)
            return

        // 404
        if (ex is HttpException && ex.code() == 404)
            return

        // Coroutine job was cancelled
        if (ex.message?.contains("Job was cancelled") == true)
            return

        firebaseCrashlytics.log("$msg: ${ex.message}")
        firebaseCrashlytics.recordException(ex)
    }

    private val isTestServer
        get() = config.getConfig().isDeveloper
}