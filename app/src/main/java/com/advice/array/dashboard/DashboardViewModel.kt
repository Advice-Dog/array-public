package com.advice.array.dashboard

import androidx.lifecycle.*
import com.advice.array.analytics.AnalyticsManager
import com.advice.array.api.LocalCookieJar
import com.advice.array.api.UnraidRepository
import com.advice.array.api.response.LoginResponse
import com.advice.array.api.response.Response
import com.advice.array.models.*
import com.advice.array.utils.Storage
import com.advice.array.utils.VersionParser
import com.neovisionaries.ws.client.WebSocket
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class DashboardViewModel(loginResponse: LoginResponse) : ViewModel(), KoinComponent {

    private val analytics: AnalyticsManager by inject()
    private val repository: UnraidRepository by inject()
    private val storage: Storage by inject()
    private val localCookieJar: LocalCookieJar by inject()

    val arrayStatus: LiveData<Boolean>
        get() = _arrayStatus

    val server: LiveData<LoginResponse?>
        get() = _server

    val dashboard: LiveData<DashboardResponse?>
        get() = _dashboard

    val uptime: LiveData<String>
        get() = _uptime

    val notifications: LiveData<List<Notification>>
        get() = _notifications

    val cpuLoad: LiveData<List<Usage>?>
        get() = _cpuLoad

    val cpuHistory: LiveData<List<Int>>
        get() = _cpuHistory

    val memoryUsage: LiveData<List<Usage>?>
        get() = _memoryUsage

    val containers: LiveData<List<DockerContainer>>
        get() = _containers

    val dashboardShares: LiveData<List<DashboardShare>>
    val shares: LiveData<List<Share>>
        get() = _shares

    val vms: LiveData<List<VirtualMachine>>

    val array: LiveData<DevicesResponse>
        get() = _array

    val cache: LiveData<DevicesResponse>
        get() = _cache

    val flash: LiveData<DevicesResponse>
        get() = _flash

    val parity: LiveData<Parity?>
        get() = _parity

    val ups: LiveData<UPSStatus?>
        get() = _ups

    val networkHistory: LiveData<List<Pair<Long, Long>>>
        get() = _networkHistory

    val network: LiveData<List<Interface>>
        get() = _network

    val reboot: LiveData<Boolean?>
        get() = _reboot

    val uuid: LiveData<String>
        get() = _uuid

    private val _arrayStatus = MutableLiveData<Boolean>()

    private val _server = MutableLiveData<LoginResponse?>()
    private val _dashboard = MutableLiveData<DashboardResponse?>()
    private val _uptime = MutableLiveData<String>()

    private val _notifications = MutableLiveData<List<Notification>>()
    private val _cpuLoad = MutableLiveData<List<Usage>>()
    private val _cpuHistory = MutableLiveData<List<Int>>()
    private val _memoryUsage = MutableLiveData<List<Usage>>()
    private val _containers = MutableLiveData<List<DockerContainer>>()
    private val _vms = MutableLiveData<List<VirtualMachine>>()

    private val _array = MutableLiveData<DevicesResponse>(DevicesResponse.Loading)
    private val _cache = MutableLiveData<DevicesResponse>(DevicesResponse.Loading)
    private val _flash = MutableLiveData<DevicesResponse>(DevicesResponse.Loading)
    private val _parity = MutableLiveData<Parity?>()
    private val _network = MutableLiveData<List<Interface>>()
    private val _networkHistory = MutableLiveData<List<Pair<Long, Long>>>()

    private val _ups = MutableLiveData<UPSStatus?>()

    private val _shares = MutableLiveData<List<Share>>()

    private val _reboot = MutableLiveData<Boolean?>()

    private val _uuid = MutableLiveData<String>()

    private var cpuSocket: WebSocket? = null
    private var networkSocket: WebSocket? = null
    private var varSocket: WebSocket? = null
    private var paritySocket: WebSocket? = null
    private var devicesSocket: WebSocket? = null

    init {
        dashboardShares = Transformations.switchMap(_shares) {
            val result = MutableLiveData<List<DashboardShare>>()
            result.value = it.map { DashboardShare(it.name, it.comment, it.smb, 0) }
            return@switchMap result
        }

        vms = Transformations.switchMap(_vms) {
            val result = MutableLiveData<List<VirtualMachine>>()
            result.value = it
            return@switchMap result
        }

        storage.server = loginResponse
        _server.value = loginResponse
        _uuid.value = storage.uuid

        analytics.setUserProperties(loginResponse)

        initComponents()
    }

    fun getServer() {
        _server.value = storage.server
    }

    private fun initComponents() {
        // sockets
        getCpuLoad()
        getVar()
        getNetworkStats()

        // looping
        getMemoryUsage()
        getDevices()
        getShares()
        getUptime()
        getParity()

        getDashboard()
        getContainers()
        getVirtualMachines()
        getUPSStatus()
    }

    private fun getUptime() {
        viewModelScope.launch {
            while (isActive) {
                val start = _server.value?.startDate
                if (start != null) {
                    val now = Date()
                    val difference = (now.time - start.time) / 1000
                    val totalMinutes = difference.toTimeStamp()
                    _uptime.postValue(totalMinutes)
                }
                delay(5_000)
            }
        }
    }

    private fun getShares() {
        viewModelScope.launch {
            while (isActive) {
                updateShares()
                delay(30_000)
            }
        }
    }

    private suspend fun updateShares() {
        when (val result = repository.getShares()) {
            is Response.Success -> {
                _shares.postValue(result.data)
            }
        }
    }

    private fun getNotifications() {
        viewModelScope.launch {
            while (isActive) {
                when (val result = repository.getNotifications()) {
                    is Response.Success -> {
                        _notifications.postValue(result.data)
                    }
                }

                delay(15_000)
            }
        }
    }

    suspend fun _login() {
        val address = storage.address
        val (username, password) = storage.username to storage.password
        if (address != null && username != null && password != null)
            _login(address, username, password)
    }

    private suspend fun _login(address: String?, username: String?, password: String?) {
        if (address == null || username == null) {
            storage.server = null
            _server.postValue(null)
            _dashboard.postValue(null)
            return
        }

        when (val result = repository.login(address, username, password)) {
            is Response.Success -> {
                storage.server = result.data
                _server.postValue(result.data)

                storage.username = username
                storage.password = password

                updateDashboard()

                initComponents()
            }
            is Response.Error -> {
                storage.server = null
                _server.postValue(null)
                _dashboard.postValue(null)
            }
        }
    }

    private suspend fun updateDashboard() {
        when (val dashboard = repository.getDashboard()) {
            is Response.Success -> {
                when (dashboard.data.arrayStatus) {
                    "Array Started" -> _arrayStatus.postValue(true)
                    else -> _arrayStatus.postValue(false)
                }
                _dashboard.postValue(dashboard.data)
            }
            is Response.Error -> {
                _dashboard.postValue(null)
            }
        }
    }

    private fun getParity() {
        viewModelScope.launch {
            if (isVersion("6.10.0-rc1")) {
                paritySocket = repository.getParitySocket { parity ->
                    _parity.postValue(parity)
                }
            } else {
                while (isActive) {
                    updateParity()
                    delay(30_000)
                }
            }
        }
    }

    private fun getNetworkStats() {
        viewModelScope.launch {
            networkSocket = repository.getNetworkSocket {
                _network.postValue(it)

                // adding total to history
                val history = _networkHistory.value ?: emptyList()
                val toMutableList = history.toMutableList()
                toMutableList.add(it.first().inboundSpeed to it.first().outboundSpeed)
                _networkHistory.postValue(toMutableList.takeLast(60))
            }
        }
    }

    private fun getVar() {
        viewModelScope.launch {
            varSocket = repository.getVariableSocket()
        }
    }

    private fun getCpuLoad() {
        viewModelScope.launch {
            cpuSocket = repository.getCpuUsage {
                _cpuLoad.postValue(it)

                // adding total to history
                val history = _cpuHistory.value ?: emptyList()
                val toMutableList = history.toMutableList()
                toMutableList.add(it.first().amount)
                _cpuHistory.postValue(toMutableList.takeLast(60))
            }
        }
    }

    private fun getDashboard() {
        viewModelScope.launch {
            while (isActive) {
                updateDashboard()
                delay(15_000)
            }
        }
    }

    private fun getMemoryUsage() {
        viewModelScope.launch {
            if (isVersion("6.10.0-rc1")) {
                repository.getMemorySocket {
                    _memoryUsage.postValue(it)
                }
            } else {
                while (isActive) {
                    updateMemory()
                    delay(10_000)
                }
            }
        }
    }

    private suspend fun updateMemory() {
        when (val load = repository.getMemoryUsage()) {
            is Response.Success -> {
                _memoryUsage.postValue(load.data)
            }
        }
    }

    private fun getDevices() {
        viewModelScope.launch {
            if (isVersion("6.10.0-rc1")) {
                devicesSocket = repository.getDevicesSocket { array, cache, flash ->
                    _array.postValue(DevicesResponse.Success(array))
                    _cache.postValue(DevicesResponse.Success(cache))
                    _flash.postValue(DevicesResponse.Success(flash))
                }
            } else {
                while (isActive) {
                    updateArray()
                    delay(25_000)
                }
            }
        }
    }

    private suspend fun updateArray() {
        when (val array = repository.getDetailedDevices("array")) {
            is Response.Success -> {
                _array.postValue(DevicesResponse.Success(array.data))
            }
            is Response.Error -> {
                _array.postValue(DevicesResponse.Failure(array.exception))
            }
        }
        when (val cache = repository.getDetailedDevices("cache")) {
            is Response.Success -> {
                _cache.postValue(DevicesResponse.Success(cache.data))
            }
            is Response.Error -> {
                _cache.postValue(DevicesResponse.Failure(cache.exception))
            }
        }
        when (val flash = repository.getDetailedDevices("flash")) {
            is Response.Success -> {
                _flash.postValue(DevicesResponse.Success(flash.data))
            }
            is Response.Error -> {
                _flash.postValue(DevicesResponse.Failure(flash.exception))
            }
        }
    }

    private fun getContainers() {
        viewModelScope.launch {
            while (isActive) {
                updateDockerContainers()
                delay(15_000)
            }
        }
    }

    private fun getVirtualMachines() {
        viewModelScope.launch {
            while (isActive) {
                updateVirtualMachines()
                delay(15_000)
            }
        }
    }

    private suspend fun updateVirtualMachines() {
        when (val response = repository.getVirtualMachines()) {
            is Response.Success -> {
                _vms.postValue(response.data)
            }
        }
    }

    fun getDockerContainer(container: DockerContainer): LiveData<DockerContainer> {
        val result = MediatorLiveData<DockerContainer>()
        result.value = container

        result.addSource(_containers) {
            val container = it.find { it.id == container.id }
            if (container != null) {
                result.postValue(container)
            }
        }

        return result
    }

    fun send(id: String, name: String, action: String): LiveData<String?> {
        val result = MutableLiveData<String>()
        // null as 'loading'
        result.postValue(null)

        viewModelScope.launch {
            when (val response = repository.sendDockerContainerAction(id, name, action)) {
                is Response.Success -> {
                    // refreshing the list of docker containers
                    updateDockerContainers()

                    result.postValue(response.data.success)
                }
                is Response.Error -> {
                    result.postValue(response.exception.message)
                }
            }
        }
        return result
    }

    fun sendVirtualMachine(id: String, name: String, action: String): LiveData<String?> {
        val result = MutableLiveData<String>()
        // null as 'loading'
        result.postValue(null)

        viewModelScope.launch {
            when (val response = repository.sendVMAction(id, name, action)) {
                is Response.Success -> {
                    // refreshing the list of docker containers
                    updateVirtualMachines()

                    result.postValue(response.data.success)
                }
                is Response.Error -> {
                    result.postValue(response.exception.message)
                }
            }
        }
        return result
    }

    private suspend fun updateDockerContainers() {
        when (val response = repository.getDockerContainers()) {
            is Response.Success -> {
                _containers.postValue(response.data)
            }
        }
    }

    fun update(container: String) {
        viewModelScope.launch {
            when (repository.updateDockerContainer(container)) {
                is Response.Success -> {

                }
                is Response.Error -> {

                }
            }
        }
    }

    fun getVirtualMachine(virtualMachine: VirtualMachine): LiveData<VirtualMachine> {
        val result = MediatorLiveData<VirtualMachine>()
        result.value = virtualMachine

        result.addSource(_vms) {
            val vm = it.find { it.id == virtualMachine.id }
            if (vm != null) {
                result.postValue(vm)
            }
        }

        return result
    }

    private fun getUPSStatus() {
        viewModelScope.launch {
            while (isActive) {
                when (val response = repository.getUPSStatus()) {
                    is Response.Success -> {
                        _ups.postValue(response.data)
                    }
                }
                delay(15_000)
            }
        }
    }

    override fun onCleared() {
        closeSockets()
    }

    fun logout() {
        storage.config = null
        storage.password = null
        storage.username = null

        closeSockets()
    }

    private fun closeSockets() {
        cpuSocket?.disconnect()
        varSocket?.disconnect()
        paritySocket?.disconnect()
        devicesSocket?.disconnect()
    }

    fun onResume() {
        // reconnecting sockets
        if (cpuSocket.isClosedOrNull()) {
            getCpuLoad()
        }
        if (networkSocket.isClosedOrNull()) {
            getNetworkStats()
        }
        if (paritySocket.isClosedOrNull() && isVersion("6.10.0-rc1")) {
            getParity()
        }
        if (devicesSocket.isClosedOrNull() && isVersion("6.10.0-rc1")) {
            getDevices()
        }
    }

    private fun WebSocket?.isClosedOrNull(): Boolean {
        return this == null || !this.isOpen
    }

    fun reboot(): LiveData<Boolean> {
        val response = MutableLiveData<Boolean>()

        // loading
        response.postValue(null)

        viewModelScope.launch {
            when (val result = repository.rebootSystem()) {
                is Response.Success -> {
                    _reboot.postValue(true)
                    response.postValue(result.data)
                    // command successful, loop and try to reconnect
                    onCleared()

                    _server.postValue(null)
                    _dashboard.postValue(null)

                    while (response.hasActiveObservers()) {
                        // Keep logging in.
                        _login()

                        // Logged back in, break out.
                        if (_dashboard.value != null) {
                            _reboot.postValue(null)
                            break
                        }
                        delay(5_000)
                    }
                }
                is Response.Error -> {
                    response.postValue(false)
                }
            }
        }

        return response
    }

    fun getRebootTimer(rebooting: Boolean): LiveData<Long> {
        val result = MutableLiveData<Long>()

        val start = Date().time

        viewModelScope.launch {
            // delay for the label and to get an observer.
            delay(5_000)
            while (result.hasActiveObservers()) {
                val milliseconds = Date().time - start
                val time = if (rebooting) {
                    milliseconds
                } else {
                    10_000 - milliseconds
                }
                result.postValue(time)
                delay(500)
            }
        }

        return result
    }

    fun shutdown() {
        viewModelScope.launch {
            when (val result = repository.shutdownSystem()) {
                is Response.Success -> {
                    _reboot.postValue(false)
                    onCleared()
                    _server.postValue(null)
                    _dashboard.postValue(null)
                }
                is Response.Error -> {

                }
            }
        }
    }

// Parity

    fun startParityCheck(): LiveData<Boolean> {
        val ret = MutableLiveData<Boolean>()

        viewModelScope.launch {
            when (val result = repository.startParityCheck()) {
                is Response.Success -> {
                    updateParity()
                    ret.postValue(true)
                }
                is Response.Error -> {
                    ret.postValue(false)
                }
            }
        }

        return ret
    }

    private suspend fun updateParity() {
        when (val result = repository.getParityStatus()) {
            is Response.Success -> {
                _parity.postValue(result.data)
            }
            is Response.Error -> {
                _parity.postValue(null)
            }
        }
    }

    fun resumeParityCheck() {
        viewModelScope.launch {
            repository.resumeParityCheck()
            updateParity()
        }
    }

    fun pauseParityCheck(): LiveData<Boolean> {
        val ret = MutableLiveData<Boolean>()

        viewModelScope.launch {
            when (val result = repository.pauseParityCheck()) {
                is Response.Success -> {
                    updateParity()
                    ret.postValue(true)
                }
                is Response.Error -> {
                    ret.postValue(false)
                }
            }
            updateParity()
        }

        return ret
    }

    fun startArray(): LiveData<Boolean> {
        val ret = MutableLiveData<Boolean>()

        viewModelScope.launch {
            when (val result = repository.startArray()) {
                is Response.Success -> {
                    updateArray()
                    ret.postValue(true)
                    updateDockerContainers()
                    updateShares()
                }
                is Response.Error -> {
                    ret.postValue(false)
                }
            }
        }

        return ret
    }

    fun stopArray(): LiveData<Boolean> {
        val ret = MutableLiveData<Boolean>()

        viewModelScope.launch {
            when (val result = repository.stopArray()) {
                is Response.Success -> {
                    updateArray()
                    ret.postValue(true)
                }
                is Response.Error -> {
                    ret.postValue(false)
                }
            }
        }

        return ret
    }

    fun startMover(): LiveData<Boolean> {
        val ret = MutableLiveData<Boolean>()

        viewModelScope.launch {
            when (val result = repository.startMover()) {
                is Response.Success -> {
                    ret.postValue(true)
                }
                is Response.Error -> {
                    ret.postValue(false)
                }
            }
        }

        return ret
    }

    fun getServerVersion(): String? = _server.value?.config?.get("version")

    private fun isVersion(target: String) =
        VersionParser.isGreaterOrEqual(getServerVersion(), target, false)

}

// input in seconds
fun Long.toTimeStamp(): String {
    val days = this / 86400
    val hours = this / 3600 % 24
    val minutes = this / 60 % 60

    val list = ArrayList<Pair<String, Long>>()
    list.add("day" to days)
    list.add("hour" to hours)
    list.add("minute" to minutes)

    return list.filter { it.second != 0L }.joinToString(separator = ", ") {
        val postfix = if (it.second != 1L) "s" else ""
        "${it.second} ${it.first}${postfix}"
    }
}
