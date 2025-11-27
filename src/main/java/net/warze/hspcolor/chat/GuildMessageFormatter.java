package net.warze.hspcolor.chat;

import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import ooo.sequoia.text.parser.TeXParser;
import ooo.sequoia.text.model.StyledText;
import ooo.sequoia.text.model.StyledTextPart;

public final class GuildMessageFormatter {
    private final TeXParser parser = new TeXParser();
    private static final Pattern USERNAME_PATTERN = Pattern.compile("(?i)([a-z0-9_]{1,16})$");
    private final BooleanSupplier stripPillSupplier;
    private static final int PILL_TEXT_COLOR = 0x000000;

    public GuildMessageFormatter() {
        this(() -> false);
    }

    public GuildMessageFormatter(BooleanSupplier stripPillSupplier) {
        this.stripPillSupplier = stripPillSupplier == null ? () -> false : stripPillSupplier;
    }

    public MutableText apply(GuildMessageDetector.Detection detection) {
        StyledText styled = detection.styled();
        String replacementText = buildReplacement(detection);
        if (replacementText == null) {
            return styled.getComponent();
        }
        StyledText prefix = styled.substring(0, detection.visibleStart());
        StyledText suffix = styled.substring(detection.visibleEnd());
        StyledText original = styled.substring(detection.visibleStart(), detection.visibleEnd());
        MutableText replacement = parser.parseMutableText(replacementText);
        Style preserved = extractInteractiveStyle(original);
        applyInteractiveStyle(replacement, preserved);
        MutableText result = Text.empty();
        result.append(prefix.getComponent());
        result.append(replacement);
        result.append(suffix.getComponent());
        return result;
    }

    private String buildReplacement(GuildMessageDetector.Detection detection) {
        GuildRank rank = detection.rank();
        String plain = stripFormatting(detection.visible());
        String name = removeRankTokens(plain, rank).trim();
        if (name.isEmpty()) name = plain.trim();
        name = extractUsername(name);
        if (name.isEmpty()) return null;
        String coloredName = colorize(rank.nameColor(), parser.sanitize(name));
        if (stripPillSupplier.getAsBoolean()) {
            return coloredName;
        }
        String pill = buildPill(rank);
        if (pill == null) return null;
        return pill + " " + coloredName;
    }

    private String buildPill(GuildRank rank) {
        String label = rank.plainName();
        if (label == null || label.isEmpty()) return null;
        String bg = toHex(rank.roleColor());
        String text = toHex(PILL_TEXT_COLOR);
        return "\\pillbadge{" + bg + "}{" + text + "}{" + text + "}{" + parser.sanitize(label) + "}";
    }

    private String extractUsername(String text) {
        Matcher matcher = USERNAME_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        int idx = text.lastIndexOf(' ');
        if (idx >= 0 && idx + 1 < text.length()) {
            return text.substring(idx + 1);
        }
        return text;
    }

    private String colorize(int rgb, String body) {
        return "\\color{" + toHex(rgb) + "}{" + body + "}";
    }

    private String toHex(int rgb) {
        return String.format("%06X", rgb & 0xFFFFFF);
    }

    private String removeRankTokens(String text, GuildRank rank) {
        String sanitized = text;
        String legacy = rank.normalizedLegacyToken();
        if (!legacy.isEmpty()) sanitized = sanitized.replace(legacy, "");
        String display = rank.normalizedDisplayToken();
        if (!display.isEmpty()) sanitized = sanitized.replace(display, "");
        return sanitized;
    }

    private String stripFormatting(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        boolean skip = false;
        for (int i = 0; i < text.length();) {
            char ch = text.charAt(i);
            if (skip) {
                skip = false;
                i++;
                continue;
            }
            if (ch == '§') {
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

    private Style extractInteractiveStyle(StyledText fragment) {
        Style fallback = Style.EMPTY;
        for (StyledTextPart part : fragment) {
            Style style = part.getPartStyle().getStyle();
            if (style.equals(Style.EMPTY)) continue;
            if (fallback.equals(Style.EMPTY)) fallback = style;
            if (style.getHoverEvent() != null || style.getClickEvent() != null || style.getInsertion() != null) {
                return style;
            }
        }
        return fallback;
    }

    private void applyInteractiveStyle(MutableText text, Style interactive) {
        if (interactive == Style.EMPTY) return;
        mergeStyle(text, interactive);
        for (Text sibling : text.getSiblings()) {
            if (sibling instanceof MutableText mutable) {
                applyInteractiveStyle(mutable, interactive);
            }
        }
    }

    private void mergeStyle(MutableText text, Style interactive) {
        Style merged = text.getStyle();
        if (interactive.getHoverEvent() != null) merged = merged.withHoverEvent(interactive.getHoverEvent());
        if (interactive.getClickEvent() != null) merged = merged.withClickEvent(interactive.getClickEvent());
        if (interactive.getInsertion() != null) merged = merged.withInsertion(interactive.getInsertion());
        if (interactive.isBold()) merged = merged.withBold(true);
        if (interactive.isItalic()) merged = merged.withItalic(true);
        if (interactive.isUnderlined()) merged = merged.withUnderline(true);
        if (interactive.isStrikethrough()) merged = merged.withStrikethrough(true);
        if (interactive.isObfuscated()) merged = merged.withObfuscated(true);
        text.setStyle(merged);
    }
}
