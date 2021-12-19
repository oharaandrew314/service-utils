package io.andrewohara.utils.jdk

import java.time.Clock
import java.time.Duration
import java.time.Instant

operator fun Clock.plus(duration: Duration): Instant = instant() + duration
operator fun Clock.minus(duration: Duration): Instant = instant() - duration