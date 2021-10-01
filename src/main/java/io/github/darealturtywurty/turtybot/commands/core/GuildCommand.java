package io.github.darealturtywurty.turtybot.commands.core;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public interface GuildCommand {

    default List<String> blacklistChannels() {
        return new ArrayList<>();
    }

    CommandCategory getCategory();

    default long getCooldownMillis() {
        return 0L;
    }

    String getDescription();

    String getName();

    List<OptionData> getOptions();

    default List<SubcommandData> getSubcommandData() {
        return List.of();
    }

    default List<SubcommandGroupData> getSubcommandGroupData() {
        return List.of();
    }

    void handle(CoreCommandContext ctx);

    default boolean isBoosterOnly() {
        return false;
    }

    default boolean isBotOwnerOnly() {
        return false;
    }

    default boolean isDevelopmentOnly() {
        return false;
    }

    default boolean isModeratorOnly() {
        return false;
    }

    default boolean isNSFW() {
        return false;
    }

    default boolean productionReady() {
        return false;
    }

    default List<String> whitelistChannels() {
        return new ArrayList<>();
    }
}
