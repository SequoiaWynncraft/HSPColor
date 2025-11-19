package net.warze.hspcolor.utils;

import net.minecraft.client.Minecraft;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Warze
 */
public class ModUpdater {
    private static final String BASE_URL = "https://u.warze.org/w/";
    private static final String PACK_URL = BASE_URL + "hspcolor.zip";
    private static final String PACK_NAME = "hspcolor_latest.zip";
    private static final String PACK_PATH = "file/" + PACK_NAME;

    public static void run() {
        new Thread(() -> {
            try {
                updateResourcePack();
                updateMod();
            } catch (Exception e) {
                LoggerUtils.error("Error: " + e.getMessage());
            }
        }).start();
    }

    private static void updateResourcePack() throws Exception {
        Path mcDir = Minecraft.getInstance().gameDirectory.toPath();
        Path rpDir = mcDir.resolve("resourcepacks");
        Files.createDirectories(rpDir);
        Path outFile = rpDir.resolve(PACK_NAME);

        try (InputStream in = new URI(PACK_URL).toURL().openStream()) {
            Files.copy(in, outFile, StandardCopyOption.REPLACE_EXISTING);
            LoggerUtils.info("Downloaded resource pack to " + outFile);
        }

        Path optionsFile = mcDir.resolve("options.txt");
        if (!Files.exists(optionsFile)) {
            Files.write(optionsFile, List.of("resourcePacks:[\"" + PACK_PATH + "\"]"));
        } else {
            List<String> lines = Files.readAllLines(optionsFile);
            boolean wasFound = false;
            for (int i = 0; i < lines.size() && !wasFound; i++) {
                if (lines.get(i).startsWith("resourcePacks:[")) {
                    wasFound = true;
                    if (!lines.get(i).contains("\"" + PACK_PATH + "\"")) {
                        int insertPos = lines.get(i).lastIndexOf("]");
                        String updated = lines.get(i).substring(0, insertPos) +
                                (lines.get(i).charAt(insertPos - 1) != '[' ? "," : "") +
                                "\"" + PACK_PATH + "\"]";
                        lines.set(i, updated);
                    }
                    Files.write(optionsFile, lines);
                    LoggerUtils.info("Added resource pack to options.txt");
                }
            }
            if (!wasFound) {
                lines.add("resourcePacks:[\"" + PACK_PATH + "\"]");
                Files.write(optionsFile, lines);
                LoggerUtils.info("Appended resourcePacks line to options.txt");
            }
        }
    }

    public static void updateMod() {
        try {
            Path mcDir = Minecraft.getInstance().gameDirectory.toPath();
            Path modDir = mcDir.resolve("mods");
            String remoteVersion = new String(new URI(BASE_URL + "hspcolorlatest.txt").toURL().openStream().readAllBytes()).trim();
            Path jarOut = modDir.resolve("hspcolorv" + remoteVersion + ".jar");

            if (!Files.exists(jarOut)) {
                try (InputStream in = new URI(BASE_URL + "hspcolorv" + remoteVersion + ".jar").toURL().openStream()) {
                    Files.copy(in, jarOut, StandardCopyOption.REPLACE_EXISTING);
                    LoggerUtils.info("Downloaded new mod version: " + remoteVersion);
                } catch (Exception e) {
                    LoggerUtils.error("Failed to download mod: " + e.getMessage());
                    return;
                }
            } else {
                LoggerUtils.info("Mod already exists, skipping download");
            }

            List<Path> mods = new ArrayList<>();
            try (Stream<Path> stream = Files.list(modDir)) {
                stream.forEach(mods::add);
            }
            for (Path mod : mods) {
                String fileName = mod.getFileName().toString();
                if (!fileName.startsWith("hspcolorv") || !fileName.endsWith(".jar")) continue;
                String version = fileName.substring(8, fileName.length() - 4);
                if (version.equals(remoteVersion)) continue;
                if (!VersionUtils.isNewer(version, remoteVersion)) {
                    try {
                        Files.delete(mod);
                        LoggerUtils.info("Deleted old mod version: " + version);
                    } catch (Exception e) {
                        LoggerUtils.error("Failed to delete old mod: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtils.error("Failed to check or download mod: " + e.getMessage());
        }
    }
}
