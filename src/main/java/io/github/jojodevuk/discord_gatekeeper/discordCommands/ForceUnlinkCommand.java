package io.github.jojodevuk.discord_gatekeeper.discordCommands;

import io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper;
import io.github.jojodevuk.discord_gatekeeper.classes.Player;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.UUID;

public class ForceUnlinkCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("force_unlink")) return;

        final OptionMapping userOption = event.getOption("user");
        if (userOption == null) return;
        final User userToUnlink = userOption.getAsUser();

        Player linkedPlayer = DiscordGatekeeper.PLAYER_MANAGER.getPlayerByDiscordId(userToUnlink.getIdLong());


        if (DiscordGatekeeper.PLAYER_MANAGER.isLinked(userToUnlink.getIdLong())) {
            UUID minecraftUUID = linkedPlayer.uuid();
            String minecraftName = DiscordGatekeeper.PLAYER_MANAGER.getNameFromUUID(minecraftUUID);
            String minecraftNameFormatted = minecraftName == null ? minecraftUUID.toString() : minecraftName + " (" + minecraftUUID.toString() + ")";

            boolean didUnlink = DiscordGatekeeper.PLAYER_MANAGER.unlinkPlayer(userToUnlink.getIdLong());

            if (didUnlink) {
                event.reply("Successfully unlinked " + minecraftNameFormatted + " from " + userToUnlink.getName()).setEphemeral(true).queue();
            } else {
                event.reply(userToUnlink.getName() + " isn't linked to a minecraft account").setEphemeral(true).queue();
            }
        } else {
            event.reply(userToUnlink.getName() + " isn't linked to a minecraft account").setEphemeral(true).queue();
        }
    }

}
