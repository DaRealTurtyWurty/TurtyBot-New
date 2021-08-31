package io.github.darealturtywurty.turtybot.commands.minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class MojangStatusCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MINECRAFT;
    }

    @Override
    public String getDescription() {
        return "Gets the current status of the mojang servers.";
    }

    @Override
    public String getName() {
        return "mojangstatus";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        try {
            final URLConnection urlc = new URL("https://status.mojang.com/check").openConnection();
            urlc.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            final var reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));

            final var strBuilder = new StringBuilder();
            final String arg = reader.readLine();
            while (!arg.contains("boi")) {
                if (strBuilder.toString().contains(arg)) {
                    break;
                }
                strBuilder.append(arg);
            }

            if (!strBuilder.toString().isBlank()) {
                String service, status = "";
                final StringBuilder msg = new StringBuilder();
                final String[] services = strBuilder.toString().replace("{", "").replace("}", "")
                        .replace("[", "").replace("]", "").replace("\"", "").split(",");
                for (final String str : services) {
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
                ctx.getEvent().deferReply()
                        .addEmbeds(new EmbedBuilder().setTitle("Mojang Service Status")
                                .setDescription(msg.toString().trim())
                                .setColor(BotUtils.generateRandomPastelColor()).build())
                        .mentionRepliedUser(false).queue();
            }
        } catch (IOException | IllegalArgumentException e) {
            ctx.getEvent().deferReply().setContent(
                    "There has been an error processing this command! Please report this to the server owner.")
                    .mentionRepliedUser(false).queue();
        }
    }
}
