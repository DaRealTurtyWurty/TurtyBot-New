package io.github.darealturtywurty.turtybot.commands.minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;

public class MCNameHistory implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MINECRAFT;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getName() {
		return "mcnamehistory";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getArgs().length < 1) {
			ctx.getMessage().reply("You must specify the UUID of your Minecraft account!").mentionRepliedUser(false).queue();
			return;
		}

		final String text = URLEncoder.encode(String.join(" ", ctx.getArgs()), StandardCharsets.UTF_8);
		try {
			final URLConnection urlc = new URL("https://api.mojang.com/user/profiles/" + text + "/names").openConnection();
			urlc.addRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			final String result = IOUtils.toString(new BufferedReader(new InputStreamReader(urlc.getInputStream())));
			final JsonArray array = Constants.GSON.fromJson(result, JsonArray.class).getAsJsonArray();

			final var embed = new EmbedBuilder();
			embed.setTitle("Minecraft Name History for user: " + array.get(0).getAsJsonObject().get("name").getAsString());
			embed.setTimestamp(Instant.now());
			embed.setColor(ctx.getMember().getColorRaw());
			StreamSupport
					.stream(array.spliterator(),
							false)
					.map(JsonElement::getAsJsonObject)
					.forEachOrdered(object -> embed.addField("1.",
							"Name: " + object.get("name").getAsString()
									+ (object.has("changedToAt")
											? "\n" + DateFormat.getDateTimeInstance()
													.format(new Date(object.get("changedToAt").getAsLong()))
											: ""),
							false));
			ctx.getMessage().replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
		} catch (final IOException e) {
			ctx.getMessage().reply("There was an issue getting the name history for this user!").mentionRepliedUser(false)
					.queue();
			System.out.println(e);
		}
	}
}
