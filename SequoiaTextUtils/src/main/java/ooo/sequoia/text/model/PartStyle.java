// Provided by Sequoia Mod
package ooo.sequoia.text.model;

import com.wynntils.utils.colors.CustomColor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class PartStyle {
    private static final Int2ObjectMap<Formatting> COLOR_LOOKUP = Arrays.stream(Formatting.values())
        .filter(Formatting::isColor)
        .collect(Int2ObjectOpenHashMap::new, (map, formatting) -> map.put(formatting.getColorValue() | 0xFF000000, formatting), Map::putAll);

    private final StyledTextPart owner;
    private final CustomColor color;
    private final CustomColor shadowColor;
    private final boolean obfuscated;
    private final boolean bold;
    private final boolean strikethrough;
    private final boolean underlined;
    private final boolean italic;
    private final ClickEvent clickEvent;
    private final HoverEvent hoverEvent;
    private final Identifier font;

    private PartStyle(StyledTextPart owner, CustomColor color, CustomColor shadowColor, boolean obfuscated, boolean bold,
                      boolean strikethrough, boolean underlined, boolean italic, ClickEvent clickEvent, HoverEvent hoverEvent,
                      Identifier font) {
        this.owner = owner;
        this.color = color;
        this.shadowColor = shadowColor;
        this.obfuscated = obfuscated;
        this.bold = bold;
        this.strikethrough = strikethrough;
        this.underlined = underlined;
        this.italic = italic;
        this.clickEvent = clickEvent;
        this.hoverEvent = hoverEvent;
        this.font = font;
    }

    PartStyle(PartStyle partStyle, StyledTextPart owner) {
        this(owner, partStyle.color, partStyle.shadowColor, partStyle.obfuscated, partStyle.bold, partStyle.strikethrough,
            partStyle.underlined, partStyle.italic, partStyle.clickEvent, partStyle.hoverEvent, partStyle.font);
    }

    static PartStyle fromStyle(Style style, StyledTextPart owner, Style parentStyle) {
        Style inherited = parentStyle == null ? style : style.withParent(parentStyle);
        CustomColor textColor = inherited.getColor() == null ? CustomColor.NONE : CustomColor.fromInt(inherited.getColor().getRgb() | 0xFF000000);
        Integer shadow = inherited.getShadowColor();
        CustomColor shadowColor = shadow == null ? CustomColor.NONE : CustomColor.fromInt(shadow | 0xFF000000);
        return new PartStyle(owner, textColor, shadowColor, inherited.isObfuscated(), inherited.isBold(), inherited.isStrikethrough(),
            inherited.isUnderlined(), inherited.isItalic(), inherited.getClickEvent(), inherited.getHoverEvent(), inherited.getFont());
    }

    public String asString(PartStyle previousStyle, StyleType type) {
        if (type == StyleType.NONE) return "";

        StringBuilder builder = new StringBuilder();
        boolean skipFormatting = false;
        if (previousStyle != null && (color == CustomColor.NONE || previousStyle.color.equals(color))) {
            String diff = tryConstructDifference(previousStyle, type == StyleType.INCLUDE_EVENTS);
            if (diff != null) {
                builder.append(diff);
                skipFormatting = true;
            } else {
                builder.append('§').append(Formatting.RESET.getCode());
            }
        }

        if (!skipFormatting) {
            if (color != CustomColor.NONE) {
                Formatting formatting = COLOR_LOOKUP.get(color.asInt());
                if (formatting != null) {
                    builder.append('§').append(formatting.getCode());
                } else {
                    builder.append('§').append(color.toHexString());
                }
            }
            if (obfuscated) builder.append('§').append(Formatting.OBFUSCATED.getCode());
            if (bold) builder.append('§').append(Formatting.BOLD.getCode());
            if (strikethrough) builder.append('§').append(Formatting.STRIKETHROUGH.getCode());
            if (underlined) builder.append('§').append(Formatting.UNDERLINE.getCode());
            if (italic) builder.append('§').append(Formatting.ITALIC.getCode());

            if (type == StyleType.INCLUDE_EVENTS) {
                if (clickEvent != null) builder.append('§').append('[').append(owner.getParent().getClickEventIndex(clickEvent)).append(']');
                if (hoverEvent != null) builder.append('§').append('<').append(owner.getParent().getHoverEventIndex(hoverEvent)).append('>');
            }
        }

        return builder.toString();
    }

    public Style getStyle() {
        Style style = Style.EMPTY;
        if (color != CustomColor.NONE) {
            style = style.withColor(TextColor.fromRgb(color.asInt() & 0xFFFFFF));
        }
        if (shadowColor != CustomColor.NONE) {
            style = style.withShadowColor(shadowColor.asInt() & 0xFFFFFF);
        }
        if (bold) style = style.withBold(true);
        if (italic) style = style.withItalic(true);
        if (underlined) style = style.withUnderline(true);
        if (strikethrough) style = style.withStrikethrough(true);
        if (obfuscated) style = style.withObfuscated(true);
        if (clickEvent != null) style = style.withClickEvent(clickEvent);
        if (hoverEvent != null) style = style.withHoverEvent(hoverEvent);
        if (font != null) style = style.withFont(font);
        return style;
    }

    public PartStyle withClickEvent(ClickEvent event) {
        return new PartStyle(owner, color, shadowColor, obfuscated, bold, strikethrough, underlined, italic, event, hoverEvent, font);
    }

    public PartStyle withHoverEvent(HoverEvent event) {
        return new PartStyle(owner, color, shadowColor, obfuscated, bold, strikethrough, underlined, italic, clickEvent, event, font);
    }

    public PartStyle withColor(CustomColor customColor) {
        return new PartStyle(owner, customColor, shadowColor, obfuscated, bold, strikethrough, underlined, italic, clickEvent, hoverEvent, font);
    }

    public PartStyle withFont(Identifier newFont) {
        return new PartStyle(owner, color, shadowColor, obfuscated, bold, strikethrough, underlined, italic, clickEvent, hoverEvent, newFont);
    }

    public PartStyle withShadowColor(CustomColor newShadow) {
        return new PartStyle(owner, color, newShadow, obfuscated, bold, strikethrough, underlined, italic, clickEvent, hoverEvent, font);
    }

    public PartStyle withBold(boolean toggle) {
        return new PartStyle(owner, color, shadowColor, obfuscated, toggle, strikethrough, underlined, italic, clickEvent, hoverEvent, font);
    }

    public PartStyle withObfuscated(boolean toggle) {
        return new PartStyle(owner, color, shadowColor, toggle, bold, strikethrough, underlined, italic, clickEvent, hoverEvent, font);
    }

    public PartStyle withStrikethrough(boolean toggle) {
        return new PartStyle(owner, color, shadowColor, obfuscated, bold, toggle, underlined, italic, clickEvent, hoverEvent, font);
    }

    public PartStyle withUnderlined(boolean toggle) {
        return new PartStyle(owner, color, shadowColor, obfuscated, bold, strikethrough, toggle, italic, clickEvent, hoverEvent, font);
    }

    public PartStyle withItalic(boolean toggle) {
        return new PartStyle(owner, color, shadowColor, obfuscated, bold, strikethrough, underlined, toggle, clickEvent, hoverEvent, font);
    }

    public boolean isBold() {
        return bold;
    }

    public boolean isObfuscated() {
        return obfuscated;
    }

    public boolean isStrikethrough() {
        return strikethrough;
    }

    public boolean isUnderlined() {
        return underlined;
    }

    public boolean isItalic() {
        return italic;
    }

    public ClickEvent getClickEvent() {
        return clickEvent;
    }

    public HoverEvent getHoverEvent() {
        return hoverEvent;
    }

    public CustomColor getColor() {
        return color;
    }

    public CustomColor getShadowColor() {
        return shadowColor;
    }

    public Identifier getFont() {
        return font;
    }

    private String tryConstructDifference(PartStyle previous, boolean includeEvents) {
        StringBuilder diff = new StringBuilder();
        int oldColor = previous.color.asInt();
        int newColor = color.asInt();
        if (oldColor == -1) {
            if (newColor != -1) {
                Optional.ofNullable(Arrays.stream(Formatting.values())
                        .filter(f -> f.isColor() && newColor == (f.getColorValue() | 0xFF000000))
                        .findFirst().orElse(null))
                    .ifPresent(f -> diff.append('§').append(f.getCode()));
            }
        } else if (oldColor != newColor) {
            return null;
        }

        if (!previous.obfuscated && obfuscated) diff.append('§').append(Formatting.OBFUSCATED.getCode());
        else if (previous.obfuscated && !obfuscated) return null;

        if (!previous.bold && bold) diff.append('§').append(Formatting.BOLD.getCode());
        else if (previous.bold && !bold) return null;

        if (!previous.strikethrough && strikethrough) diff.append('§').append(Formatting.STRIKETHROUGH.getCode());
        else if (previous.strikethrough && !strikethrough) return null;

        if (!previous.underlined && underlined) diff.append('§').append(Formatting.UNDERLINE.getCode());
        else if (previous.underlined && !underlined) return null;

        if (!previous.italic && italic) diff.append('§').append(Formatting.ITALIC.getCode());
        else if (previous.italic && !italic) return null;

        if (includeEvents) {
            if (previous.clickEvent != null && clickEvent == null) return null;
            if (!Objects.equals(previous.clickEvent, clickEvent) && clickEvent != null) {
                diff.append('§').append('[').append(owner.getParent().getClickEventIndex(clickEvent)).append(']');
            }
            if (previous.hoverEvent != null && hoverEvent == null) return null;
            if (!Objects.equals(previous.hoverEvent, hoverEvent) && hoverEvent != null) {
                diff.append('§').append('<').append(owner.getParent().getHoverEventIndex(hoverEvent)).append('>');
            }
        }

        return diff.isEmpty() ? "" : diff.toString();
    }

    public enum StyleType {
        INCLUDE_EVENTS,
        DEFAULT,
        NONE
    }
}
