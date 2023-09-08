package io.github.jojodevuk.discord_gatekeeper.mixin;

import io.github.jojodevuk.discord_gatekeeper.managers.ChatMessageManager;
import io.github.jojodevuk.discord_gatekeeper.records.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper.CONFIG;
import static io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper.JDA;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {
    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeath(DamageSource source, CallbackInfo ci){
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) (Object) this;
        String deathMessage = source.getDeathMessage(serverPlayerEntity).getString();

        Guild guild = JDA.getGuildById(CONFIG.discordServerId);
        TextChannel channel = guild.getTextChannelById(CONFIG.chatLinkChannelId);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(0xff0000);
        embed.setTitle(deathMessage);
        embed.setAuthor("", ChatMessageManager.getBustImage(serverPlayerEntity.getUuid()));
        embed.setThumbnail("https://i.pinimg.com/736x/18/b4/41/18b441b32d830273ef86587285df8b13.jpg");
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
