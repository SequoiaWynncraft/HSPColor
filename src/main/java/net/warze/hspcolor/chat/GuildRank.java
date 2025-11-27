package net.warze.hspcolor.chat;

import java.util.Locale;

enum GuildRank {
    OWNER("󏿿󏿿󏿿󏿿󏿿󏿿󏿠", "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿠", 0xFFE91E63, 0xFFE9387B, "OWNER"),
    CHIEF("󏿿󏿿󏿿󏿿󏿿󏿿󏿢", "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿢", 0xFFE67E22, 0xFFE6993C, "CHIEF"),
    STRATEGIST("󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿄", "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿄", 0xFF8F4CE2, 0xFFA66DEC, "STRATEGIST"),
    CAPTAIN("󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿖", "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿖", 0xFF2ECC71, 0xFF48CE87, "CAPTAIN"),
    RECRUITER("󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿊", "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿊", 0xFF206694, 0xFF508DD8, "RECRUITER"),
    RECRUIT("󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿖", "󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿖", 0xFF9EB4BE, 0xFFB3BDC5, "RECRUIT");

    private final String legacyToken;
    private final String displayToken;
    private final String normalizedLegacyToken;
    private final String normalizedDisplayToken;
    private final int roleColor;
    private final int nameColor;
    private final String plainName;

    GuildRank(String legacyToken, String displayToken, int roleColor, int nameColor, String plainName) {
        this.legacyToken = legacyToken;
        this.displayToken = displayToken;
        this.normalizedLegacyToken = normalizeToken(legacyToken);
        this.normalizedDisplayToken = normalizeToken(displayToken);
        this.roleColor = roleColor;
        this.nameColor = nameColor;
        this.plainName = plainName == null ? "" : plainName.toUpperCase(Locale.ROOT);
    }

    String legacyToken() {
        return legacyToken;
    }

    String displayToken() {
        return displayToken;
    }

    String normalizedLegacyToken() {
        return normalizedLegacyToken;
    }

    String normalizedDisplayToken() {
        return normalizedDisplayToken;
    }

    int roleColor() {
        return roleColor;
    }

    int roleColorRgb() {
        return roleColor & 0xFFFFFF;
    }

    int nameColor() {
        return nameColor;
    }

    String plainName() {
        return plainName;
    }

    private static String normalizeToken(String token) {
        if (token == null || token.isEmpty()) return "";
        StringBuilder builder = new StringBuilder(token.length());
        for (int i = 0; i < token.length();) {
            int cp = token.codePointAt(i);
            if (Character.getType(cp) != Character.FORMAT) {
                builder.appendCodePoint(cp);
            }
            i += Character.charCount(cp);
        }
        return builder.toString();
    }
}
