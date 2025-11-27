// Provided by Sequoia Mod
package ooo.sequoia.text;

import net.minecraft.client.font.TextRenderer;
import java.util.Locale;

public class TextUtils {
    public static String padInvisible(TextRenderer renderer, String text, int targetWidth) {
        int have = renderer.getWidth(text);
        int need = targetWidth - have;
        if (need <= 0) return text;

        int bold = 0;
        int regular = 0;
        for (int b = 0; b <= 3; b++) {
            int remainder = need - 5 * b;
            if (remainder >= 0 && remainder % 4 == 0) {
                bold = b;
                regular = remainder / 4;
                break;
            }
        }

        StringBuilder out = new StringBuilder(text);
        if (bold > 0) {
            out.append("§l");
            appendSpaces(out, bold);
            out.append("§r");
        }
        if (regular > 0) {
            appendSpaces(out, regular);
        }
        return out.toString();
    }

    public static String upperfirst(String text) {
        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1).toLowerCase(Locale.ROOT);
    }

    private static void appendSpaces(StringBuilder builder, int count) {
        for (int i = 0; i < count; i++) {
            builder.append(' ');
        }
    }
}
