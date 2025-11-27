// Provided by Sequoia Mod
package ooo.sequoia.text.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import ooo.sequoia.text.model.PartStyle;
import ooo.sequoia.text.model.StyledText;
import ooo.sequoia.text.model.StyledTextPart;

public class TeXParser {
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
        "\\\\([^\\s\\\\{}]+)" +
            "|\\\\([\\\\{}])" +
            "|\\{" +
            "|}" +
            "|[^\\\\{}]+"
    );

    private static final UnaryOperator<Style> LIGHT = style -> style.withColor(0xC7D5F5);
    private static final UnaryOperator<Style> NORMAL = style -> style.withColor(0x9FB3D7);
    private static final UnaryOperator<Style> DARK = style -> style.withColor(0x6C7EA8);
    private static final UnaryOperator<Style> ACCENT1 = style -> style.withColor(0x5DE4C7);
    private static final UnaryOperator<Style> ACCENT2 = style -> style.withColor(0x4FC3F7);
    private static final UnaryOperator<Style> ACCENT3 = style -> style.withColor(0xF48FB1);
    private static final UnaryOperator<Style> BOLD = style -> style.withBold(true);
    private static final UnaryOperator<Style> ITALIC = style -> style.withItalic(true);
    private static final UnaryOperator<Style> UNDERLINE = style -> style.withUnderline(true);
    private static final UnaryOperator<Style> STRIKETHROUGH = style -> style.withStrikethrough(true);
    private static final UnaryOperator<Style> OBFUSCATED = style -> style.withObfuscated(true);

    private final Map<String, BiFunction<List<String>, Integer, R>> texFunctions;

    public TeXParser() {
        Map<String, BiFunction<List<String>, Integer, R>> m = new HashMap<>();
        m.put("\\gradient", this::gradient);
        m.put("\\color", this::color);
        m.put("\\dcolor", this::dcolor);
        m.put("\\hover", this::hover);
        m.put("\\click", this::click);
        m.put("\\ranking", this::ranking);
        m.put("\\nickname", this::nickname);
        m.put("\\pill", this::pill);
        m.put("\\pillbadge", this::pillBadge);
        m.put("\\-", this::light);
        m.put("\\=", this::normal);
        m.put("\\+", this::dark);
        m.put("\\1", this::accent1);
        m.put("\\2", this::accent2);
        m.put("\\3", this::accent3);
        m.put("\\i", this::italicize);
        m.put("\\b", this::boldfont);
        m.put("\\u", this::underline);
        m.put("\\s", this::strikethrough);
        m.put("\\k", this::obfuscated);
        texFunctions = m;
    }

    private record R(MutableText text, int next) {}

    public MutableText parseMutableText(String text) {
        return parseMutableText(tokenize(text), 0).text;
    }

    public MutableText parseMutableText(String text, Object... parameters) {
        return parseMutableText(String.format(text, parameters));
    }

    public String sanitize(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        StringBuilder sb = new StringBuilder(raw.length() * 2);
        for (char ch : raw.toCharArray()) {
            if (ch == '\\' || ch == '{' || ch == '}') sb.append('\\');
            sb.append(ch);
        }
        return sb.toString();
    }

    public String toTeX(StyledText styled) {
        StringBuilder out = new StringBuilder();
        ClickEvent currentClick = null;
        HoverEvent currentHover = null;
        StringBuilder run = new StringBuilder();

        for (StyledTextPart part : styled) {
            ClickEvent clickEvent = part.getPartStyle().getClickEvent();
            HoverEvent hoverEvent = part.getPartStyle().getHoverEvent();
            if (!same(clickEvent, currentClick) || !same(hoverEvent, currentHover)) {
                flushRun(out, run, currentClick, currentHover);
                run.setLength(0);
                currentClick = clickEvent;
                currentHover = hoverEvent;
            }
            run.append(escapeTeX(part.getString(null, PartStyle.StyleType.DEFAULT)));
        }

        flushRun(out, run, currentClick, currentHover);
        return out.toString();
    }

    private void flushRun(StringBuilder out, StringBuilder run, ClickEvent click, HoverEvent hover) {
        if (run.length() == 0) return;
        String inner = run.toString();
        if (hover != null) {
            String hoverText = serializeHoverText(hover);
            inner = "\\hover{" + hoverText + "}{" + inner + "}";
        }
        if (click != null) {
            inner = "\\click{" + click.getAction().name() + "}{" + escapeTeX(click.getValue()) + "}{" + inner + "}";
        }
        out.append(inner);
    }

    private boolean same(ClickEvent a, ClickEvent b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.getAction() == b.getAction() && Objects.equals(a.getValue(), b.getValue());
    }

    private boolean same(HoverEvent a, HoverEvent b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.getAction() != b.getAction()) return false;
        if (a.getAction() == HoverEvent.Action.SHOW_TEXT) {
            Text ta = a.getValue(HoverEvent.Action.SHOW_TEXT);
            Text tb = b.getValue(HoverEvent.Action.SHOW_TEXT);
            return Objects.equals(ta == null ? null : ta.getString(), tb == null ? null : tb.getString());
        }
        return Objects.equals(a.getValue(a.getAction()), b.getValue(b.getAction()));
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(text);
        while (matcher.find()) tokens.add(matcher.group());
        return tokens;
    }

    private R parseMutableText(List<String> tokens, int index) {
        MutableText parsed = Text.empty();
        while (index < tokens.size() && !tokens.get(index).equals("}")) {
            String token = tokens.get(index);
            R literal;
            if (token.equals("\\n")) {
                literal = new R(Text.literal("\n"), index + 1);
            } else if (token.startsWith("\\") && !token.equals("\\\\") && !token.equals("\\{") && !token.equals("\\}")) {
                index++;
                BiFunction<List<String>, Integer, R> fn = texFunctions.get(token);
                literal = fn == null ? getNextLiteral(tokens, index - 1) : fn.apply(tokens, index);
            } else {
                literal = getNextLiteral(tokens, index);
            }
            parsed.append(literal.text);
            index = literal.next;
        }
        return new R(parsed, index + 1);
    }

    private R getNextLiteral(List<String> tokens, int index) {
        String token = tokens.get(index);
        if ("{".equals(token)) {
            return parseMutableText(tokens, index + 1);
        }
        return new R(Text.literal(token), index + 1);
    }

    private R gradient(List<String> tokens, int index) {
        R countLiteral = getNextLiteral(tokens, index);
        int colorCount = Integer.parseInt(countLiteral.text.getString());
        index = countLiteral.next;
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < colorCount; i++) {
            R colorLiteral = getNextLiteral(tokens, index);
            colors.add(Integer.parseInt(colorLiteral.text.getString(), 16));
            index = colorLiteral.next;
        }
        R textLiteral = getNextLiteral(tokens, index);
        String raw = textLiteral.text.getString();
        index = textLiteral.next;
        MutableText out = Text.empty();
        int n = raw.length();
        if (n == 0 || colorCount == 0) return new R(out, index);
        for (int i = 0; i < n; i++) {
            int rgb = getRgb(i, n, colorCount, colors);
            out.append(Text.literal(String.valueOf(raw.charAt(i))).styled(style -> style.withColor(rgb)));
        }
        return new R(out, index);
    }

    private R color(List<String> tokens, int index) {
        R color = getNextLiteral(tokens, index);
        int rgb = Integer.parseInt(color.text.getString(), 16);
        index = color.next;
        R text = getNextLiteral(tokens, index);
        index = text.next;
        return new R(text.text.styled(style -> style.withColor(rgb)), index);
    }

    private R dcolor(List<String> tokens, int index) {
        R color = getNextLiteral(tokens, index);
        int rgb = Integer.parseInt(color.text.getString(), 10);
        index = color.next;
        R text = getNextLiteral(tokens, index);
        index = text.next;
        return new R(text.text.styled(style -> style.withColor(rgb)), index);
    }

    private R hover(List<String> tokens, int index) {
        R hoverLiteral = getNextLiteral(tokens, index);
        MutableText hover = hoverLiteral.text;
        index = hoverLiteral.next;
        R textLiteral = getNextLiteral(tokens, index);
        index = textLiteral.next;
        return new R(textLiteral.text.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))), index);
    }

    private R click(List<String> tokens, int index) {
        R actionLiteral = getNextLiteral(tokens, index);
        ClickEvent.Action action = ClickEvent.Action.valueOf(actionLiteral.text.getString());
        index = actionLiteral.next;
        R valueLiteral = getNextLiteral(tokens, index);
        String value = valueLiteral.text.getString();
        index = valueLiteral.next;
        R textLiteral = getNextLiteral(tokens, index);
        index = textLiteral.next;
        return new R(textLiteral.text.styled(style -> style.withClickEvent(new ClickEvent(action, value))), index);
    }

    private R ranking(List<String> tokens, int index) {
        R value = getNextLiteral(tokens, index);
        index = value.next;
        String text = value.text.getString();
        String macro = text.equals("null") ? "" : " \\-{(}\\2{#%s}\\-{)}";
        return new R(parseMutableText(String.format(macro, text)), index);
    }

    private R nickname(List<String> tokens, int index) {
        R value = getNextLiteral(tokens, index);
        index = value.next;
        String nick = value.text.getString();
        String macro = nick.equals("null") ? "" : " \\-{(}\\i{\\3{%s}}\\-{)}";
        return new R(parseMutableText(String.format(macro, nick)), index);
    }

    private R pill(List<String> tokens, int index) {
        R bg = getNextLiteral(tokens, index);
        index = bg.next;
        R textColor = getNextLiteral(tokens, index);
        index = textColor.next;
        R textLiteral = getNextLiteral(tokens, index);
        index = textLiteral.next;
        String raw = textLiteral.text.getString();
        if (Objects.equals(raw, "null")) return new R(Text.empty(), index);
        return new R(parseMutableText(getPill(raw, bg.text.getString(), textColor.text.getString())), index);
    }

    private R pillBadge(List<String> tokens, Integer startIndex) {
        int index = startIndex;
        R bg = getNextLiteral(tokens, index);
        index = bg.next;
        R textColor = getNextLiteral(tokens, index);
        index = textColor.next;
        R badgeColor = getNextLiteral(tokens, index);
        index = badgeColor.next;
        R textLiteral = getNextLiteral(tokens, index);
        index = textLiteral.next;
        String raw = textLiteral.text.getString();
        if (Objects.equals(raw, "null")) return new R(Text.empty(), index);
        return new R(parseMutableText(getPillWithBadge(raw, bg.text.getString(), textColor.text.getString(), badgeColor.text.getString())), index);
    }

    private R styled(List<String> tokens, int index, UnaryOperator<Style> operator) {
        R textLiteral = getNextLiteral(tokens, index);
        return new R(textLiteral.text.styled(operator), textLiteral.next);
    }

    private R light(List<String> tokens, Integer index) {
        return styled(tokens, index, LIGHT);
    }

    private R normal(List<String> tokens, Integer index) {
        return styled(tokens, index, NORMAL);
    }

    private R dark(List<String> tokens, Integer index) {
        return styled(tokens, index, DARK);
    }

    private R accent1(List<String> tokens, Integer index) {
        return styled(tokens, index, ACCENT1);
    }

    private R accent2(List<String> tokens, Integer index) {
        return styled(tokens, index, ACCENT2);
    }

    private R accent3(List<String> tokens, Integer index) {
        return styled(tokens, index, ACCENT3);
    }

    private R italicize(List<String> tokens, Integer index) {
        return styled(tokens, index, ITALIC);
    }

    private R boldfont(List<String> tokens, Integer index) {
        return styled(tokens, index, BOLD);
    }

    private R underline(List<String> tokens, Integer index) {
        return styled(tokens, index, UNDERLINE);
    }

    private R strikethrough(List<String> tokens, Integer index) {
        return styled(tokens, index, STRIKETHROUGH);
    }

    private R obfuscated(List<String> tokens, Integer index) {
        return styled(tokens, index, OBFUSCATED);
    }

    private int getRgb(int pos, int len, int colors, List<Integer> palette) {
    if (colors == 1) return palette.get(0);
        float t = len == 1 ? 0 : (float) pos / (len - 1);
        float scaled = t * (colors - 1);
        int segment = Math.min((int) scaled, colors - 2);
        float localT = scaled - segment;
        int c0 = palette.get(segment);
        int c1 = palette.get(segment + 1);
        int r0 = (c0 >> 16) & 0xFF, g0 = (c0 >> 8) & 0xFF, b0 = c0 & 0xFF;
        int r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int r = Math.round(r0 + (r1 - r0) * localT);
        int g = Math.round(g0 + (g1 - g0) * localT);
        int b = Math.round(b0 + (b1 - b0) * localT);
        return (r << 16) | (g << 8) | b;
    }

    public String getPill(String text, String bgHex, String textHex) {
        String bg = colorCommand(bgHex);
        String fg = colorCommand(textHex);
        StringBuilder builder = new StringBuilder();
        builder.append(bg).append("{\ue010\u2064}");
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char unicode = getUnicodeChar(c);
                builder.append(bg).append("{\ue00f\ue012}")
                    .append(fg).append('{').append(unicode).append('}');
            }
        }
        builder.append(bg).append("{\ue011}");
        return builder.toString();
    }

    public String getPillWithBadge(String text, String bgHex, String textHex, String badgeHex) {
        String bg = colorCommand(bgHex);
        String fg = colorCommand(textHex);
        String badge = colorCommand(badgeHex);
        StringBuilder builder = new StringBuilder();
        builder.append(bg).append("{\ue010\u2064}");
        builder.append(bg).append("{\ue00f\ue012}")
            .append(badge).append("{\ueffa}");
        builder.append(bg).append("{\ue00f\ue012}");
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char unicode = getUnicodeChar(c);
                builder.append(bg).append("{\ue00f\ue012}")
                    .append(fg).append('{').append(unicode).append('}');
            }
        }
        builder.append(bg).append("{\ue011}");
        return builder.toString();
    }

    private char getUnicodeChar(char letter) {
        if (letter >= 'A' && letter <= 'Z') {
            return (char) ('\ue040' + (letter - 'A'));
        }
        if (letter >= 'a' && letter <= 'z') {
            return (char) ('\ue040' + (letter - 'a'));
        }
        return ' ';
    }

    private String colorCommand(String hex) {
        return String.format("\\color{%s}", hex);
    }

    private String serializeHoverText(HoverEvent hover) {
        if (hover.getAction() == HoverEvent.Action.SHOW_TEXT) {
            Text value = hover.getValue(HoverEvent.Action.SHOW_TEXT);
            if (value != null) {
                return toTeX(StyledText.fromComponent(value));
            }
        }
        return "<hover>";
    }

    private String escapeTeX(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.replace("\\", "\\\\").replace("{", "\\{").replace("}", "\\}");
    }
}
