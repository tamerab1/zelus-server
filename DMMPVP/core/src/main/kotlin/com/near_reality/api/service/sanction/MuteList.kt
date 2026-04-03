package com.near_reality.api.service.sanction

import kotlinx.serialization.json.*
import java.io.File

object MuteList {
    private val file = File("data/muted.json")
    private var mutes: MutableSet<String> = mutableSetOf()

    init {
        load()
    }

    fun load() {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.writeText("""{ "muted": [] }""")
        }
        val json = Json.parseToJsonElement(file.readText()).jsonObject
        val mutedArray = json["muted"]?.jsonArray ?: JsonArray(emptyList())
        mutes = mutedArray.map { it.jsonPrimitive.content.lowercase() }.toMutableSet()
    }

    fun save() {
        val json = buildJsonObject {
            put("muted", JsonArray(mutes.map { JsonPrimitive(it) }))
        }
        file.writeText(Json.encodeToString(JsonObject.serializer(), json))
    }

    fun addMute(name: String) {
        mutes.add(name.lowercase())
        save()
    }

    fun removeMute(name: String) {
        mutes.remove(name.lowercase())
        save()
    }

    fun isMuted(name: String): Boolean {
        return name.lowercase() in mutes
    }
}
