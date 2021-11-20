package io.andrewohara.utils.config

import java.net.URL

fun ConfigLoader.Companion.java8(baseUrl: String) = ConfigLoader { name ->
    val url = URL("${baseUrl.trimEnd('/')}/${name.trimEnd('/')}")
    url.openStream().readBytes()
}