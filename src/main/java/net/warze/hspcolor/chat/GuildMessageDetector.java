package net.warze.hspcolor.chat;

import java.util.Locale;
import ooo.sequoia.text.model.PartStyle;
import ooo.sequoia.text.model.StyledText;

final class GuildMessageDetector {

    Detection detect(StyledText styled) {
        String plain = styled.getString(PartStyle.StyleType.NONE);
        int colon = plain.indexOf(':');
        if (colon <= 0) return null;
        RankMatch match = findRankMatch(plain, colon);
        if (match == null) return null;
        int visibleStart = match.startIndex();
        if (visibleStart < 0 || visibleStart >= colon) return null;
        String visible = plain.substring(visibleStart, colon);
        if (visible.isBlank()) return null;
        return new Detection(styled, match.rank(), visibleStart, colon, visible);
    }

    private RankMatch findRankMatch(String plain, int colon) {
        String header = plain.substring(0, colon);
        for (GuildRank rank : GuildRank.values()) {
            int legacy = header.indexOf(rank.normalizedLegacyToken());
            if (legacy >= 0) return new RankMatch(rank, legacy);
            int display = header.indexOf(rank.normalizedDisplayToken());
            if (display >= 0) return new RankMatch(rank, display);
        }
        String uppercase = header.toUpperCase(Locale.ROOT);
        for (GuildRank rank : GuildRank.values()) {
            String plainName = rank.plainName();
            if (plainName.isEmpty()) continue;
            int idx = uppercase.indexOf(plainName);
            if (idx >= 0) return new RankMatch(rank, idx);
        }
        return null;
    }

    record Detection(StyledText styled, GuildRank rank, int visibleStart, int visibleEnd, String visible) {}

    private record RankMatch(GuildRank rank, int startIndex) {}
}
