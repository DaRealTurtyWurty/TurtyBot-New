package io.github.darealturtywurty.turtybot.commands.core;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface GuildCommand {

    default List<String> blacklistChannels() {
        return new ArrayList<>();
    }

    CommandCategory getCategory();

    String getDescription();

    String getName();

    List<OptionData> getOptions();

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

    default List<String> whitelistChannels() {
        return new ArrayList<>();
    }
}
