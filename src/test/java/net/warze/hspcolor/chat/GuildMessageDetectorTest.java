package net.warze.hspcolor.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ooo.sequoia.text.model.PartStyle;
import ooo.sequoia.text.model.StyledText;
import org.junit.jupiter.api.Test;

class GuildMessageDetectorTest {
    private final GuildMessageDetector detector = new GuildMessageDetector();

    @Test
    void detectsStrategistFromSystemLog() {
        String strategist = "󏿼󏿿󏿾 󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿄󐀂 _WayLessSad_: meow";
        StyledText styled = StyledText.fromUnformattedString(strategist);
        String plain = styled.getString(PartStyle.StyleType.NONE);
        String stripped = stripFormatting(plain);
        debugPrint("strategist", stripped, GuildRank.STRATEGIST.normalizedLegacyToken());
        GuildMessageDetector.Detection detection = detector.detect(styled);
        assertNotNull(detection, () ->
            "Detector failed. plain=" + plain +
                " stripped=" + stripped +
                " containsLegacy=" + stripped.contains(GuildRank.STRATEGIST.normalizedLegacyToken()) +
                " containsDisplay=" + stripped.contains(GuildRank.STRATEGIST.normalizedDisplayToken()));
        assertEquals(GuildRank.STRATEGIST, detection.rank());
        assertEquals(stripped.indexOf(GuildRank.STRATEGIST.normalizedLegacyToken()), detection.visibleStart());
    }

    @Test
    void detectsChiefFromSystemLog() {
        String chief = "󏿼󐀆 󏿿󏿿󏿿󏿿󏿿󏿿󏿢󐀂 Sparkles_: meow";
        StyledText styled = StyledText.fromUnformattedString(chief);
        String plain = styled.getString(PartStyle.StyleType.NONE);
        String stripped = stripFormatting(plain);
        debugPrint("chief", stripped, GuildRank.CHIEF.normalizedLegacyToken());
        GuildMessageDetector.Detection detection = detector.detect(styled);
        assertNotNull(detection, () ->
            "Detector failed. plain=" + plain +
                " stripped=" + stripped +
                " containsLegacy=" + stripped.contains(GuildRank.CHIEF.normalizedLegacyToken()) +
                " containsDisplay=" + stripped.contains(GuildRank.CHIEF.normalizedDisplayToken()));
        assertEquals(GuildRank.CHIEF, detection.rank());
        assertEquals(stripped.indexOf(GuildRank.CHIEF.normalizedLegacyToken()), detection.visibleStart());
    }

    @Test
    void detectsRecruitFromAsciiName() {
        String recruit = "[Guild] RECRUIT badge PlayerOne: hi";
        StyledText styled = StyledText.fromUnformattedString(recruit);
        GuildMessageDetector.Detection detection = detector.detect(styled);
        assertNotNull(detection);
        assertEquals(GuildRank.RECRUIT, detection.rank());
        assertEquals(recruit.indexOf("RECRUIT"), detection.visibleStart());
    }

    private static void debugPrint(String label, String stripped, String token) {
        System.out.println("--- " + label + " stripped codepoints ---");
        stripped.codePoints().limit(40).forEach(cp -> System.out.print(Integer.toHexString(cp) + " "));
        System.out.println();
        System.out.println("--- token codepoints ---");
        token.codePoints().forEach(cp -> System.out.print(Integer.toHexString(cp) + " "));
        System.out.println();
    }

    private static String stripFormatting(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder builder = new StringBuilder(text.length());
        boolean skip = false;
        for (int i = 0; i < text.length();) {
            char ch = text.charAt(i);
            if (skip) {
                skip = false;
                i++;
                continue;
            }
            if (ch == '\u00A7') {
                skip = true;
                i++;
                continue;
            }
            int cp = text.codePointAt(i);
            if (Character.getType(cp) == Character.FORMAT) {
                i += Character.charCount(cp);
                continue;
            }
            builder.appendCodePoint(cp);
            i += Character.charCount(cp);
        }
        return builder.toString();
    }
}
