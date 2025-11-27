package ooo.sequoia.text.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.text.MutableText;
import org.junit.jupiter.api.Test;

class TeXPillTest {
    private final TeXParser parser = new TeXParser();

    @Test
    void pillMacroDoesNotLeakRawCommands() {
        MutableText parsed = parser.parseMutableText("\\pill{48CE87}{2ECC71}{CAPTAIN}");
        String flattened = parsed.getString();
        assertFalse(flattened.contains("\\color"), "Guild pills should render glyphs, not raw TeX commands");
    }

    @Test
    void pillBadgeMacroRendersIcon() {
        MutableText parsed = parser.parseMutableText("\\pillbadge{48CE87}{000000}{000000}{CAPTAIN}");
        String flattened = parsed.getString();
        assertTrue(flattened.indexOf('\ueffa') >= 0, "Badge glyph should be included before rank text");
        assertFalse(flattened.contains("\\color"), "Badge macro should not leak raw TeX commands");
    }
}
