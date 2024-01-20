package com.advice.array.api

import com.advice.array.api.converters.*
import com.advice.array.api.response.LoginResponse
import com.advice.array.models.DashboardResponse
import com.advice.array.models.ParityData
import com.advice.array.models.ParityStatus
import com.advice.array.models.UPSStatus
import okhttp3.ResponseBody
import org.jsoup.nodes.Document
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

object JsoupConverterFactory : Converter.Factory(), KoinComponent {

    private val memoryUsageConverter = MemoryUsageConverter()
    private val dockerContainerConverter by inject<DockerContainerConverter>()
    private val virtualMachineConverter by inject<VirtualMachineConverter>()
    private val deviceConverter = DeviceConverter()
    private val networkInterfaceConverter = NetworkInterfaceConverter()
    private val directoryConverter = DirectoryConverter()
    private val shareConverter: ShareConverter by inject()

    private val loginResponseConverter by inject<LoginResponseConverter>()
    private val dashboardResponseConverter by inject<DashboardResponseConverter>()
    private val parityConverter = ParityConverter()
    private val parityStatusConverter = ParityStatusConverter()
    private val upsConverter by inject<UPSConverter>()

    val converters = listOf<UnraidConverter<*>>(
        memoryUsageConverter,
        dockerContainerConverter,
        virtualMachineConverter,
        deviceConverter,
        networkInterfaceConverter,
        directoryConverter,
        shareConverter,
        loginResponseConverter,
        dashboardResponseConverter,
        parityConverter,
        parityStatusConverter,
        upsConverter
    )

    private var isSetup = true

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        val baseUri = retrofit.baseUrl().toString()

        if (isSetup) {
            converters.forEach {
                it.baseUri = baseUri
            }
            isSetup = false
        }

        if ("List" in type.toString() && "Usage" in type.toString()) {
            return memoryUsageConverter
        }
        if (type.toString().contains("DockerContainer")) {
            return dockerContainerConverter
        }
        if (type.toString().contains("VirtualMachine")) {
            return virtualMachineConverter
        }
        if (type.toString().contains("Device")) {
            return deviceConverter
        }
        if (type.toString().contains("Interface")) {
            return networkInterfaceConverter
        }
        if (type.toString().contains("Share")) {
            return shareConverter
        }
        if (type.toString().contains("Directory")) {
            return directoryConverter
        }

        return when (type) {
            LoginResponse::class.java -> loginResponseConverter
            DashboardResponse::class.java -> dashboardResponseConverter
            ParityData::class.java -> parityConverter
            ParityStatus::class.java -> parityStatusConverter
            UPSStatus::class.java -> upsConverter
            Document::class.java -> JsoupConverter(baseUri)
            else -> null
        }
    }
}