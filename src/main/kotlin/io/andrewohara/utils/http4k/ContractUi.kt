package io.andrewohara.utils.http4k

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.*

object ContractUi {

    operator fun invoke(
        descriptionPath: String,
        swaggerUiPath: String = "/",
        redocPath: String = "/redoc"
    ): RoutingHttpHandler = routes(
        "docs" bind Method.GET to {
            Response(Status.FOUND).header("Location", "docs/index.html?url=$descriptionPath")
        },
        "/docs" bind static(ResourceLoader.Classpath("META-INF/resources/webjars/swagger-ui/3.47.1")),

//        "redoc" bind Method.GET to {
//
//        },
        "/redoc" bind static(ResourceLoader.Classpath("META-INF/resources/webjars/redoc/2.0.0-rc.53")),
    )

//    fun redocHtml(title: String) = """
//    |<!DOCTYPE html>
//    |<html>
//    |  <head>
//    |    <title>$title</title>
//    |    <!-- Needed for adaptive design -->
//    |    <meta charset="utf-8"/>
//    |    <meta name="viewport" content="width=device-width, initial-scale=1">
//    |    <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700" rel="stylesheet">
//    |    <!-- ReDoc doesn't change outer page styles -->
//    |    <style>body{margin:0;padding:0;}</style>
//    |  </head>
//    |  <body>
//    |  <redoc id='redoc'></redoc>
//    |  <script src="$publicBasePath/bundles/redoc.standalone.js"></script>
//    |  <script>
//    |   window.onload = () => {
//    |     Redoc.init('$docsPath', ${options.json()}, document.getElementById('redoc'))
//    |   }
//    | </script>
//    |  </body>
//    |</html>
//    |""".trimMargin()
}