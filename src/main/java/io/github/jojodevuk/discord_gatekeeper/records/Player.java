package io.github.jojodevuk.discord_gatekeeper.records;

import java.util.UUID;

/**
 * Record of a Minecraft UUID to discordID link.
 * @param uuid UUID of Minecraft account
 * @param discordID ID of discord account
 */
public record Player(UUID uuid, Long discordID) {

}