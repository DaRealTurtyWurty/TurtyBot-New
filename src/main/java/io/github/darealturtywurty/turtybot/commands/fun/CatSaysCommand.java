package io.github.darealturtywurty.turtybot.commands.fun;

import java.time.Instant;
import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;

public class CatSaysCommand implements IGuildCommand {

	@Override
	public List<String> getAliases() {
		return List.of("catsay", "catsaid");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public String getDescription() {
		return "Random cat image with the supplied text";
	}

	@Override
	public String getName() {
		return "catsays";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getArgs().length < 1) {
			ctx.getMessage().reply("You must supply the message that you want the cat to say!").mentionRepliedUser(false)
					.queue();
			return;
		}

		final String args = String.join(" ", ctx.getArgs());
		final var embed = new EmbedBuilder();
		embed.setColor(BotUtils.generateRandomPastelColor());
		embed.setTimestamp(Instant.now());
		embed.setTitle("Cat says: `" + args + "`");
		final String randQuery = "?" + Constants.RANDOM.nextInt() + "=" + Constants.RANDOM.nextInt();
		embed.setImage("https://cataas.com/cat/says/" + args + randQuery);
		ctx.getMessage().replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
	}
}
