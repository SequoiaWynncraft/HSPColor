package net.warze.hspcolor;

import net.fabricmc.api.ClientModInitializer;

public final class HspColorClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HspColor.instance().initialize();
	}
}
