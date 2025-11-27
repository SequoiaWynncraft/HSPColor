// Provided by Sequoia Mod
package ooo.sequoia.text.model;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.type.IterationDecision;
import com.wynntils.utils.type.Pair;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public final class StyledText implements Iterable<StyledTextPart> {
    private static final char POSITIVE_SPACE_HIGH_SURROGATE = '\udb00';
    private static final char NEGATIVE_SPACE_HIGH_SURROGATE = '\udaff';
    public static final StyledText EMPTY = new StyledText(List.of(), List.of(), List.of());
    private final List<StyledTextPart> parts;
    private final List<ClickEvent> clickEvents;
    private final List<HoverEvent> hoverEvents;

    private StyledText(List<StyledTextPart> parts, List<ClickEvent> clickEvents, List<HoverEvent> hoverEvents) {
        this.parts = parts.stream()
            .filter(part -> !part.isEmpty())
            .map(part -> new StyledTextPart(part, this))
            .collect(Collectors.toList());
        this.clickEvents = Collections.unmodifiableList(clickEvents);
        this.hoverEvents = Collections.unmodifiableList(hoverEvents);
    }

    public static StyledText fromComponent(Text component) {
        List<StyledTextPart> parts = new ArrayList<>();
        Deque<Pair<Text, Style>> deque = new LinkedList<>();
        deque.add(new Pair<>(component, Style.EMPTY));

        while (!deque.isEmpty()) {
            Pair<Text, Style> currentPair = deque.pop();
            Text current = currentPair.key();
            Style parentStyle = currentPair.value();
            String string = MutableText.of(current.getContent()).getString();
            List<StyledTextPart> styledParts = StyledTextPart.fromCodedString(string, current.getStyle(), null, parentStyle);
            Style childStyle = current.getStyle().withParent(parentStyle);
            List<Pair<Text, Style>> siblingPairs = current.getSiblings()
                .stream()
                .map(sibling -> new Pair<>(sibling, childStyle))
                .collect(Collectors.toList());
            Collections.reverse(siblingPairs);
            siblingPairs.forEach(deque::addFirst);
            parts.addAll(styledParts.stream().filter(part -> !part.isEmpty()).toList());
        }

        return fromParts(parts);
    }

    public static StyledText fromJson(JsonArray jsonArray) {
        return new StyledText(StyledTextPart.fromJson(jsonArray), List.of(), List.of());
    }

    public static StyledText fromString(String codedString) {
        return new StyledText(StyledTextPart.fromCodedString(codedString, Style.EMPTY, null, Style.EMPTY), List.of(), List.of());
    }

    public static StyledText fromModifiedString(String codedString, StyledText styledText) {
        List<HoverEvent> hoverEvents = List.copyOf(styledText.hoverEvents);
        List<ClickEvent> clickEvents = List.copyOf(styledText.clickEvents);
        return new StyledText(StyledTextPart.fromCodedString(codedString, Style.EMPTY, styledText, Style.EMPTY), clickEvents, hoverEvents);
    }

    public static StyledText fromUnformattedString(String unformattedString) {
        StyledTextPart part = new StyledTextPart(unformattedString, Style.EMPTY, null, Style.EMPTY);
        return new StyledText(List.of(part), List.of(), List.of());
    }

    public static StyledText fromPart(StyledTextPart part) {
        return fromParts(List.of(part));
    }

    public static StyledText fromParts(List<StyledTextPart> parts) {
        List<ClickEvent> clickEvents = new ArrayList<>();
        List<HoverEvent> hoverEvents = new ArrayList<>();
        for (StyledTextPart part : parts) {
            ClickEvent clickEvent = part.getPartStyle().getClickEvent();
            if (clickEvent != null && !clickEvents.contains(clickEvent)) clickEvents.add(clickEvent);
            HoverEvent hoverEvent = part.getPartStyle().getHoverEvent();
            if (hoverEvent != null && !hoverEvents.contains(hoverEvent)) hoverEvents.add(hoverEvent);
        }
        return new StyledText(parts, clickEvents, hoverEvents);
    }

    public String getString(PartStyle.StyleType type) {
        StringBuilder builder = new StringBuilder();
        PartStyle previous = null;
        for (StyledTextPart part : parts) {
            builder.append(part.getString(previous, type));
            previous = part.getPartStyle();
        }
        return builder.toString();
    }

    public String getString() {
        return getString(PartStyle.StyleType.DEFAULT);
    }

    public String getStringWithoutFormatting() {
        return getString(PartStyle.StyleType.NONE);
    }

    public MutableText getComponent() {
        if (parts.isEmpty()) return Text.empty();
        MutableText component = Text.empty();
        parts.forEach(part -> component.append(part.getComponent()));
        return component;
    }

    public int length() {
        return parts.stream().mapToInt(StyledTextPart::length).sum();
    }

    public int length(PartStyle.StyleType type) {
        return getString(type).length();
    }

    public static StyledText join(StyledText separator, StyledText... texts) {
        List<StyledTextPart> parts = new ArrayList<>();
        for (int i = 0; i < texts.length; i++) {
            parts.addAll(texts[i].parts);
            if (i != texts.length - 1) parts.addAll(separator.parts);
        }
        return fromParts(parts);
    }

    public static StyledText join(StyledText separator, Iterable<StyledText> texts) {
        return join(separator, Iterables.toArray(texts, StyledText.class));
    }

    public static StyledText join(String separator, StyledText... texts) {
        return join(fromString(separator), texts);
    }

    public static StyledText join(String separator, Iterable<StyledText> texts) {
        return join(fromString(separator), Iterables.toArray(texts, StyledText.class));
    }

    public static StyledText concat(StyledText... texts) {
        return fromParts(Arrays.stream(texts).map(text -> text.parts).flatMap(Collection::stream).toList());
    }

    public static StyledText concat(Iterable<StyledText> texts) {
        return concat(Iterables.toArray(texts, StyledText.class));
    }

    public StyledText getNormalized() {
        return fromParts(parts.stream().map(StyledTextPart::asNormalized).collect(Collectors.toList()));
    }

    public StyledText stripAlignment() {
        return iterate((part, buffer) -> {
            String text = part.getString(null, PartStyle.StyleType.NONE);
            if (text.indexOf(POSITIVE_SPACE_HIGH_SURROGATE) >= 0 || text.indexOf(NEGATIVE_SPACE_HIGH_SURROGATE) >= 0) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < text.length(); i++) {
                    char ch = text.charAt(i);
                    if (Character.isHighSurrogate(ch) && (ch == POSITIVE_SPACE_HIGH_SURROGATE || ch == NEGATIVE_SPACE_HIGH_SURROGATE)) {
                        if (i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1))) i++;
                    } else {
                        builder.append(ch);
                    }
                }
                buffer.set(0, new StyledTextPart(builder.toString(), part.getPartStyle().getStyle(), null, Style.EMPTY));
            }
            return IterationDecision.CONTINUE;
        });
    }

    public StyledText trim() {
        if (parts.isEmpty()) return this;
        List<StyledTextPart> newParts = new ArrayList<>(parts);
        newParts.set(0, newParts.getFirst().stripLeading());
        int last = newParts.size() - 1;
        newParts.set(last, newParts.get(last).stripTrailing());
        return fromParts(newParts);
    }

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    public boolean isBlank() {
        return parts.stream().allMatch(StyledTextPart::isBlank);
    }

    public boolean contains(String codedString) {
        return contains(codedString, PartStyle.StyleType.DEFAULT);
    }

    public boolean contains(StyledText styledText) {
        return contains(styledText.getString(PartStyle.StyleType.DEFAULT), PartStyle.StyleType.DEFAULT);
    }

    public boolean contains(String codedString, PartStyle.StyleType type) {
        return getString(type).contains(codedString);
    }

    public boolean contains(StyledText styledText, PartStyle.StyleType type) {
        return contains(styledText.getString(type), type);
    }

    public boolean startsWith(String codedString) {
        return startsWith(codedString, PartStyle.StyleType.DEFAULT);
    }

    public boolean startsWith(StyledText styledText) {
        return startsWith(styledText.getString(PartStyle.StyleType.DEFAULT), PartStyle.StyleType.DEFAULT);
    }

    public boolean startsWith(String codedString, PartStyle.StyleType type) {
        return getString(type).startsWith(codedString);
    }

    public boolean startsWith(StyledText styledText, PartStyle.StyleType type) {
        return startsWith(styledText.getString(type), type);
    }

    public boolean endsWith(String codedString) {
        return endsWith(codedString, PartStyle.StyleType.DEFAULT);
    }

    public boolean endsWith(StyledText styledText) {
        return endsWith(styledText.getString(PartStyle.StyleType.DEFAULT), PartStyle.StyleType.DEFAULT);
    }

    public boolean endsWith(String codedString, PartStyle.StyleType type) {
        return getString(type).endsWith(codedString);
    }

    public boolean endsWith(StyledText styledText, PartStyle.StyleType type) {
        return endsWith(styledText.getString(type), type);
    }

    public Matcher getMatcher(Pattern pattern) {
        return getMatcher(pattern, PartStyle.StyleType.DEFAULT);
    }

    public Matcher getMatcher(Pattern pattern, PartStyle.StyleType type) {
        return pattern.matcher(getString(type));
    }

    public boolean matches(Pattern pattern) {
        return matches(pattern, PartStyle.StyleType.DEFAULT);
    }

    public boolean matches(Pattern pattern, PartStyle.StyleType type) {
        return pattern.matcher(getString(type)).matches();
    }

    public boolean find(Pattern pattern) {
        return find(pattern, PartStyle.StyleType.DEFAULT);
    }

    public boolean find(Pattern pattern, PartStyle.StyleType type) {
        return pattern.matcher(getString(type)).find();
    }

    public StyledText append(StyledText styledText) {
        return concat(this, styledText);
    }

    public StyledText append(String codedString) {
        return append(fromString(codedString));
    }

    public StyledText appendPart(StyledTextPart part) {
        List<StyledTextPart> newParts = new ArrayList<>(parts);
        newParts.add(part);
        return fromParts(newParts);
    }

    public StyledText prepend(StyledText styledText) {
        return concat(styledText, this);
    }

    public StyledText prepend(String codedString) {
        return prepend(fromString(codedString));
    }

    public StyledText prependPart(StyledTextPart part) {
        List<StyledTextPart> newParts = new ArrayList<>(parts);
        newParts.addFirst(part);
        return fromParts(newParts);
    }

    public StyledText[] split(String regex) {
        return split(regex, false);
    }

    public StyledText[] split(String regex, boolean keepTrailingEmpty) {
        if (parts.isEmpty()) return new StyledText[]{EMPTY};
        Pattern pattern = Pattern.compile(regex);
        List<StyledText> splitTexts = new ArrayList<>();
        List<StyledTextPart> buffer = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            StyledTextPart part = parts.get(i);
            String partString = part.getString(null, PartStyle.StyleType.NONE);
            int maxSplit = !keepTrailingEmpty && i == parts.size() - 1 ? 0 : -1;
            List<String> stringParts = Arrays.stream(pattern.split(partString, maxSplit)).toList();
            Matcher matcher = pattern.matcher(partString);
            if (matcher.find()) {
                for (int j = 0; j < stringParts.size(); j++) {
                    buffer.add(new StyledTextPart(stringParts.get(j), part.getPartStyle().getStyle(), null, Style.EMPTY));
                    if (j != stringParts.size() - 1) {
                        splitTexts.add(fromParts(buffer));
                        buffer.clear();
                    }
                }
            } else {
                buffer.add(part);
            }
        }
        if (!buffer.isEmpty()) splitTexts.add(fromParts(buffer));
        return splitTexts.toArray(StyledText[]::new);
    }

    public StyledText substring(int beginIndex) {
        return substring(beginIndex, length(), PartStyle.StyleType.NONE);
    }

    public StyledText substring(int beginIndex, PartStyle.StyleType type) {
        return substring(beginIndex, length(type), type);
    }

    public StyledText substring(int beginIndex, int endIndex) {
        return substring(beginIndex, endIndex, PartStyle.StyleType.NONE);
    }

    public StyledText substring(int beginIndex, int endIndex, PartStyle.StyleType type) {
        if (endIndex < beginIndex) throw new IndexOutOfBoundsException("endIndex must be greater than beginIndex");
        if (beginIndex < 0) throw new IndexOutOfBoundsException("beginIndex must be >= 0");
        if (endIndex > length(type)) throw new IndexOutOfBoundsException("endIndex must be <= length(type)");
        List<StyledTextPart> included = new ArrayList<>();
        int currentIndex = 0;
        PartStyle previous = null;
        for (StyledTextPart part : parts) {
            int partLength = part.getString(previous, type).length();
            if (currentIndex >= beginIndex && currentIndex + partLength < endIndex) {
                included.add(part);
            } else if (MathUtils.rangesIntersect(currentIndex, currentIndex + partLength, beginIndex, endIndex - 1)) {
                int startInPart = Math.max(0, beginIndex - currentIndex);
                int endInPart = Math.min(partLength, endIndex - currentIndex);
                String fullString = part.getString(previous, type);
                String before = fullString.substring(0, startInPart);
                String slice = fullString.substring(startInPart, endInPart);
                if (before.endsWith("§") || slice.endsWith("§")) throw new IllegalArgumentException("Substring splits formatting code");
                included.addAll(StyledTextPart.fromCodedString(slice, part.getPartStyle().getStyle(), null, Style.EMPTY));
            }
            currentIndex += part.getString(previous, type).length();
            previous = part.getPartStyle();
        }
        return fromParts(included);
    }

    public StyledText[] partition(int... indexes) {
        return partition(PartStyle.StyleType.NONE, indexes);
    }

    public StyledText[] partition(PartStyle.StyleType type, int... indexes) {
        if (indexes.length == 0) return new StyledText[]{this};
        List<StyledText> segments = new ArrayList<>();
        int current = 0;
        for (int index : indexes) {
            if (index < current) throw new IllegalArgumentException("Indexes must be ascending");
            segments.add(substring(current, index, type));
            current = index;
        }
        segments.add(substring(current, type));
        return segments.toArray(StyledText[]::new);
    }

    public StyledText replaceFirst(String regex, String replacement) {
        return replaceFirst(Pattern.compile(regex), replacement);
    }

    public StyledText replaceFirst(Pattern pattern, String replacement) {
        List<StyledTextPart> newParts = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            StyledTextPart part = parts.get(i);
            String partString = part.getString(null, PartStyle.StyleType.NONE);
            Matcher matcher = pattern.matcher(partString);
            if (matcher.find()) {
                String replaced = matcher.replaceFirst(replacement);
                newParts.add(new StyledTextPart(replaced, part.getPartStyle().getStyle(), null, Style.EMPTY));
                newParts.addAll(parts.subList(i + 1, parts.size()));
                break;
            }
            newParts.add(part);
        }
        return fromParts(newParts);
    }

    public StyledText replaceAll(String regex, String replacement) {
        return replaceAll(Pattern.compile(regex), replacement);
    }

    public StyledText replaceAll(Pattern pattern, String replacement) {
        List<StyledTextPart> newParts = new ArrayList<>();
        for (StyledTextPart part : parts) {
            String partString = part.getString(null, PartStyle.StyleType.NONE);
            Matcher matcher = pattern.matcher(partString);
            if (matcher.find()) {
                String replaced = matcher.replaceAll(replacement);
                newParts.add(new StyledTextPart(replaced, part.getPartStyle().getStyle(), null, Style.EMPTY));
            } else {
                newParts.add(part);
            }
        }
        return fromParts(newParts);
    }

    public StyledText[] getPartsAsTextArray() {
        return parts.stream().map(StyledText::fromPart).toArray(StyledText[]::new);
    }

    public StyledText iterate(BiFunction<StyledTextPart, List<StyledTextPart>, IterationDecision> function) {
        List<StyledTextPart> newParts = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            StyledTextPart part = parts.get(i);
            List<StyledTextPart> functionParts = new ArrayList<>();
            functionParts.add(part);
            IterationDecision decision = function.apply(part, functionParts);
            newParts.addAll(functionParts);
            if (decision == IterationDecision.BREAK) {
                newParts.addAll(parts.subList(i + 1, parts.size()));
                break;
            }
        }
        return fromParts(newParts);
    }

    public StyledText iterateBackwards(BiFunction<StyledTextPart, List<StyledTextPart>, IterationDecision> function) {
        List<StyledTextPart> newParts = new ArrayList<>();
        for (int i = parts.size() - 1; i >= 0; i--) {
            StyledTextPart part = parts.get(i);
            List<StyledTextPart> functionParts = new ArrayList<>();
            functionParts.add(part);
            IterationDecision decision = function.apply(part, functionParts);
            newParts.addAll(0, functionParts);
            if (decision == IterationDecision.BREAK) {
                newParts.addAll(0, parts.subList(0, i));
                break;
            }
        }
        return fromParts(newParts);
    }

    public StyledText map(Function<StyledTextPart, StyledTextPart> mapper) {
        return fromParts(parts.stream().map(mapper).collect(Collectors.toList()));
    }

    public StyledText withoutFormatting() {
        return iterate((part, buffer) -> {
            buffer.set(0, new StyledTextPart(part.getString(null, PartStyle.StyleType.NONE), Style.EMPTY, null, Style.EMPTY));
            return IterationDecision.CONTINUE;
        });
    }

    public boolean equalsString(String raw) {
        return equalsString(raw, PartStyle.StyleType.DEFAULT);
    }

    public boolean equalsString(String raw, PartStyle.StyleType type) {
        return getString(type).equals(raw);
    }

    public StyledTextPart getFirstPart() {
        return parts.isEmpty() ? null : parts.getFirst();
    }

    public StyledTextPart getLastPart() {
        return parts.isEmpty() ? null : parts.getLast();
    }

    public int getPartCount() {
        return parts.size();
    }

    int getClickEventIndex(ClickEvent clickEvent) {
        for (int i = 0; i < clickEvents.size(); i++) {
            if (clickEvents.get(i).equals(clickEvent)) return i + 1;
        }
        return -1;
    }

    ClickEvent getClickEvent(int index) {
        return Iterables.get(clickEvents, index - 1, null);
    }

    int getHoverEventIndex(HoverEvent hoverEvent) {
        for (int i = 0; i < hoverEvents.size(); i++) {
            if (hoverEvents.get(i).equals(hoverEvent)) return i + 1;
        }
        return -1;
    }

    HoverEvent getHoverEvent(int index) {
        return Iterables.get(hoverEvents, index - 1, null);
    }

    private StyledTextPart getPartBefore(StyledTextPart part) {
        int index = parts.indexOf(part);
        return index == 0 ? null : parts.get(index - 1);
    }

    @Override
    public Iterator<StyledTextPart> iterator() {
        return parts.iterator();
    }

    @Override
    public String toString() {
        return "StyledText{'" + getString(PartStyle.StyleType.INCLUDE_EVENTS) + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StyledText that)) return false;
        return Objects.deepEquals(parts, that.parts)
            && Objects.deepEquals(clickEvents, that.clickEvents)
            && Objects.deepEquals(hoverEvents, that.hoverEvents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parts, clickEvents, hoverEvents);
    }

    public static class StyledTextSerializer implements JsonSerializer<StyledText>, JsonDeserializer<StyledText> {
        @Override
        public StyledText deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return StyledText.fromString(json.getAsString());
        }

        @Override
        public JsonElement serialize(StyledText src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.getString());
        }
    }
}
