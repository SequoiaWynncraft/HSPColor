package net.warze.hspcolor.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("hspcolor.json");

    private ConfigManager() {
    }

    public static HspColorConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                HspColorConfig parsed = GSON.fromJson(reader, HspColorConfig.class);
                if (parsed != null) return parsed;
            } catch (Exception ignored) {
            }
        }
        return HspColorConfig.defaults();
    }

    public static void save(HspColorConfig config) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (Exception ignored) {
        }
    }
}
