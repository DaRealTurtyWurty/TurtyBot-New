package io.github.darealturtywurty.turtybot.commands.fun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class AdviceCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		try {
			URLConnection urlc = new URL("https://api.adviceslip.com/advice").openConnection();
			urlc.addRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			String result = new BufferedReader(new InputStreamReader(urlc.getInputStream())).readLine();
			urlc.getInputStream().close();
			ctx.getMessage().reply(result.split("\"")[7]).mentionRepliedUser(false).queue();
		} catch (IOException e) {
			ctx.getMessage().reply("There was an issue accessing the advice database.").mentionRepliedUser(false).queue();
		}
	}

	@Override
	public String getName() {
		return "advice";
	}

	@Override
	public String getDescription() {
		return "Gets some pro advice.";
	}
}
