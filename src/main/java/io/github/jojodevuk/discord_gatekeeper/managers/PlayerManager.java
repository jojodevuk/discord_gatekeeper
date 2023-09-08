package io.github.jojodevuk.discord_gatekeeper.managers;

import com.google.gson.reflect.TypeToken;
import io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper;
import io.github.jojodevuk.discord_gatekeeper.records.LinkCode;
import io.github.jojodevuk.discord_gatekeeper.records.Player;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerManager {
    // A single store
    private Set<Player> linkedPlayers = new HashSet<>();
    public Map<UUID, String> UUIDNameCache = new HashMap<>();
    private Set<LinkCode> linkCodes = new HashSet<>();

    /**
     * Generates a random link code.
     *
     * @return  The generated random link code.
     */
    private static String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(new Random().nextInt(0, 10));
        }
        return sb.toString();
    }

    public UUID getUUIDFromLinkCode(String code) {
        return linkCodes.isEmpty() ? null : linkCodes
                .stream()
                .filter(linkCode -> linkCode.linkCode().equals(code))
                .findFirst()
                .map(LinkCode::uuid)
                .orElse(null);
    }

    public String getLinkCodeFromUUID(UUID uuid) {
        return linkCodes.isEmpty() ? null : linkCodes
                .stream()
                .filter(linkCode -> linkCode.uuid().equals(uuid))
                .findFirst()
                .map(LinkCode::linkCode)
                .orElse(null);
    }

    private LinkCode getLinkCodeObject(String code) {
        return linkCodes.isEmpty() ? null : linkCodes
                .stream()
                .filter(linkCode -> linkCode.linkCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    private String generateLinkCode(UUID uuid) {
        String code = generateRandomCode();
        Set<String> tempLinkCodes = linkCodes // Make set of all used codes to avoid lambda expression
                .stream()
                .map(LinkCode::linkCode)
                .collect(Collectors.toSet());

        while (tempLinkCodes.contains(code)) { code = generateRandomCode(); }
        linkCodes.add(new LinkCode(code, uuid));
        return code;
    }

    public String getLinkCode(UUID uuid) {
        String linkCode = getLinkCodeFromUUID(uuid); // tries to find an already existing link code
        if (linkCode == null) {
            linkCode = generateLinkCode(uuid); // if no link code generated for this uuid, generate new one
        }
        return linkCode;
    }

    private void saveUUIDCache() {
        File uuidUsernameCache = ConfigManager.getFile("uuidUsernameCache.json");
        ConfigManager.writeToFile(uuidUsernameCache, UUIDNameCache);
    }

    @SuppressWarnings("unchecked")
    public void loadUUIDCache() {
        File uuidUsernameCache = ConfigManager.getFile("uuidUsernameCache.json");
        Type type = new TypeToken<Map<UUID, String>>(){}.getType();
        Map<UUID, String> loadedData = ConfigManager.readFromFile(uuidUsernameCache, type);
        if (loadedData != null) { UUIDNameCache = loadedData; }
    }

    public String getNameFromUUID(UUID uuid) {
        return UUIDNameCache.get(uuid);
    }

    public void addCacheEntry(UUID uuid, String username) {
        String oldName = UUIDNameCache.put(uuid, username);
        if (!Objects.equals(oldName, username)) {
            saveUUIDCache();
            DiscordGatekeeper.LOGGER.debug("Saving uuid cache to json");
        }
    }

    public void saveLinkedPlayers() {
        File linkedUsers = ConfigManager.getFile("linkedUsers.json");
        ConfigManager.writeToFile(linkedUsers, linkedPlayers);
    }

    public void loadLinkedPlayers() {
        File linkedUsers = ConfigManager.getFile("linkedUsers.json");
        Type type = new TypeToken<Set<Player>>(){}.getType();
        Set<Player> loadedData = ConfigManager.readFromFile(linkedUsers, type);
        if (loadedData != null) { linkedPlayers = loadedData; }
    }

    /**
     * Fetch a player link object by Minecraft UUID
     * @param uuid Minecraft UUID of the player you want to fetch the link status of
     * @return Player object
     */
    public Player getPlayerByUUID(UUID uuid) {
        return linkedPlayers.isEmpty() ? null : linkedPlayers
                .stream()
                .filter(player -> player.uuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public Player getPlayerByDiscordId(long discordId) {
        return linkedPlayers.isEmpty() ? null : linkedPlayers
                .stream()
                .filter(player -> player.discordID().equals(discordId))
                .findFirst()
                .orElse(null);
    }

    public boolean isLinked(UUID uuid) {
        return getPlayerByUUID(uuid) != null;
    }

    public boolean isLinked(long discordID) {
        return getPlayerByDiscordId(discordID) != null;
    }

    public Player linkPlayer(String linkCode, Long discordID) {
        UUID uuid = getUUIDFromLinkCode(linkCode);
        Player player = new Player(uuid, discordID);
        linkedPlayers.add(player);
        linkCodes.remove(getLinkCodeObject(linkCode));
        saveLinkedPlayers();
        return player;
    }

    public boolean unlinkPlayer(UUID uuid) {
        boolean removedPlayer = linkedPlayers.remove(getPlayerByUUID(uuid));
        saveLinkedPlayers();

        return removedPlayer;
    }

    public boolean unlinkPlayer(long discordID) {
        boolean removedPlayer = linkedPlayers.remove(getPlayerByDiscordId(discordID));
        saveLinkedPlayers();

        return removedPlayer;
    }

}
