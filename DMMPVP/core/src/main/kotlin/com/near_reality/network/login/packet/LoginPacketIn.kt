package com.near_reality.network.login.packet

import com.near_reality.api.service.sanction.BanList
import com.near_reality.net.HardwareInfo
import com.near_reality.network.login.AuthenticatorInfo
import com.near_reality.network.login.ClientSettings
import com.near_reality.network.login.LoginCiphers
import com.zenyte.CacheManager
import com.zenyte.game.GameConstants
import com.zenyte.game.world.World
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.entity.player.PlayerInformation
import com.zenyte.game.world.entity.player.login.LoginManager
import com.zenyte.net.ClientResponse
import com.zenyte.net.NetworkConstants
import com.zenyte.net.PacketIn
import com.zenyte.net.login.LoginType
import com.zenyte.utils.TextUtils
import com.zenyte.utils.TimeUnit
import kotlinx.serialization.json.*
import java.io.File

data class LoginPacketIn(
    val type: LoginType,
    val version: Int,
    val subVersion: Int,
    val username: String,
    val password: String,
    val clientSettings: ClientSettings,
    val uniqueId: ByteArray,
    val hardwareInfo: HardwareInfo,
    val authInfo: AuthenticatorInfo?,
    val crcs: IntArray,
    val sessionToken: String,
    val previousXTEAKeys: IntArray,
    val ciphers: LoginCiphers,
) : PacketIn {

    fun getResponse(): ClientResponse? {

        if (!GameConstants.WORLD_PROFILE.isBeta() && !GameConstants.WORLD_PROFILE.isDevelopment()) {
            for (i in 0..crcs.lastIndex) {
                if (i == 16) continue // Index 16 is never updated so ignore it entirely.
                val crc = crcs[i]
                if (crc != 0 && crc != CacheManager.getCRC(i))
                    return ClientResponse.SERVER_UPDATED
            }
        }

        if (NetworkConstants.SESSION_TOKEN != sessionToken)
            return ClientResponse.BAD_SESSION_ID

        if (World.getPlayers().size >= NetworkConstants.PLAYER_CAP)
            return ClientResponse.WORLD_FULL

        if (World.isUpdating() && World.getUpdateTimer() < TimeUnit.MINUTES.toTicks(1))
            return ClientResponse.UPDATE_IN_PROGRESS

        val formattedUsername = TextUtils.formatNameForProtocol(username)
        if (formattedUsername.isEmpty() || formattedUsername.length > 12 || !TextUtils.isValidName(formattedUsername))
            return ClientResponse.INVALID_USERNAME_OR_PASSWORD
// Ban check
        if (BanList.isBanned(formattedUsername)) {
            return ClientResponse.BANNED
        }

        // Check the plain password
        if (!isPasswordValid(formattedUsername, password)) {
            return ClientResponse.INVALID_USERNAME_OR_PASSWORD
        }

        return null
    }

    /**
     * Validates if the username and password match based on data stored in data/characters.
     */
    private fun isPasswordValid(username: String, plainPassword: String): Boolean {
        println("Current working directory: ${System.getProperty("user.dir")}")

        val charactersDir = File("data/characters")
        if (!charactersDir.exists() || !charactersDir.isDirectory) {
            println("Character data directory does not exist or is invalid: ${charactersDir.absolutePath}")
            return false
        }

        val playerFile = File(charactersDir, "$username.json") // Assuming player files are named username.json
        if (!playerFile.exists()) {
            println("Player file does not exist for username: $username at path: ${playerFile.absolutePath}")

            // Player does not exist, create a new account
            val playerInformation = PlayerInformation(username, plainPassword, 0, null, null)
            val player = Player(playerInformation, null)

            // Set default values for the new player
            player.setDefaultSettings()

            // **Set the plain password correctly.**
            player.setPlainPassword(plainPassword)

            // **Serialize the player to file** with the correct password.
            LoginManager.serializePlayerToFile(player)

            // Log player in after creation
            println("New account created for username: $username")
            return true
        }

        try {
            val fileContent = playerFile.readText()

            val storedPlainPassword = extractPassword(fileContent)

            // Debug log the password values
            println("Stored password: $storedPlainPassword")
            println("Provided password: $plainPassword")

            if (storedPlainPassword == plainPassword) {
                println("Password validation successful for username: $username")
                return true
            } else {
                println("Invalid password for username: $username")
                return false
            }
        } catch (e: Exception) {
            println("Error reading or parsing player file for username $username: ${e.message}")
            return false
        }
    }


    // Assuming this function is used to extract the plainPassword from the JSON content
    private fun extractPassword(fileContent: String): String? {
        val regex = """"plainPassword":\s*"(.*?)"""".toRegex()
        val matchResult = regex.find(fileContent)
        return matchResult?.groups?.get(1)?.value
    }
}