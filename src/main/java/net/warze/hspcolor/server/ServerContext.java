package net.warze.hspcolor.server;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

public final class ServerContext {
    private static final List<String> SCOREBOARD_CUES = List.of(
        "wynncraft",
        "territory",
        "world:",
        "combat lv",
        "guild",
        "wars",
        "online"
    );

    private static final long REFRESH_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(750);

    private final AtomicBoolean onWynncraft = new AtomicBoolean();
    private long lastEvaluation;

    public boolean isOnWynncraft() {
        refresh();
        return onWynncraft.get();
    }

    private synchronized void refresh() {
        long now = System.nanoTime();
        if (now - lastEvaluation < REFRESH_INTERVAL_NANOS) return;
        lastEvaluation = now;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.isIntegratedServerRunning()) {
            onWynncraft.set(false);
            return;
        }
        boolean detected = detectFromNetwork(client) || detectFromScoreboard(client);
        onWynncraft.set(detected);
    }

    private boolean detectFromNetwork(MinecraftClient client) {
        ServerInfo entry = client.getCurrentServerEntry();
        if (entry != null && entry.address != null) {
            if (containsWynn(entry.address)) return true;
        }
        ClientPlayNetworkHandler network = client.getNetworkHandler();
        if (network == null) return false;
        ClientConnection connection = network.getConnection();
        if (connection == null || !connection.isOpen()) return false;
        SocketAddress socketAddress = connection.getAddress();
        if (socketAddress == null) return false;
        return containsWynn(socketAddress.toString());
    }

    private boolean detectFromScoreboard(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) return false;
        Scoreboard scoreboard = world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false;
        if (textContains(objective.getDisplayName())) return true;
        Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(objective);
        if (entries.isEmpty()) return false;
        int matches = 0;
        for (ScoreboardEntry entry : entries) {
            Text display = entry.display();
            if (textContains(display)) {
                matches++;
                if (matches >= 2) return true;
            }
        }
        return false;
    }

    private boolean textContains(Text text) {
        if (text == null) return false;
        String normalized = text.getString().toLowerCase(Locale.ROOT);
        for (String cue : SCOREBOARD_CUES) {
            if (normalized.contains(cue)) return true;
        }
        return false;
    }

    private boolean containsWynn(String value) {
        return value.toLowerCase(Locale.ROOT).contains("wynncraft");
    }
}
