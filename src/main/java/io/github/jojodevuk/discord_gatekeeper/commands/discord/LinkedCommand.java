package io.github.jojodevuk.discord_gatekeeper.commands.discord;

import io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper;
import io.github.jojodevuk.discord_gatekeeper.records.Player;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.UUID;

public class LinkedCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("linked")) return;

        final OptionMapping userOption = event.getOption("user");

        boolean isAdmin = true;

        final User userToCheck = userOption != null && isAdmin ? userOption.getAsUser() : event.getUser();

        Player linkedPlayer = DiscordGatekeeper.PLAYER_MANAGER.getPlayerByDiscordId(userToCheck.getIdLong());
        if (linkedPlayer == null) {
            event.reply( userToCheck.getName() + " isn't linked to a minecraft account").setEphemeral(false).queue();
        } else {
            UUID minecraftUUID = linkedPlayer.uuid();
            String minecraftUsername = DiscordGatekeeper.PLAYER_MANAGER.getNameFromUUID(minecraftUUID);
            String minecraftNameFormatted = minecraftUsername == null ? minecraftUUID.toString() : minecraftUsername + " (" + minecraftUUID.toString() + ")";
            event.reply( userToCheck.getName() + " is linked to " + minecraftNameFormatted).setEphemeral(false).queue();
        }

    }
}
