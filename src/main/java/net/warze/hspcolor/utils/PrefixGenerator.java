package net.warze.hspcolor.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * @author Warze
 */
public class PrefixGenerator {
    public PrefixGenerator() {}

    private static char getUnicodeChar(char letter) {
        int offset;
        if (letter >= 'A' && letter <= 'Z') {
            offset = letter - 65;
            return (char)('\ue040' + offset);
        } else if (letter >= 'a' && letter <= 'z') {
            offset = letter - 97;
            return (char)('\ue040' + offset);
        } else {
            return ' ';
        }
    }

    public static MutableComponent getPrefix(String text, int bgColor, int textColor) {
        MutableComponent prefix = Component.literal("")
            .append(Component.literal("\ue010\u2064").withColor(bgColor));

        char[] characters = text.toCharArray();
        for (char c : characters) {
            if (Character.isLetter(c)) {
                char unicodeChar = getUnicodeChar(c);
                prefix.append(Component.literal("\ue00f\ue012").withColor(bgColor))
                      .append(Component.literal("" + unicodeChar).withColor(textColor));
            }
        }

        prefix.append(Component.literal("\ue011").withColor(bgColor));
        return prefix;
    }

    public static MutableComponent getPrefix(String text, int bgColor, int textColor, char icon) {
        MutableComponent prefix = Component.literal("")
            .append(Component.literal("\ue010\u2064").withColor(bgColor));
    
        prefix.append(Component.literal("\ue00f\ue012").withColor(bgColor))
              .append(Component.literal("\ueffa").withColor(textColor))
              .append(Component.literal("\ue00f\ue012").withColor(bgColor));
    
        char[] characters = text.toCharArray();
        for (char c : characters) {
            if (Character.isLetter(c)) {
                char unicodeChar = getUnicodeChar(c);
                prefix.append(Component.literal("\ue00f\ue012").withColor(bgColor))
                      .append(Component.literal(String.valueOf(unicodeChar)).withColor(textColor));
            }
        }
    
        prefix.append(Component.literal("\ue011").withColor(bgColor));
        return prefix;
    }    

    public MutableComponent getCustomPrefix(String text, int bgColor, int textColor) {
        return getPrefix(text, bgColor, textColor);
    }
}
