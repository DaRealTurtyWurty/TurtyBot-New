package io.github.darealturtywurty.turtybot.commands.fun;

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
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class AdviceCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Gets some pro advice.";
    }

    @Override
    public String getName() {
        return "advice";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        try {
            final URLConnection urlc = new URL("https://api.adviceslip.com/advice").openConnection();
            urlc.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            final String result = new BufferedReader(new InputStreamReader(urlc.getInputStream())).readLine();
            urlc.getInputStream().close();
            ctx.getEvent().deferReply().setContent(result.split("\"")[7]).mentionRepliedUser(false).queue();
        } catch (final IOException e) {
            ctx.getEvent().deferReply().setContent("There was an issue accessing the advice database.")
                    .mentionRepliedUser(false).queue();
        }
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
