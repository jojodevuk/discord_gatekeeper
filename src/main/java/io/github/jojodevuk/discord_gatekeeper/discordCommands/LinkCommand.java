package io.github.jojodevuk.discord_gatekeeper.discordCommands;

import io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper;
import io.github.jojodevuk.discord_gatekeeper.classes.Player;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.UUID;

public class LinkCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("link")) return;

        final OptionMapping codeOption = event.getOption("code");
        if (codeOption == null) return;
        final String code = codeOption.getAsString();
        final long discordID = event.getUser().getIdLong();

        if (DiscordGatekeeper.PLAYER_MANAGER.isLinked(discordID)) {
            event.reply("You are already linked!\nUse `/unlink` to unlink your account.").setEphemeral(true).queue();
            return;
        }

        UUID minecraftUUID = DiscordGatekeeper.PLAYER_MANAGER.getUUIDFromLinkCode(code);

        if (minecraftUUID == null) {
            event.reply("Looks like that code is invalid, it should look like `123456`.\nConnect to the server to generate a code.").setEphemeral(true).queue();
            return;
        }

        String minecraftName = DiscordGatekeeper.PLAYER_MANAGER.getNameFromUUID(minecraftUUID);

        Player linkedPlayer = DiscordGatekeeper.PLAYER_MANAGER.linkPlayer(code, discordID);



        String minecraftNameFormatted = minecraftName == null ? minecraftUUID.toString() : minecraftName + " (" + minecraftUUID.toString() + ")";

        if (linkedPlayer != null) { event.reply("You have linked your account with " + minecraftNameFormatted).setEphemeral(false).queue(); }
        else { event.reply("Something went wrong when linking your account, please contact a mod.").setEphemeral(true).queue(); }
    }

}
