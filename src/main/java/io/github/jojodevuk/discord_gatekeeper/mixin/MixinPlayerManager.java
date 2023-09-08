package io.github.jojodevuk.discord_gatekeeper.mixin;

import com.mojang.authlib.GameProfile;
import io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper;
import io.github.jojodevuk.discord_gatekeeper.managers.ChatMessageManager;
import io.github.jojodevuk.discord_gatekeeper.records.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.List;

import static io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper.*;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @Inject(method = "checkCanJoin", at = @At("HEAD"), cancellable = true)
    private void checkCanJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
        Player player = DiscordGatekeeper.PLAYER_MANAGER.getPlayerByUUID(profile.getId());
        DiscordGatekeeper.PLAYER_MANAGER.addCacheEntry(profile.getId(), profile.getName());

        if (player == null) {
            String code = DiscordGatekeeper.PLAYER_MANAGER.getLinkCode(profile.getId());
            cir.setReturnValue(Text.of(DiscordGatekeeper.CONFIG.notLinkedMessage + " Your code is " + code));
            DiscordGatekeeper.LOGGER.info("Disconnected " + profile.getName() + " with a code to link their discord account.");
            return;
        }

        Guild guild = DiscordGatekeeper.JDA.getGuildById(DiscordGatekeeper.CONFIG.discordServerId);
        try {
            Member member = guild.getMemberById(player.discordID());

            List<Role> memberRoles = member.getRoles();

//            boolean hasRole = Objects.equals(memberRoles.
//                    stream()
//                    .filter(role -> role.getId().equals("1232568145"))
//                    .findFirst()
//                    .map(ISnowflake::getId)
//                    .orElse(null), "1146820829674475670");

            boolean hasRole = memberRoles
                    .stream()
                    .anyMatch(role -> DiscordGatekeeper.CONFIG.allowedRoleIDs.contains(role.getId()));

            if (!hasRole) {
                cir.setReturnValue(Text.of(DiscordGatekeeper.CONFIG.noRoleMessage));
                DiscordGatekeeper.LOGGER.info("Disconnected " + profile.getName() + " because they don't have the role.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        try {
            Guild guild = JDA.getGuildById(CONFIG.discordServerId);
            TextChannel channel = guild.getTextChannelById(CONFIG.chatLinkChannelId);
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(0x6bf26b);
            embed.setTitle(player.getName().getString() + " has joined!");
            embed.setThumbnail(ChatMessageManager.getBustImage(player.getUuid()));
            channel.sendMessageEmbeds(embed.build()).queue();
        } catch (Exception e) {
            LOGGER.warn(ExceptionUtils.getStackTrace(e));
        }
    }

    @Inject(method = "remove", at = @At("RETURN"))
    private void onPlayerDisconnect(ServerPlayerEntity player, CallbackInfo ci) {
        try {
            Guild guild = JDA.getGuildById(CONFIG.discordServerId);
            TextChannel channel = guild.getTextChannelById(CONFIG.chatLinkChannelId);
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(0xff1f1f);
            embed.setTitle(player.getName().getString() + " has left");
            embed.setThumbnail(ChatMessageManager.getBustImage(player.getUuid()));
            channel.sendMessageEmbeds(embed.build()).queue();
        } catch (Exception e) {
            LOGGER.warn(ExceptionUtils.getStackTrace(e));
        }
    }
}