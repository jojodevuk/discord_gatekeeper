package io.github.jojodevuk.discord_gatekeeper;

import club.minnced.discord.webhook.WebhookClient;
import io.github.jojodevuk.discord_gatekeeper.events.discord.MessageListener;
import io.github.jojodevuk.discord_gatekeeper.events.discord.ReadyEventListener;
import io.github.jojodevuk.discord_gatekeeper.managers.ConfigManager;
import io.github.jojodevuk.discord_gatekeeper.managers.PlayerManager;
import kotlin.Suppress;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.jojodevuk.discord_gatekeeper.managers.ChatMessageManager.makeWebhook;

public class DiscordGatekeeper implements DedicatedServerModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(DiscordGatekeeper.class);

	public static final String MOD_ID = "discord_gatekeeper";
	public static final String VERSION = FabricLoader.getInstance().getModContainer("discord_gatekeeper").orElseThrow().getMetadata().getVersion().getFriendlyString();

	public static final File CONFIG_DIR	= FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).toFile();
	public static final File CONFIG_FILE = new File(CONFIG_DIR, "discord_gatekeeper.json");
	public static Config CONFIG;
	public static final PlayerManager PLAYER_MANAGER = new PlayerManager();

	public static JDA JDA;
	public static WebhookClient WEBHOOK_CLIENT;

	@Override
	public void onInitializeServer() {
		try {
			ConfigManager.init(false);
		} catch (Exception ignored) {}

		PLAYER_MANAGER.loadUUIDCache();
		PLAYER_MANAGER.loadLinkedPlayers();

		Pattern pattern = Pattern.compile("[\\w-]{26}\\.[\\w-]{6}\\.[\\w-]{38}");
		Matcher matcher = pattern.matcher(CONFIG.botToken);
		boolean matchFound = matcher.find();

		if (!matchFound) {
			LOGGER.error("You need to set your bot token in the config file!");
			return;
		}
		else {
			LOGGER.info("Bot token (probably) correct, starting up.");
		}

		if (CONFIG.webhookURL == null) LOGGER.error("Webhook URL not specified, chat mirroring disabled.");
		else {
			WEBHOOK_CLIENT = WebhookClient.withUrl(CONFIG.webhookURL);
		}

		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> WEBHOOK_CLIENT.send(makeWebhook(message.getContent().getString(), sender)));

		JDABuilder jdaBuilder = JDABuilder.createDefault(CONFIG.botToken);
		jdaBuilder.addEventListeners(new ReadyEventListener());
		jdaBuilder.addEventListeners(new MessageListener());
		jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT);
		jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
		jdaBuilder.setChunkingFilter(ChunkingFilter.ALL);
		jdaBuilder.build();
	}

	public static MinecraftServer getServer(){
		@SuppressWarnings("deprecation")
		Object gameInstance = FabricLoader.getInstance().getGameInstance();
		if (gameInstance instanceof MinecraftServer) {
			return (MinecraftServer) gameInstance;
		} else {
			return null;
		}
	}
}