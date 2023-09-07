package io.github.jojodevuk.discord_gatekeeper.discordCommands;

import io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper;
import io.github.jojodevuk.discord_gatekeeper.records.Player;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.UUID;

public class UnlinkCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("unlink")) return;

        long userID = event.getUser().getIdLong();

        if (DiscordGatekeeper.PLAYER_MANAGER.isLinked(userID)) {
            Player linkedPlayer = DiscordGatekeeper.PLAYER_MANAGER.getPlayerByDiscordId(userID);
            UUID minecraftUUID = linkedPlayer.uuid();
            String minecraftName = DiscordGatekeeper.PLAYER_MANAGER.getNameFromUUID(minecraftUUID);
            String minecraftNameFormatted = minecraftName == null ? minecraftUUID.toString() : minecraftName + " (" + minecraftUUID.toString() + ")";

            boolean didUnlink = DiscordGatekeeper.PLAYER_MANAGER.unlinkPlayer(userID);

            if (didUnlink) {
                event.reply("You have unlinked your discord account from " + minecraftNameFormatted).setEphemeral(true).queue();
            } else {
                event.reply("Your discord account isn't linked to a minecraft account").setEphemeral(true).queue();
            }
        } else {
            event.reply("Your discord account isn't linked to a minecraft account").setEphemeral(true).queue();
        }
    }

}
