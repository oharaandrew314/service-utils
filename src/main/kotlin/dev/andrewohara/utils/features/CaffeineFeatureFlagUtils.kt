package dev.andrewohara.utils.features

import java.time.Duration
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache

fun FeatureFlag.cached(cache: LoadingCache<String, String>) = FeatureFlag(cache::get)

fun FeatureFlag.cached(duration: Duration): FeatureFlag {
    val cache = Caffeine.newBuilder()
        .expireAfterWrite(duration)
        .build<String, String>(::invoke)

    return cached(cache)
}