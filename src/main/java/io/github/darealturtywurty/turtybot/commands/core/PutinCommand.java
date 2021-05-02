package io.github.darealturtywurty.turtybot.commands.core;

import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;

public class PutinCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		var embed = new EmbedBuilder();
		embed.setTitle("The Russian Lord");
		embed.setColor(0xFF3C86);
		embed.setImage("https://media.tenor.com/images/1f70e6cb05211bc481af145bfe67bc64/tenor.gif");
		ctx.getChannel().sendMessage(embed.build()).queue();

		ctx.getMessage().delete().queue();
	}

	@Override
	public String getName() {
		return "putin";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("russian_lord", "russian-lord", "wide");
	}

	@Override
	public String getDescription() {
		return "The most important command of them all!";
	}

	@Override
	public boolean isBoosterOnly() {
		return true;
	}
}
