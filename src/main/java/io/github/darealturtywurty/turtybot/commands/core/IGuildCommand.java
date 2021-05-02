package io.github.darealturtywurty.turtybot.commands.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.internal.utils.tuple.Pair;

public interface IGuildCommand {
	void handle(CommandContext ctx);

	String getName();

	String getDescription();

	default List<String> getAliases() {
		return Arrays.asList();
	}

	default Pair<Boolean, List<String>> validChannels() {
		return Pair.of(false, new ArrayList<>());
	}

	default Pair<Boolean, List<String>> validCategories() {
		return Pair.of(false, new ArrayList<>());
	}

	default boolean isNSFW() {
		return false;
	}

	default boolean shouldDeleteOriginal() {
		return true;
	}

	default boolean isModeratorOnly() {
		return false;
	}

	default boolean isOwnerOnly() {
		return false;
	}

	default boolean isBoosterOnly() {
		return false;
	}

	default boolean shouldPrivateMessage() {
		return false;
	}

	default boolean isDevelopmentOnly() {
		return false;
	}
}
