package com.near_reality.api.service.sanction

import kotlinx.serialization.json.*
import java.io.File

object BanList {
    private val file = File("data/banned.json")
    private var bans: MutableSet<String> = mutableSetOf()

    init {
        load()
    }

    fun load() {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.writeText("""{ "banned": [] }""")
        }
        val json = Json.parseToJsonElement(file.readText()).jsonObject
        val bannedArray = json["banned"]?.jsonArray ?: JsonArray(emptyList())
        bans = bannedArray.map { it.jsonPrimitive.content.lowercase() }.toMutableSet()
    }

    fun save() {
        val json = buildJsonObject {
            put("banned", JsonArray(bans.map { JsonPrimitive(it) }))
        }
        file.writeText(Json.encodeToString(JsonObject.serializer(), json))
    }

    fun addBan(name: String) {
        bans.add(name.lowercase())
        save()
    }

    fun removeBan(name: String) {
        bans.remove(name.lowercase())
        save()
    }

    fun isBanned(name: String): Boolean {
        return name.lowercase() in bans
    }
}
