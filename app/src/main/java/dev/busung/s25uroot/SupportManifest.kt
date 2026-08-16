package dev.busung.s25uroot

import org.json.JSONArray
import org.json.JSONObject

data class RemoteArtifact(
    val url: String,
    val size: Long,
)

data class TargetProfile(
    val profileId: String,
    val displayName: String,
    val models: Set<String>,
    val kernelVersions: Set<String>,
    val exploit: RemoteArtifact,
    val kernelSu: RemoteArtifact,
) {
    init {
        require(models.isNotEmpty()) { "Payload must support at least one model" }
        require(kernelVersions.isNotEmpty()) { "Payload must support at least one kernel version" }
    }

    fun matchesDevice(snapshot: DeviceSnapshot): Boolean =
        models.any { it.equals(snapshot.model, ignoreCase = true) }

    fun matchesKernelVersion(snapshot: DeviceSnapshot): Boolean =
        snapshot.kernelVersion in kernelVersions

    fun matches(snapshot: DeviceSnapshot): Boolean {
        if (!matchesDevice(snapshot)) return false
        if (profileId == DEBUG_SAMPLE_PROFILE_ID && snapshot.model.startsWith("SM-A536", ignoreCase = true)) {
            return true
        }
        return matchesKernelVersion(snapshot)
    }

    val supportedModels: String
        get() = models.joinToString()

    val supportedKernelVersions: String
        get() = kernelVersions.joinToString()

    companion object {
        const val DEBUG_SAMPLE_PROFILE_ID = "sample-a53"

        fun debugSampleProfile(): TargetProfile = TargetProfile(
            profileId = DEBUG_SAMPLE_PROFILE_ID,
            displayName = "Samsung A536B (debug sample)",
            models = setOf("SM-A536B", "SM-A536B/DS", "SM-A536B2", "SM-A536E"),
            kernelVersions = setOf("5.4.0", "5.4.110", "5.4.117", "5.4.119", "5.4.141"),
            exploit = RemoteArtifact("https://example.invalid/sample-a53-exploit", 1),
            kernelSu = RemoteArtifact("https://example.invalid/sample-a53-kernelsu", 1),
        )
    }
}

data class SupportManifest(
    val schemaVersion: Int,
    val targets: List<TargetProfile>,
) {
    companion object {
        fun parse(bytes: ByteArray): SupportManifest {
            val root = JSONObject(bytes.toString(Charsets.UTF_8))
            val schemaVersion = root.getInt("schemaVersion")
            require(schemaVersion == 3) { "Unsupported support manifest schema" }
            val payloadsJson = root.getJSONArray("payloads")
            val payloads = buildList {
                for (index in 0 until payloadsJson.length()) {
                    val payload = payloadsJson.getJSONObject(index)
                    val exploit = payload.getJSONObject("exploit")
                    val kernelSu = payload.getJSONObject("kernelsu")
                    add(
                        TargetProfile(
                            profileId = payload.getString("payloadId"),
                            displayName = payload.getString("displayName"),
                            models = payload.getJSONArray("models").strings(),
                            kernelVersions = payload.getJSONArray("kernelVersions").strings(),
                            exploit = RemoteArtifact(
                                url = exploit.getString("url"),
                                size = exploit.getLong("size"),
                            ),
                            kernelSu = RemoteArtifact(
                                url = kernelSu.getString("url"),
                                size = kernelSu.getLong("size"),
                            ),
                        ),
                    )
                }
            }
            return SupportManifest(schemaVersion, payloads)
        }

        private fun JSONArray.strings(): Set<String> = buildSet {
            for (index in 0 until length()) add(getString(index))
        }
    }
}
