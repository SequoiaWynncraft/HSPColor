package net.warze.hspcolor.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.warze.hspcolor.HspColor;

public final class HspColorConfigScreenFactory {
    public Screen build(Screen parent) {
        HspColorConfig config = HspColor.instance().config();
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("config.hspcolor.title"));
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("config.hspcolor.category.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.hspcolor.option.resource_pack_updates"), config.resourcePackUpdates)
            .setSaveConsumer(value -> config.resourcePackUpdates = value)
            .setDefaultValue(true)
            .build());
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.hspcolor.option.mod_updates"), config.modUpdates)
            .setSaveConsumer(value -> config.modUpdates = value)
            .setDefaultValue(true)
            .build());
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.hspcolor.option.guild_formatter"), config.guildFormatter)
            .setSaveConsumer(value -> config.guildFormatter = value)
            .setDefaultValue(true)
            .build());
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.hspcolor.option.strip_guild_rank_pill"), config.stripGuildRankPill)
            .setSaveConsumer(value -> config.stripGuildRankPill = value)
            .setDefaultValue(false)
            .build());
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.hspcolor.option.color_normalizer"), config.colorNormalizer)
            .setSaveConsumer(value -> config.colorNormalizer = value)
            .setDefaultValue(true)
            .build());
        builder.setSavingRunnable(() -> HspColor.instance().saveConfig());
        return builder.build();
    }
}
