package net.warze.hspcolor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.warze.hspcolor.utils.LoggerUtils;
import net.warze.hspcolor.utils.ModUpdater;

public class HspColor implements ModInitializer {
    @Override
    public void onInitialize() {
        LoggerUtils.init();
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ModUpdater.run();
        }
    }
}
