package net.warze.hspcolor;

import net.warze.hspcolor.chat.ChatMessageInterceptor;
import net.warze.hspcolor.config.ConfigManager;
import net.warze.hspcolor.config.HspColorConfig;
import net.warze.hspcolor.server.ServerContext;
import net.warze.hspcolor.updater.ResourceUpdater;

public final class HspColor {
    private static final HspColor INSTANCE = new HspColor();

    private final ServerContext serverContext = new ServerContext();
    private HspColorConfig config;
    private ChatMessageInterceptor chatInterceptor;
    private ResourceUpdater resourceUpdater;

    private HspColor() {
    }

    public static HspColor instance() {
        return INSTANCE;
    }

    public void initialize() {
        config = ConfigManager.load();
        chatInterceptor = new ChatMessageInterceptor(serverContext, config);
        resourceUpdater = new ResourceUpdater(config);
        if (config.resourcePackUpdates || config.modUpdates) {
            resourceUpdater.start();
        }
        saveConfig();
    }

    public ChatMessageInterceptor chatInterceptor() {
        return chatInterceptor;
    }

    public ServerContext serverContext() {
        return serverContext;
    }

    public HspColorConfig config() {
        return config;
    }

    public void saveConfig() {
        if (config != null) {
            ConfigManager.save(config);
        }
    }
}
