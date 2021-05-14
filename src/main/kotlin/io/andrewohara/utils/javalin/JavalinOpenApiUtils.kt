package io.andrewohara.utils.javalin

import io.javalin.core.JavalinConfig
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.info.Info

object JavalinOpenApiUtils {

    fun registerUi(config: JavalinConfig, serviceName: String, version: String = "1") {
        val applicationInfo = Info().apply {
            this.title = serviceName
            this.version = version
        }
        val options = OpenApiOptions(applicationInfo)
            .path("/spec")
            .reDoc(ReDocOptions("/redoc"))
            .swagger(SwaggerOptions("/"))

        config.registerPlugin(OpenApiPlugin(options))
    }
}