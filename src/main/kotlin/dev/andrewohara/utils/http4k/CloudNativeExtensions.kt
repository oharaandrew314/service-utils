package dev.andrewohara.utils.http4k

import org.http4k.cloudnative.env.EnvironmentKey
import java.nio.file.Path


fun EnvironmentKey.path() = map(Path::of, Path::toString)