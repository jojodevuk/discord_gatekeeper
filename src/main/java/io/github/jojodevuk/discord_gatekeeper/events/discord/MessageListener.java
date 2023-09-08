package io.github.jojodevuk.discord_gatekeeper.events.discord;

import io.github.jojodevuk.discord_gatekeeper.Config;
import io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper;
import io.github.jojodevuk.discord_gatekeeper.managers.ChatMessageManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User user = event.getAuthor();
        Message msg = event.getMessage();

        if (!msg.getChannel().getId().equals(DiscordGatekeeper.CONFIG.chatLinkChannelId)) return;
        if (user.isBot()) return;

        int memberColour;
        try { memberColour = event.getMember().getColorRaw(); }
        catch (Exception e) { memberColour = 0x000000; }

        String content = msg.getContentDisplay();
        Text contentRedacted = Text.literal(content.replaceAll("(https://.*?\\s|https://.*)", "[URL REDACTED] "));

        Text formattedMessage = Text.literal("")
                .append(ChatMessageManager.colourText("[Discord] ", 0x5865F2))
                .append(ChatMessageManager.colourText(user.getName(), memberColour))
                .append(": ").append(contentRedacted);

        MinecraftServer server = DiscordGatekeeper.getServer();
        server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {serverPlayerEntity.sendMessage(formattedMessage);});
    }
}
