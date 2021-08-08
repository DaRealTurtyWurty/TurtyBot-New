package io.github.darealturtywurty.turtybot.commands.fun;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class UpsideDownTextCommand implements IGuildCommand {

	private static final String NORMAL_CHARS = "abcdefghijklmnopqrstuvwxyz_,;.?!/\\'";
	private static final String UPSIDEDOWN_CHARS = "ɐqɔpǝɟbɥıظʞןɯuodbɹsʇnʌʍxʎz‾'؛˙¿¡/\\\\,";

	@Override
	public List<String> getAliases() {
		return List.of("upsidedown-text", "upsidedown_text");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public String getDescription() {
		return "Makes the input text upside-down.";
	}

	@Override
	public String getName() {
		return "upsidedowntext";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getArgs().length < 1) {
			ctx.getMessage().reply("You must supply the text that you want to make upside-down!").mentionRepliedUser(false)
					.queue();
			return;
		}

		final String text = String.join(" ", ctx.getArgs());
		final var newText = new StringBuilder();
		for (int charIndex = 0; charIndex < text.length(); charIndex++) {
			final char letter = text.charAt(charIndex);
			final int normalIndex = NORMAL_CHARS.indexOf(letter);
			newText.append(normalIndex != -1 ? UPSIDEDOWN_CHARS.charAt(normalIndex) : letter);
		}
		ctx.getMessage().reply(newText.toString()).mentionRepliedUser(false).queue();
	}
}