package io.github.darealturtywurty.turtybot.commands.fun;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;

public class EightBallCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public String getDescription() {
		return "Answers your question to what the gods of random think.";
	}

	@Override
	public String getName() {
		return "8ball";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getArgs().length < 1) {
			ctx.getMessage().reply("You must supply the message that you want the 8ball to answer!")
					.mentionRepliedUser(false).queue();
			return;
		}

		final String args = String.join(" ", ctx.getArgs());
		try {
			final URLConnection urlc = new URL(
					"https://8ball.delegator.com/magic/JSON/" + URLEncoder.encode(args, StandardCharsets.UTF_8))
							.openConnection();
			urlc.addRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			final String result = IOUtils.toString(new BufferedReader(new InputStreamReader(urlc.getInputStream())));
			urlc.getInputStream().close();
			final JsonObject magicObj = Constants.GSON.fromJson(result, JsonObject.class).get("magic").getAsJsonObject();

			final var embed = new EmbedBuilder();
			var color = Color.BLACK;
			switch (magicObj.get("type").getAsString().toLowerCase()) {
			case "affirmative":
				color = Color.GREEN;
				break;
			case "neutral":
				color = Color.BLUE;
				break;
			case "contrary":
				color = Color.RED;
				break;
			default:
				break;
			}

			embed.setColor(color);
			embed.setTimestamp(Instant.now());
			embed.setTitle("8Ball 🎱");
			embed.setDescription(magicObj.get("answer").getAsString());
			ctx.getMessage().replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
		} catch (final IOException e) {
			ctx.getMessage().reply("There was an issue accessing the database.").mentionRepliedUser(false).queue();
		}
	}

}
