package io.andrewohara.utils.http4k

import org.http4k.contract.ContractRoutingHttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.*

object ContractUi {

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

    private fun swaggerUiHtml(pageTitle: String, specPath: String) = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <script src="//unpkg.com/swagger-ui-dist@3/swagger-ui-standalone-preset.js"></script>
    <!-- <script src="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/3.22.1/swagger-ui-standalone-preset.js"></script> -->
    <script src="//unpkg.com/swagger-ui-dist@3/swagger-ui-bundle.js"></script>
    <!-- <script src="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/3.22.1/swagger-ui-bundle.js"></script> -->
    <link rel="stylesheet" href="//unpkg.com/swagger-ui-dist@3/swagger-ui.css" />
    <!-- <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/3.22.1/swagger-ui.css" /> -->
    <title>$pageTitle</title>
</head>
<body>
    <div id="swagger-ui"></div>
    <script>
        window.onload = function() {
          SwaggerUIBundle({
            url: "$specPath",
            dom_id: '#swagger-ui',
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
    ): RoutingHttpHandler {
        return routes(
            "" bind Method.GET to {
                Response(Status.FOUND).header("Location", swaggerUiPath)
            },
            swaggerUiPath bind Method.GET to {
                Response(Status.OK).body(swaggerUiHtml(pageTitle, descriptionPath))
            },
            redocPath bind Method.GET to {
                Response(Status.OK).body(redocHtml(pageTitle, descriptionPath))
            },
            contract
        )
    }
}

