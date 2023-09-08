package io.github.jojodevuk.discord_gatekeeper.events.discord;

import io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper;
import io.github.jojodevuk.discord_gatekeeper.commands.discord.ForceUnlinkCommand;
import io.github.jojodevuk.discord_gatekeeper.commands.discord.LinkCommand;
import io.github.jojodevuk.discord_gatekeeper.commands.discord.LinkedCommand;
import io.github.jojodevuk.discord_gatekeeper.commands.discord.UnlinkCommand;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class ReadyEventListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        DiscordGatekeeper.JDA = event.getJDA();
        DiscordGatekeeper.LOGGER.info("Bot is ready!");

        event.getJDA().getGuilds().forEach(guild -> guild.updateCommands().addCommands(
                Commands.slash("link", "Link your Minecraft account to your Discord account.")
                        .addOption(OptionType.STRING, "code", "The code you received when you joined the Minecraft server.", true),

                Commands.slash("unlink", "Unlink your Minecraft account from your Discord account."),

                Commands.slash("force_unlink", "Forces an unlink. Admin only.")
                        .addOption(OptionType.USER, "user", "Discord user to unlink minecraft account for.", true),

                Commands.slash("linked", "Checks which minecraft account your account is linked to.")
                        .addOption(OptionType.USER, "user", "Another discord account to check linked status, admin only.")
        ).queue());

        event.getJDA().addEventListener(new LinkCommand(), new UnlinkCommand(), new ForceUnlinkCommand(), new LinkedCommand());
    }

}
