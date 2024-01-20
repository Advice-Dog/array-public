package com.advice.array.api

import com.advice.array.api.response.ActionResponse
import com.advice.array.api.response.LoginResponse
import com.advice.array.models.*
import org.jsoup.nodes.Document
import retrofit2.http.*

interface UnraidService {

    @GET("login")
    suspend fun getLogin(): Document

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String?
    ): LoginResponse

    @GET("Dashboard")
    suspend fun getDashboard(): DashboardResponse


    @GET("plugins/dynamix.vm.manager/include/VMMachines.php")
    suspend fun getVirtualMachines(): List<VirtualMachine>

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("plugins/dynamix.vm.manager/include/VMajax.php")
    suspend fun sendVirtualMachineAction(
        @Field("action") cmd: String = "stop",
        @Field("uuid") container: String,
        @Field("name") name: String,
        @Field("csrf_token") token: String,
    ): ActionResponse

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("webGui/include/DeviceList.php")
    suspend fun getDiskList(
        @Field("path") path: String = "Main",
        @Field("device") device: String = "array",
        @Field("csrf_token") token: String
    ): List<Device>

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("webGui/include/DashUpdate.php")
    suspend fun getDevices(
        @Field("cmd") cmd: String,
        @Field("csrf_token") token: String,
        @Field("path") path: String = "Dashboard",
        @Field("hot") hot: String = "45",
        @Field("max") max: String = "55",
        @Field("warning") warning: String = "70",
        @Field("critical") critical: String = "90",
        @Field("unit") unit: String = "C",
        @Field("text") text: String = "1"
    ): List<Device>

    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("webGui/include/DashUpdate.php")
    suspend fun getNetworkingDetails(@Body body: String): List<Interface>

    // Parity

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("webGui/include/DashUpdate.php")
    suspend fun getParity(
        @Field("cmd") cmd: String = "parity",
        @Field("time") time: String = "%c",
        @Field("csrf_token") token: String
    ): ParityData

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("webGui/include/DashUpdate.php")
    suspend fun getParityStatus(
        @Field("cmd") cmd: String = "status",
        @Field("number") time: String = ".,",
        @Field("csrf_token") token: String
    ): ParityStatus

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("update.htm")
    suspend fun startParityCheck(
        @Field("startState") startState: String = "STARTED",
        @Field("cmdCheck") cmdCheck: String = "Check",
        @Field("optionCorrect") optionCorrect: String = "correct",
        @Field("csrf_token") token: String
    )

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("update.htm")
    suspend fun resumeParityCheck(
        @Field("startState") startState: String = "STARTED",
        @Field("cmdCheck") cmdCheck: String = "Resume",
        @Field("csrf_token") token: String
    )

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("update.htm")
    suspend fun pauseParityCheck(
        @Field("startState") startState: String = "STARTED",
        @Field("cmdNoCheck") cmdCheck: String = "Pause",
        @Field("csrf_token") token: String
    )


    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("webGui/include/DashUpdate.php")
    suspend fun getMemoryUsage(
        @Field("cmd") cmd: String = "sys",
        @Field("csrf_token") token: String
    ): List<Usage>

    @GET("webGui/include/ShareList.php")
    suspend fun getShareList(): List<Share>

    @GET("webGui/include/Browse.php")
    suspend fun getShareDirectory(
        @Query("dir") dir: String = "/mnt/user/appdata/binhex-delugevpn",
        @Query("path") path: String = "Shares/Browse",
        @Query("user") user: Int = 1
    ): List<Directory>

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("webGui/include/DashUpdate.php")
    suspend fun getShareStreams(
        @Field("names") names: String,
        @Field("csrf_token") token: String,
        @Field("cmd") cmd: String = "shares",
        @Field("com") com: String = "smb"
    ): String

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("webGui/include/Notify.php")
    suspend fun initNotifications(
        @Field("csrf_token") token: String,
        @Field("cmd") cmd: String = "init"
    )

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("webGui/include/Notify.php")
    suspend fun getNotifications(
        @Field("csrf_token") token: String,
        @Field("cmd") cmd: String = "get"
    ): List<Notification>

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("webGui/include/Notify.php")
    suspend fun dismissNotification(
        @Field("file") file: String,
        @Field("csrf_token") token: String,
        @Field("cmd") cmd: String = "archive"
    )

    // UPS
    @GET("plugins/dynamix.apcupsd/include/UPSstatus.php")
    suspend fun getUPSStatus(
        @Query("all") all: Boolean = false
    ): UPSStatus?


    // Docker

    @GET("plugins/dynamix.docker.manager/include/DockerContainers.php")
    suspend fun getDockerContainers(): List<DockerContainer>

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("plugins/dynamix.docker.manager/include/Events.php")
    suspend fun sendAction(
        @Field("action") cmd: String = "stop",
        @Field("container") container: String,
        @Field("name") name: String,
        @Field("csrf_token") token: String,
    ): ActionResponse

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("plugins/dynamix.docker.manager/include/Events.php")
    suspend fun removeImage(
        @Field("action") cmd: String = "remove_image",
        @Field("image") image: String,
        @Field("csrf_token") token: String,
    ): ActionResponse

    @GET("plugins/dynamix.docker.manager/include/CreateDocker.php")
    suspend fun updateDockerContainer(
        @Query("ct[]") containers: String,
        @Query("updateContainer") updateContainer: Boolean = true
    ): Document

    @GET("plugins/dynamix.docker.manager/include/Events.php")
    suspend fun getDockerContainerLogs(
        @Query("container") container: String,
        @Query("action") action: String = "log"
    ): String

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("update.htm")
    suspend fun startArray(
        @Field("startState") startState: String = "STOPPED",
        @Field("cmdStart") cmdStart: String = "Start",
        @Field("csrf_token") token: String
    )

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("update.htm")
    suspend fun stopArray(
        @Field("startState") startState: String = "STARTED",
        @Field("cmdStop") cmdStop: String = "Stop",
        @Field("csrf_token") token: String
    )

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("update.htm")
    suspend fun startMover(
        @Field("cmdStartMover") startState: String = "Move",
        @Field("csrf_token") token: String
    )

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @POST("webGui/include/Boot.php")
    suspend fun reboot(
        @Field("cmd") cmd: String = "reboot",
        @Field("csrf_token") token: String,
    )
}