package io.andrewohara.utils.features

import io.split.client.SplitClient
import io.split.client.api.Key
import io.split.client.api.SplitResult

class FakeSplitClient(private val treatments: Map<String, String>, private val defaultTreatment: String = "off"): SplitClient {

    override fun getTreatment(key: String, split: String) = treatments[split] ?: defaultTreatment
    override fun getTreatment(key: String, split: String, attributes: MutableMap<String, Any>) = getTreatment(key, split)
    override fun getTreatment(key: Key, split: String, attributes: MutableMap<String, Any>) = defaultTreatment

    override fun getTreatmentWithConfig(key: String, split: String) = SplitResult(getTreatment(key, split), null)
    override fun getTreatmentWithConfig(key: String, split: String, attributes: MutableMap<String, Any>) = getTreatmentWithConfig(key, split)
    override fun getTreatmentWithConfig(key: Key, split: String, attributes: MutableMap<String, Any>) = SplitResult(defaultTreatment, null)

    override fun track(key: String, trafficType: String, eventType: String) = false
    override fun track(key: String, trafficType: String, eventType: String, value: Double) = false
    override fun track(key: String, trafficType: String, eventType: String, properties: MutableMap<String, Any>) = false
    override fun track(key: String, trafficType: String, eventType: String, value: Double, properties: MutableMap<String, Any>) = false

    override fun blockUntilReady() {}
    override fun destroy() {}
}