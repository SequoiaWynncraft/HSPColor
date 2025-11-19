package net.warze.hspcolor.utils;

import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;

import java.util.regex.Pattern;

/**
 * @author Warze
 */
public class TextUtils {
    public static Component empty() {
        return Component.empty();
    }

    /**
     * replace keywords in a {@link MutableComponent}
     *
     * @param text      the text
     * @param oldStringPattern the RegEx pattern of the old string
     * @param newString new string
     * @return text after replacement
     */
    public static MutableComponent replaceTextInComponent(MutableComponent component, Pattern pattern, String replacement) {
        MutableComponent result = Component.empty().withStyle(component.getStyle());
        
        if (component.getContents() instanceof LiteralContents literal) {
            String text = literal.text();
            String replaced = pattern.matcher(text).replaceAll(replacement);
            result = Component.literal(replaced).withStyle(component.getStyle());
        } else {
            result = component.copy();
        }

        for (Component sibling : component.getSiblings()) {
            if (sibling instanceof MutableComponent mutableSibling) {
                result.append(replaceTextInComponent(mutableSibling, pattern, replacement));
            } else {
                result.append(sibling);
            }
        }

        return result;
    }
}
