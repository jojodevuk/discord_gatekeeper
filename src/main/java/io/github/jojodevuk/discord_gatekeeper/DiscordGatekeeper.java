package io.github.jojodevuk.discord_gatekeeper;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.jojodevuk.discord_gatekeeper.events.ReadyEventListener;
import io.github.jojodevuk.discord_gatekeeper.managers.PlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordGatekeeper implements DedicatedServerModInitializer {

	public static final String MOD_ID = "discord_gatekeeper";

    public static final Logger LOGGER = LoggerFactory.getLogger(DiscordGatekeeper.class);

	public static final PlayerManager PLAYER_MANAGER = new PlayerManager();

	public static ModConfigServer config;
	public static JDA jda;

	public static final Gson GSON = new GsonBuilder()
//			.setPrettyPrinting()
			.create();

	@Override
	public void onInitializeServer() {
		loadConfigDir();
		loadConfig();

		PLAYER_MANAGER.loadUUIDCache();
		PLAYER_MANAGER.loadLinkedPlayers();

		Pattern pattern = Pattern.compile("[\\w-]{26}\\.[\\w-]{6}\\.[\\w-]{38}");
		Matcher matcher = pattern.matcher(config.botToken());
		boolean matchFound = matcher.find();

		if (!matchFound) {
			LOGGER.error("You need to set your bot token in the config file!");
			return;
		}
		else {
			LOGGER.info("Bot token (probably) correct, starting up.");
		}

		JDABuilder jdaBuilder = JDABuilder.createDefault(config.botToken());
		jdaBuilder.addEventListeners(new ReadyEventListener());
		jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS);
		jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
		jdaBuilder.setChunkingFilter(ChunkingFilter.ALL);
		jdaBuilder.build();

//		CommandRegistrationCallback.EVENT.register(EssentialCommandRegistry::register);
	}

	void loadConfigDir() {
		File configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).toFile();
		if (!configDir.exists()) configDir.mkdir();
	}

	void loadConfig() {
		final File configFile = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).resolve(MOD_ID + "-server.json").toFile();
		if (configFile.exists()) {
			try (FileReader fileReader = new FileReader(configFile)) {
				config = GSON.fromJson(fileReader, ModConfigServer.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try (FileWriter fileWriter = new FileWriter(configFile)) {
				GSON.toJson(ModConfigServer.DEFAULT, fileWriter);
				config = ModConfigServer.DEFAULT;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public record ModConfigServer(
			String botToken,
			String serverId,
			Set<String> roleId,
			String noRoleMessage,
			String notLinkedMessage
	) {
		public static final ModConfigServer DEFAULT = new ModConfigServer(
				"INSERT_BOT_TOKEN_HERE",
				"INSERT_SERVER_ID_HERE",
				Sets.newHashSet("INSERT_ROLE_ID_HERE"),
				"You don't have the required role.",
				"You are not linked to a Discord account! Please link your account by joining the Discord server and typing /link <code>."
		);
	}

	public static File getConfigFile(String filePath) {
		return FabricLoader.getInstance().getConfigDir().resolve(DiscordGatekeeper.MOD_ID).resolve(filePath).toFile();
	}

	public static void writeToFile(File file, Object obj) {
		try (FileWriter fileWriter = new FileWriter(file)) {
			DiscordGatekeeper.GSON.toJson(obj, fileWriter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static <T> T readFromFile(File file, Class<T> clazz) {
		try (FileReader fileReader = new FileReader(file)) {
			return GSON.fromJson(fileReader, clazz);
		} catch (java.io.FileNotFoundException e) {
			LOGGER.info("Cannot read file " + file.getName() + " as it does not exist.");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> T readFromFile(File file, Type tipe) {
		try (FileReader fileReader = new FileReader(file)) {
			return GSON.fromJson(fileReader, tipe);
		} catch (java.io.FileNotFoundException e) {
			LOGGER.info("Cannot read file " + file.getName() + " as it does not exist.");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}