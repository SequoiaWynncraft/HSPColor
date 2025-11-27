// Provided by Sequoia Mod
package ooo.sequoia.text.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class StyledTextPart {
    private final String text;
    private final PartStyle style;
    private final StyledText parent;

    public StyledTextPart(String text, Style style, StyledText parent, Style parentStyle) {
        this.parent = parent;
        this.text = text;
        this.style = PartStyle.fromStyle(style, this, parentStyle);
    }

    StyledTextPart(StyledTextPart part, StyledText parent) {
        this.text = part.text;
        this.style = new PartStyle(part.style, this);
        this.parent = parent;
    }

    StyledTextPart(StyledTextPart part, PartStyle style, StyledText parent) {
        this.text = part.text;
        this.style = style;
        this.parent = parent;
    }

    static List<StyledTextPart> fromCodedString(String codedString, Style style, StyledText parent, Style parentStyle) {
        List<StyledTextPart> parts = new ArrayList<>();
        Style currentStyle = style;
        StringBuilder currentString = new StringBuilder();
        boolean nextIsFormatting = false;
        StringBuilder hexColor = new StringBuilder();
        boolean clickEventPrefix = false;
        boolean hoverEventPrefix = false;
        StringBuilder eventDigits = new StringBuilder();

        for (char current : codedString.toCharArray()) {
            if (nextIsFormatting) {
                nextIsFormatting = false;
                if (parent != null) {
                    if (current == '[') {
                        clickEventPrefix = true;
                        continue;
                    }
                    if (current == '<') {
                        hoverEventPrefix = true;
                        continue;
                    }
                }

                if (current == '#') {
                    hexColor.append(current);
                } else {
                    FormattingSwitch:
                    {
                        net.minecraft.util.Formatting formatting = net.minecraft.util.Formatting.byCode(current);
                        if (formatting == null) {
                            currentString.append('§').append(current);
                            break FormattingSwitch;
                        }
                        if (!currentString.isEmpty()) {
                            if (style != Style.EMPTY) {
                                currentStyle = currentStyle.withClickEvent(style.getClickEvent()).withHoverEvent(style.getHoverEvent());
                            }
                            parts.add(new StyledTextPart(currentString.toString(), currentStyle, null, parentStyle));
                            currentString = new StringBuilder();
                        }
                        if (formatting.isColor()) {
                            currentStyle = Style.EMPTY.withColor(formatting);
                        } else {
                            currentStyle = currentStyle.withFormatting(formatting);
                        }
                    }
                }
                continue;
            }

            if (!clickEventPrefix && !hoverEventPrefix) {
                if (!hexColor.isEmpty()) {
                    hexColor.append(current);
                    if (hexColor.length() == 9) {
                        CustomColor customColor = CustomColor.fromHexString(hexColor.toString());
                        if (customColor == CustomColor.NONE) {
                            currentString.append(hexColor);
                        } else {
                            if (!currentString.isEmpty()) {
                                if (style != Style.EMPTY) {
                                    currentStyle = currentStyle.withClickEvent(style.getClickEvent()).withHoverEvent(style.getHoverEvent());
                                }
                                parts.add(new StyledTextPart(currentString.toString(), currentStyle, null, parentStyle));
                                currentString = new StringBuilder();
                            }
                            currentStyle = currentStyle.withColor(customColor.asInt());
                        }
                        hexColor = new StringBuilder();
                    }
                    continue;
                }

                if (current == '§') {
                    nextIsFormatting = true;
                    continue;
                }

                currentString.append(current);
                continue;
            }

            if (Character.isDigit(current)) {
                eventDigits.append(current);
                continue;
            }

            boolean handled = false;
            if (clickEventPrefix && current == ']') {
                ClickEvent clickEvent = parent.getClickEvent(Integer.parseInt(eventDigits.toString()));
                if (clickEvent != null) {
                    Style oldStyle = currentStyle;
                    currentStyle = currentStyle.withClickEvent(clickEvent);
                    clickEventPrefix = false;
                    eventDigits.setLength(0);
                    if (!currentString.isEmpty()) {
                        if (style != Style.EMPTY) {
                            currentStyle = currentStyle.withClickEvent(style.getClickEvent()).withHoverEvent(style.getHoverEvent());
                        }
                        parts.add(new StyledTextPart(currentString.toString(), oldStyle, null, parentStyle));
                        currentString = new StringBuilder();
                    }
                    handled = true;
                }
            }

            if (!handled && hoverEventPrefix && current == '>') {
                HoverEvent hoverEvent = parent.getHoverEvent(Integer.parseInt(eventDigits.toString()));
                if (hoverEvent != null) {
                    Style oldStyle = currentStyle;
                    currentStyle = currentStyle.withHoverEvent(hoverEvent);
                    hoverEventPrefix = false;
                    eventDigits.setLength(0);
                    if (!currentString.isEmpty()) {
                        if (style != Style.EMPTY) {
                            currentStyle = currentStyle.withClickEvent(style.getClickEvent()).withHoverEvent(style.getHoverEvent());
                        }
                        parts.add(new StyledTextPart(currentString.toString(), oldStyle, null, parentStyle));
                        currentString = new StringBuilder();
                    }
                    handled = true;
                }
            }

            if (!handled) {
                currentString.append(clickEventPrefix ? '[' : '<').append(eventDigits).append(current);
                clickEventPrefix = false;
                hoverEventPrefix = false;
                eventDigits.setLength(0);
            }
        }

        if (!currentString.isEmpty()) {
            if (style != Style.EMPTY) {
                currentStyle = currentStyle.withClickEvent(style.getClickEvent()).withHoverEvent(style.getHoverEvent());
            }
            parts.add(new StyledTextPart(currentString.toString(), currentStyle, null, parentStyle));
        }

        return parts;
    }

    static List<StyledTextPart> fromJson(JsonArray json) {
        if (json.isEmpty()) {
            return List.of(new StyledTextPart("", Style.EMPTY, null, Style.EMPTY));
        }
        List<StyledTextPart> parts = new ArrayList<>();
        for (JsonElement element : json) {
            if (!element.isJsonObject()) continue;
            JsonObject object = element.getAsJsonObject();
            String text = object.get("text").getAsString();
            Style style = Style.EMPTY;
            if (object.has("bold")) style = style.withBold(true);
            if (object.has("italic")) style = style.withItalic(true);
            if (object.has("underline")) style = style.withUnderline(true);
            if (object.has("strikethrough")) style = style.withStrikethrough(true);
            if (object.has("font")) style = style.withFont(Identifier.ofVanilla(object.get("font").getAsString()));
            if (object.has("color")) {
                style = style.withColor(CustomColor.fromHexString(object.get("color").getAsString()).asInt());
            }
            if (object.has("margin-left")) {
                String margin = object.get("margin-left").getAsString();
                if ("thin".equals(margin)) {
                    text = "À" + text;
                } else if ("large".equals(margin)) {
                    text = "ÀÀÀÀ" + text;
                }
            }
            parts.add(new StyledTextPart(text, style, null, Style.EMPTY));
        }
        return parts;
    }

    public String getString(PartStyle previous, PartStyle.StyleType type) {
        return style.asString(previous, type) + text;
    }

    public StyledText getParent() {
        return parent;
    }

    public PartStyle getPartStyle() {
        return style;
    }

    public StyledTextPart withStyle(PartStyle newStyle) {
        return new StyledTextPart(this, newStyle, parent);
    }

    public StyledTextPart withStyle(Function<PartStyle, PartStyle> transformer) {
        return withStyle(transformer.apply(style));
    }

    public MutableText getComponent() {
        return Text.literal(text).fillStyle(style.getStyle());
    }

    StyledTextPart asNormalized() {
        return new StyledTextPart(WynnUtils.normalizeBadString(text), style.getStyle(), parent, null);
    }

    StyledTextPart stripLeading() {
        return new StyledTextPart(text.stripLeading(), style.getStyle(), parent, null);
    }

    StyledTextPart stripTrailing() {
        return new StyledTextPart(text.stripTrailing(), style.getStyle(), parent, null);
    }

    boolean isEmpty() {
        return text.isEmpty();
    }

    boolean isBlank() {
        return text.isBlank();
    }

    public int length() {
        return text.length();
    }

    @Override
    public String toString() {
        return "StyledTextPart[text=" + text + ", style=" + style + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StyledTextPart that)) return false;
        return Objects.equals(text, that.text) && Objects.equals(style, that.style);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, style);
    }
}
