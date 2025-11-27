package net.warze.hspcolor.config;

public final class HspColorConfig {
    public boolean resourcePackUpdates = true;
    public boolean modUpdates = true;
    public boolean guildFormatter = true;
    public boolean colorNormalizer = true;
    public boolean stripGuildRankPill = false;

    public static HspColorConfig defaults() {
        return new HspColorConfig();
    }
}
