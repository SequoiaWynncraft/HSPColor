package net.warze.hspcolor.chat;

import net.minecraft.text.MutableText;
import ooo.sequoia.text.model.StyledText;
import net.warze.hspcolor.config.HspColorConfig;
import net.warze.hspcolor.server.ServerContext;

public final class ChatMessageInterceptor {
    private final ServerContext serverContext;
    private final HspColorConfig config;
    private final GuildMessageDetector detector = new GuildMessageDetector();
    private final GuildMessageFormatter formatter;
    private final ChatColorNormalizer colorNormalizer = new ChatColorNormalizer();

    public ChatMessageInterceptor(ServerContext serverContext, HspColorConfig config) {
        this.serverContext = serverContext;
        this.config = config;
        this.formatter = new GuildMessageFormatter(() -> this.config.stripGuildRankPill);
    }

    public MutableText rewrite(MutableText message) {
        if (!serverContext.isOnWynncraft()) return message;
        StyledText styled = StyledText.fromComponent(message);
        StyledText normalized = config.colorNormalizer ? colorNormalizer.apply(styled) : styled;
        if (!config.guildFormatter) {
            return normalized == styled ? message : normalized.getComponent();
        }
        GuildMessageDetector.Detection detection = detector.detect(normalized);
        if (detection == null) return normalized.getComponent();
        return formatter.apply(detection);
    }
}
