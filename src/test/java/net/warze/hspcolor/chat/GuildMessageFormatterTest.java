package net.warze.hspcolor.chat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ooo.sequoia.text.model.StyledText;
import org.junit.jupiter.api.Test;

class GuildMessageFormatterTest {
    private final GuildMessageDetector detector = new GuildMessageDetector();
    private final GuildMessageFormatter formatter = new GuildMessageFormatter();
    private static final String STRATEGIST_LOG = "󏿼󏿿󏿾 󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿄 _WayLessSad_: dfhdg";

    @Test
    void rewrittenMessageDoesNotInheritCustomFont() {
        Identifier customFont = Identifier.of("wynntils", "pill");
        MutableText pill = Text.literal(GuildRank.STRATEGIST.displayToken())
            .setStyle(Style.EMPTY.withFont(customFont));
        MutableText spacer = Text.literal(" ");
        MutableText player = Text.literal("_WayLessSad_: MEEEOW");
        MutableText message = Text.empty().append(pill).append(spacer).append(player);

        StyledText styled = StyledText.fromComponent(message);
        GuildMessageDetector.Detection detection = detector.detect(styled);
        assertNotNull(detection);

        MutableText rewritten = formatter.apply(detection);
        assertFalse(containsFont(rewritten, customFont), "Formatter should not copy source font");
    }

    @Test
    void glyphRankIsReplacedWithCustomPill() {
        StyledText styled = StyledText.fromUnformattedString(STRATEGIST_LOG);
        GuildMessageDetector.Detection detection = detector.detect(styled);
        assertNotNull(detection);

        MutableText rewritten = formatter.apply(detection);
        String flattened = rewritten.getString();
        assertFalse(flattened.contains(GuildRank.STRATEGIST.normalizedLegacyToken()));
        assertFalse(flattened.contains(GuildRank.STRATEGIST.normalizedDisplayToken()));
    }

    @Test
    void canStripRankPillEntirely() {
        GuildMessageFormatter strippingFormatter = new GuildMessageFormatter(() -> true);
        StyledText styled = StyledText.fromUnformattedString(STRATEGIST_LOG);
        GuildMessageDetector.Detection detection = detector.detect(styled);
        assertNotNull(detection);

        MutableText rewritten = strippingFormatter.apply(detection);
        String flattened = rewritten.getString();
        assertFalse(flattened.contains(GuildRank.STRATEGIST.plainName()));
        assertFalse(flattened.contains(GuildRank.STRATEGIST.normalizedLegacyToken()));
    }

    private static boolean containsFont(Text text, Identifier font) {
        if (font.equals(text.getStyle().getFont())) {
            return true;
        }
        for (Text sibling : text.getSiblings()) {
            if (containsFont(sibling, font)) return true;
        }
        return false;
    }
}
