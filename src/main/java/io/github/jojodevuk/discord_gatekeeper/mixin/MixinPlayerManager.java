package io.github.jojodevuk.discord_gatekeeper.mixin;

import com.mojang.authlib.GameProfile;
import io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper;
import io.github.jojodevuk.discord_gatekeeper.records.Player;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.List;


@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @Inject(method = "checkCanJoin", at = @At("HEAD"), cancellable = true)
    private void checkCanJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
        Player player = DiscordGatekeeper.PLAYER_MANAGER.getPlayerByUUID(profile.getId());
        DiscordGatekeeper.PLAYER_MANAGER.addCacheEntry(profile.getId(), profile.getName());

        if (player == null) {
            String code = DiscordGatekeeper.PLAYER_MANAGER.getLinkCode(profile.getId());
            cir.setReturnValue(Text.of(DiscordGatekeeper.config.notLinkedMessage() + " Your code is " + code));
            DiscordGatekeeper.LOGGER.info("Disconnected " + profile.getName() + " with a code to link their discord account.");
            return;
        }

        Guild guild = DiscordGatekeeper.jda.getGuildById(DiscordGatekeeper.config.serverId());
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
                    .anyMatch(role -> DiscordGatekeeper.config.roleId().contains(role.getId()));

            if (!hasRole) {
                cir.setReturnValue(Text.of(DiscordGatekeeper.config.noRoleMessage()));
                DiscordGatekeeper.LOGGER.info("Disconnected " + profile.getName() + " because they don't have the role.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}