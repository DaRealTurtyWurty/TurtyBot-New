package io.github.darealturtywurty.turtybot.commands.fun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;

public class DogImageCommand implements IGuildCommand {

	@Override
	public List<String> getAliases() {
		return List.of("dogimage", "dog_image", "dogimg", "dog_img", "dog-img", "dog-image");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public String getDescription() {
		return "Random dog image.";
	}

	@Override
	public String getName() {
		return "dog";
	}

	@Override
	public void handle(final CommandContext ctx) {
		try {
			final URLConnection urlc = new URL("https://dog.ceo/api/breeds/image/random").openConnection();
			urlc.addRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			final String result = IOUtils.toString(new BufferedReader(new InputStreamReader(urlc.getInputStream())));
			urlc.getInputStream().close();

			final var embed = new EmbedBuilder();
			embed.setColor(BotUtils.generateRandomPastelColor());
			embed.setTimestamp(Instant.now());
			embed.setTitle("Dog 🐶");
			embed.setImage(Constants.GSON.fromJson(result, JsonObject.class).get("message").getAsString());
			ctx.getMessage().replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
		} catch (final IOException e) {
			ctx.getMessage().reply("There was an issue accessing the database.").mentionRepliedUser(false).queue();
		}
	}
}
