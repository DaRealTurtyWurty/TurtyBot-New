package io.github.darealturtywurty.turtybot.commands.fun;

import java.time.Instant;
import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;

public class CatImageCommand implements IGuildCommand {

	@Override
	public List<String> getAliases() {
		return List.of("catimage", "cat_image", "catimg", "cat_img", "cat-img", "cat-image");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public String getDescription() {
		return "Random cat image.";
	}

	@Override
	public String getName() {
		return "cat";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var embed = new EmbedBuilder();
		embed.setColor(BotUtils.generateRandomPastelColor());
		embed.setTimestamp(Instant.now());
		embed.setTitle("Cat 🐱");
		final String randQuery = "?" + Constants.RANDOM.nextInt() + "=" + Constants.RANDOM.nextInt();
		embed.setImage("https://cataas.com/cat" + randQuery);
		ctx.getMessage().replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
	}
}
