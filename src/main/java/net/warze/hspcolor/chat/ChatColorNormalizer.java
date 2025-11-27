package net.warze.hspcolor.chat;

import com.wynntils.utils.colors.CustomColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ooo.sequoia.text.model.PartStyle;
import ooo.sequoia.text.model.StyledText;
import ooo.sequoia.text.model.StyledTextPart;

final class ChatColorNormalizer {
    private static final Map<Integer, Integer> COLOR_REMAP = Map.ofEntries(
        Map.entry(argb("#D4448C"), argb("#E985F7")),
        Map.entry(argb("#FDDD5C"), argb("#B8B8B8")),
        Map.entry(argb("#F3E6B2"), argb("#E6DA5E")),
        Map.entry(argb("#A0C84B"), argb("#DFDAB7")),
        Map.entry(argb("#FFD750"), argb("#FFF7F2")),
        Map.entry(argb("#BD45FF"), argb("#8684FA")),
        Map.entry(argb("#FAD9F7"), argb("#D8D8FA"))
    );

    StyledText apply(StyledText styled) {
        if (styled == null || styled.isEmpty()) return styled;
        List<StyledTextPart> parts = new ArrayList<>();
        boolean changed = false;
        for (StyledTextPart part : styled) {
            StyledTextPart updated = remap(part);
            if (updated != part) changed = true;
            parts.add(updated);
        }
        if (!changed) return styled;
        return StyledText.fromParts(parts);
    }

    private StyledTextPart remap(StyledTextPart part) {
        PartStyle style = part.getPartStyle();
        CustomColor color = style.getColor();
        if (color == CustomColor.NONE) return part;
        Integer replacement = COLOR_REMAP.get(color.asInt());
        if (replacement == null || replacement == color.asInt()) return part;
        CustomColor newColor = CustomColor.fromInt(replacement);
        return part.withStyle(original -> original.withColor(newColor));
    }

    private static int argb(String hex) {
        return CustomColor.fromHexString(hex).asInt();
    }
}
