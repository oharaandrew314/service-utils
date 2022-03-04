package io.andrewohara.utils.http4k

import org.http4k.contract.ContractRoutingHttpHandler
import org.http4k.core.*
import org.http4k.lens.Header
import org.http4k.routing.*

object ContractUi {

    private const val swaggerUiVersion = "4.6.1"

    private fun redocHtml(pageTitle: String, specPath: String) = """
<!DOCTYPE html>
<html>
<head>
    <title>$pageTitle</title>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700" rel="stylesheet">
    
    <style>
      body {
        margin: 0;
        padding: 0;
      }
    </style>
</head>
<body>
<redoc spec-url='$specPath'></redoc>
<script src="https://cdn.jsdelivr.net/npm/redoc@latest/bundles/redoc.standalone.js"> </script>
</body>
</html>
"""

    private fun swaggerUiHtml(pageTitle: String, specPath: String, displayOperationId: Boolean, displayRequestDuration: Boolean, persistAuthorization: Boolean) = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <script src="//unpkg.com/swagger-ui-dist@3/swagger-ui-standalone-preset.js"></script>
    <!-- <script src="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/$swaggerUiVersion/swagger-ui-standalone-preset.js"></script> -->
    <script src="//unpkg.com/swagger-ui-dist@3/swagger-ui-bundle.js"></script>
    <!-- <script src="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/$swaggerUiVersion/swagger-ui-bundle.js"></script> -->
    <link rel="stylesheet" href="//unpkg.com/swagger-ui-dist@3/swagger-ui.css" />
    <!-- <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/$swaggerUiVersion/swagger-ui.css" /> -->
    <title>$pageTitle</title>
</head>
<body>
    <div id="swagger-ui"></div>
    <script>
        window.onload = function() {
          SwaggerUIBundle({
            url: "$specPath",
            dom_id: '#swagger-ui',
            deepLinking: true,
            displayOperationId: $displayOperationId,
            displayRequestDuration: $displayRequestDuration,
            requestSnippetsEnabled: true,
            persistAuthorization: $persistAuthorization,
            presets: [
              SwaggerUIBundle.presets.apis,
              SwaggerUIStandalonePreset
            ],
            layout: "StandaloneLayout"
          })
        }
    </script>
</body>
</html>
"""


    operator fun invoke(
        contract: ContractRoutingHttpHandler,
        descriptionPath: String,
        pageTitle: String,
        swaggerUiPath: String = "swagger",
        redocPath: String = "redoc",
        displayOperationId: Boolean = false,
        displayRequestDuration: Boolean = false,
        persistAuthorization: Boolean = false,
    ): RoutingHttpHandler {
        return routes(
            "" bind Method.GET to {
                Response(Status.FOUND).with(Header.LOCATION of Uri.of(swaggerUiPath))
            },
            swaggerUiPath bind Method.GET to {
                val body = swaggerUiHtml(
                    pageTitle,
                    descriptionPath,
                    displayOperationId = displayOperationId,
                    displayRequestDuration = displayRequestDuration,
                    persistAuthorization = persistAuthorization
                )
                Response(Status.OK)
                    .with(Header.CONTENT_TYPE of ContentType.TEXT_HTML)
                    .body(body)
            },
            redocPath bind Method.GET to {
                Response(Status.OK)
                    .with(Header.CONTENT_TYPE of ContentType.TEXT_HTML)
                    .body(redocHtml(pageTitle, descriptionPath))
            },
            contract
        )
    }
}

