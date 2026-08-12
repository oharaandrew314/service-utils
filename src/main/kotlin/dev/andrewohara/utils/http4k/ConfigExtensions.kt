package dev.andrewohara.utils.http4k

import org.http4k.config.EnvironmentKey
import java.nio.file.Path


fun EnvironmentKey.path() = map(Path::of, Path::toString)