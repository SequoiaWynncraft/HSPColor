package net.warze.hspcolor.utils;

import net.minecraft.client.Minecraft;

/**
 * @author Warze
 */
public class MCServerUtils {
    public static boolean isWynnCraft() {
        try {
            String ip = Minecraft.getInstance().getCurrentServer().ip;
            return ip.contains("wynncraft");
        } catch (Exception e) {
            return false;
        }
    }
}
