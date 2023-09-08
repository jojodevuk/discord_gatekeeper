package io.github.jojodevuk.discord_gatekeeper;

import com.google.common.collect.Sets;

import java.util.Set;

public class Config {
    public String botToken = "INSERT_BOT_TOKEN_HERE";
    public String discordServerId = "INSERT_SERVER_ID_HERE";
    public String chatLinkChannelId = "INSERT_CHANNEL_ID_HERE";
    public String webhookURL = "INSERT_WEBHOOK_URL_HERE";
    public Set<String> allowedRoleIDs = Sets.newHashSet("INSERT_ROLE_ID_HERE");
    public String noRoleMessage = "You don't have the required role.";
    public String notLinkedMessage = "You are not linked to a Discord account! Please link your account by joining the Discord server and typing /link <code>.";
}
