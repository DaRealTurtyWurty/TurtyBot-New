package io.github.darealturtywurty.turtybot.commands.minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;

public class MojangStatusCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		try {
			URLConnection urlc = new URL("https://status.mojang.com/check").openConnection();
			urlc.addRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			var reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));

			var strBuilder = new StringBuilder();
			String arg = reader.readLine();
			while (!arg.contains("boi")) {
				if (!strBuilder.toString().contains(arg)) {
					strBuilder.append(arg);
				} else {
					break;
				}
			}

			if (!strBuilder.toString().isBlank()) {
				String service, status = "";
				StringBuilder msg = new StringBuilder();
				String[] services = strBuilder.toString().replace("{", "").replace("}", "").replace("[", "").replace("]", "")
						.replace("\"", "").split(",");
				for (String str : services) {
					service = str.split(":")[0];
					status = str.split(":")[1];
					if (status.equalsIgnoreCase("green")) {
						status = "Green (No Issues!)";
					} else if (status.equalsIgnoreCase("yellow")) {
						status = "Yellow (Some Issues!)";
					} else if (status.equalsIgnoreCase("red")) {
						status = "Red (Service Unavailable!)";
					} else {
						status = "Unknown";
					}
					msg.append("https://" + service + " 's status is: " + status + "\n");
				}
				ctx.getMessage().reply(new EmbedBuilder().setTitle("Mojang Service Status")
						.setDescription(msg.toString().trim()).setColor(BotUtils.generateRandomColor()).build())
						.mentionRepliedUser(false).queue();
			}
		} catch (IOException | IllegalArgumentException e) {
			ctx.getMessage()
					.reply("There has been an error processing this command! Please report this to the server owner.")
					.mentionRepliedUser(false).queue();
		}
	}

	@Override
	public String getName() {
		return "mojangstatus";
	}

	@Override
	public String getDescription() {
		return "Gets the current status of the mojang servers.";
	}

}
