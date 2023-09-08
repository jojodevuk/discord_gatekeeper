package io.github.jojodevuk.discord_gatekeeper.managers;

import com.google.gson.GsonBuilder;
import io.github.jojodevuk.discord_gatekeeper.Config;
import io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;

import static io.github.jojodevuk.discord_gatekeeper.DiscordGatekeeper.*;

public class ConfigManager {
    public static void init(boolean throwException) throws Exception {
        try {
            if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdir();
            if (!CONFIG_FILE.exists()) create();
            else load();
        } catch (Exception e) {
            if (throwException) { throw e; }
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private static void load() throws Exception {
        try (FileReader fileReader = new FileReader(CONFIG_FILE)) {
            CONFIG = new GsonBuilder()
                    .create()
                    .fromJson(fileReader, Config.class);
        }
    }

    private static void create() throws Exception {
        try (FileWriter fileWriter = new FileWriter(CONFIG_FILE)) {
            CONFIG = new Config();
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(CONFIG, fileWriter);
        }
    }

    public static File getFile(String filePath) {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).resolve(filePath).toFile();
    }

    public static void writeToFile(File file, Object obj) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(obj, fileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T readFromFile(File file, Class<T> clazz) {
        try (FileReader fileReader = new FileReader(file)) {
            return new GsonBuilder()
                    .setPrettyPrinting()
                    .create().fromJson(fileReader, clazz);
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
            return new GsonBuilder()
                    .setPrettyPrinting()
                    .create().fromJson(fileReader, tipe);
        } catch (java.io.FileNotFoundException e) {
            LOGGER.info("Cannot read file " + file.getName() + " as it does not exist.");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
