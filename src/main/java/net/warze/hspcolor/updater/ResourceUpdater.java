package net.warze.hspcolor.updater;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import ooo.sequoia.http.HttpClients;
import ooo.sequoia.http.clients.UpdateApiHttpClient;
import net.warze.hspcolor.config.HspColorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger("hspcolor");
    private static final String BASE_URL = "https://u.warze.org/w/";
    private static final String PACK_NAME = "hspcolor_latest.zip";
    private static final String PACK_URL = BASE_URL + "hspcolor.zip";
    private static final String PACK_ENTRY = "file/" + PACK_NAME;
    private static final String VERSION_URL = BASE_URL + "hspcolorlatest.txt";
    private static final String MOD_PREFIX = "hspcolorv";
    private static final String MOD_SUFFIX = ".jar";

    private final HspColorConfig config;
    private final UpdateApiHttpClient http;

    public ResourceUpdater(HspColorConfig config) {
        this(config, HttpClients.UPDATE_API);
    }

    ResourceUpdater(HspColorConfig config, UpdateApiHttpClient http) {
        this.config = config;
        this.http = http;
    }

    public void start() {
        CompletableFuture.runAsync(this::run, Util.getMainWorkerExecutor());
    }

    private void run() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.runDirectory == null) return;
        Path runDir = client.runDirectory.toPath();
        if (config.resourcePackUpdates) {
            try {
                boolean packDownloaded = downloadResourcePack(runDir);
                if (packDownloaded) ensurePackEnabled(runDir);
            } catch (Exception exception) {
                LOGGER.warn("Failed to update resource pack", exception);
            }
        }
        if (config.modUpdates) {
            try {
                updateModJar(runDir);
            } catch (Exception exception) {
                LOGGER.warn("Failed to update mod jar", exception);
            }
        }
    }

    private boolean downloadResourcePack(Path runDir) throws IOException {
        byte[] data = http.getBinary(PACK_URL);
        if (data == null || data.length == 0) return false;
        Path resourcePacks = runDir.resolve("resourcepacks");
        Files.createDirectories(resourcePacks);
        Path target = resourcePacks.resolve(PACK_NAME);
        Files.write(target, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        LOGGER.info("Downloaded {}", target.getFileName());
        return true;
    }

    private void ensurePackEnabled(Path runDir) throws IOException {
        Path optionsFile = runDir.resolve("options.txt");
        String entry = "resourcePacks:[\"" + PACK_ENTRY + "\"]";
        String needle = "\"" + PACK_ENTRY + "\"";
        if (!Files.exists(optionsFile)) {
            Files.writeString(optionsFile, entry + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            return;
        }
        List<String> lines = Files.readAllLines(optionsFile, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.startsWith("resourcePacks:[")) continue;
            if (line.contains(needle)) return;
            int closeIndex = line.lastIndexOf(']');
            if (closeIndex < 0) continue;
            String prefix = line.substring(0, closeIndex);
            if (prefix.charAt(prefix.length() - 1) != '[') prefix += ",";
            lines.set(i, prefix + needle + "]");
            Files.write(optionsFile, lines, StandardCharsets.UTF_8);
            return;
        }
        lines.add(entry);
        Files.write(optionsFile, lines, StandardCharsets.UTF_8);
    }

    private void updateModJar(Path runDir) throws IOException {
        String version = fetchRemoteVersion();
        if (version == null || version.isEmpty()) return;
        Path mods = runDir.resolve("mods");
        Files.createDirectories(mods);
        Path target = mods.resolve(MOD_PREFIX + version + MOD_SUFFIX);
        if (Files.notExists(target)) {
            byte[] jar = http.getBinary(BASE_URL + MOD_PREFIX + version + MOD_SUFFIX);
            if (jar == null || jar.length == 0) return;
            Files.write(target, jar, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            LOGGER.info("Downloaded {}", target.getFileName());
        }
        removeLegacyMods(mods, version);
    }

    private String fetchRemoteVersion() {
        byte[] data = http.getBinary(VERSION_URL);
        if (data == null || data.length == 0) return null;
        return new String(data, StandardCharsets.UTF_8).trim();
    }

    private void removeLegacyMods(Path mods, String version) throws IOException {
        try (Stream<Path> stream = Files.list(mods)) {
            stream.filter(Files::isRegularFile)
                .filter(path -> {
                    String name = path.getFileName().toString();
                    return name.startsWith(MOD_PREFIX) && name.endsWith(MOD_SUFFIX);
                })
                .forEach(path -> {
                    String other = extractVersion(path.getFileName().toString());
                    if (other == null || other.equals(version)) return;
                    if (compareVersions(other, version) >= 0) return;
                    try {
                        Files.delete(path);
                        LOGGER.info("Deleted {}", path.getFileName());
                    } catch (IOException exception) {
                        LOGGER.warn("Failed to delete {}", path.getFileName(), exception);
                    }
                });
        }
    }

    private String extractVersion(String name) {
        if (!name.startsWith(MOD_PREFIX) || !name.endsWith(MOD_SUFFIX)) return null;
        return name.substring(MOD_PREFIX.length(), name.length() - MOD_SUFFIX.length());
    }

    private int compareVersions(String a, String b) {
        String[] left = a.split("\\.");
        String[] right = b.split("\\.");
        int length = Math.max(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int l = i < left.length ? Integer.parseInt(left[i]) : 0;
            int r = i < right.length ? Integer.parseInt(right[i]) : 0;
            if (l != r) return Integer.compare(l, r);
        }
        return 0;
    }
}
