package io.github.darealturtywurty.turtybot.commands.minecraft;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import net.dv8tion.jda.api.EmbedBuilder;

public class UserUUIDCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		if (ctx.getArgs().length >= 1) {
			var builder = new EmbedBuilder();
			try {
				var jsonText = readJsonFromUrl("https://api.mojang.com/users/profiles/minecraft/" + ctx.getArgs()[0])
						.toString();
				builder.setTitle("Minecraft User:");
				builder.setDescription(jsonText.split("\"")[3]);
				builder.addField("UUID:", jsonText.split("\"")[7], true);
				ctx.getMessage().reply(builder.build()).mentionRepliedUser(false).queue();
			} catch (JsonParseException | IOException e) {
				builder.setTitle("Invalid User");
				builder.setColor(Color.decode("#EA2027"));
				builder.setDescription("A user with the name of " + ctx.getArgs()[0] + " does not exist.");
				builder.addField("Try again with a valid username!", "", true);
				ctx.getMessage().reply(builder.build()).mentionRepliedUser(false)
						.queue(msg -> msg.delete().queueAfter(15, TimeUnit.SECONDS));
			}
		} else {
			ctx.getMessage().reply("You must supply the username of the user that you want to get the UUID for!").queue();
		}
	}

	@Override
	public String getName() {
		return "useruuid";
	}

	@Override
	public String getDescription() {
		return "Gets the UUID of a minecraft user.";
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JsonElement readJsonFromUrl(String url) throws IOException, JsonParseException, JsonSyntaxException {
		InputStream is = new URL(url).openStream();
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String jsonText = readAll(rd);
			return JsonParser.parseString(jsonText);
		} finally {
			is.close();
		}
	}

}
