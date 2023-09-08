package io.github.jojodevuk.discord_gatekeeper.managers;

import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.UUID;

public class ChatMessageManager {
    public static WebhookMessage makeWebhook(String message, ServerPlayerEntity sender) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();

        AllowedMentions mentions = new AllowedMentions()
                .withParseEveryone(false)
                .withParseRoles(false);

        builder.setAllowedMentions(mentions);
        builder.setUsername(sender.getEntityName()); // use this username
        builder.setAvatarUrl(getBustImage(sender.getUuid())); // use this avatar
        builder.setContent(message);

        return builder.build();
    }

    public static String getBustImage(UUID uuid) {
        return "https://minotar.net/armor/bust/" + uuid.toString().replace("-", "") + "/100.png";
    }

    public static String getBustImage(String name) {
        return "https://minotar.net/armor/bust/" + name + "/100.png";
    }

    public static Text colourText(String text, int rgb) {
        return Text.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }
}
