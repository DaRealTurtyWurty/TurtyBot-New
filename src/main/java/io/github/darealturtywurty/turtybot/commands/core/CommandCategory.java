package io.github.darealturtywurty.turtybot.commands.core;

import java.util.Optional;
import java.util.stream.Stream;

public enum CommandCategory {

    FUN("Fun", "🎉"), MINECRAFT("Minecraft", "🧊"), MODERATION("Moderation", "🔨"), MUSIC("Music", "🎵"),
    UTILITY("Utility", "🧰"), RANDOM("Random", "🛋"), NSFW("NSFW", "😏");

    public final String emoji, name;

    CommandCategory(final String name, final String emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    public static Optional<CommandCategory> byName(final String name) {
        return Stream.of(CommandCategory.values())
                .filter(category -> category.name.equalsIgnoreCase(name.trim())).findFirst();
    }

    @Override
    public String toString() {
        return this.name;
    }
}
