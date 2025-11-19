package net.warze.hspcolor;

import net.fabricmc.api.ModInitializer;
import net.warze.hspcolor.utils.LoggerUtils;
import net.warze.hspcolor.utils.ModUpdater;

public class hspcolor implements ModInitializer {
    @Override
    public void onInitialize() {
        LoggerUtils.init();
        ModUpdater.run();
    }
}
