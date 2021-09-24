package io.github.darealturtywurty.turtybot.commands.minecraft;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class LocateLogsCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MINECRAFT;
    }

    @Override
    public String getDescription() {
        return "How to locate your logs.";
    }

    @Override
    public String getName() {
        return "locate-logs";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var embed = new EmbedBuilder();
        embed.setColor(Color.ORANGE);
        embed.setTimestamp(Instant.now());
        embed.setFooter(
                "Requested by " + ctx.getAuthor().getName() + "#" + ctx.getAuthor().getDiscriminator(),
                ctx.getAuthor().getEffectiveAvatarUrl());
        embed.setTitle("What are logs and how do I find them?");
        embed.setDescription(
                "When the game runs, there will always be an output, mostly used for debugging and information. "
                        + "The running output appears in the console, which can be used to debug an error perfectly fine. However, "
                        + "the better option is to get the finished output (the log), which will appear when the application has ended."
                        + "\n\nThere are three forms of logs to look out for:\n- The Crash Report\n- The Latest Log\n- The Debug Log\n\n"
                        + "Each of these serves a different purpose.");
        embed.addField("Crash Report",
                "The crash report will appear only if there was an exception or error that caused the JVM "
                        + "to terminate the program. Crash reports are generally not very useful as they only contain the message and the "
                        + "stacktrace that caused the termination. And this is not necessarily where the issue is, as this error could "
                        + "be the result of another error.\n\nCrash Reports will be located in your root project folder followed by `run` "
                        + "and then `crash-reports`. From here you will see `crash-date_time` followed by `server.txt` or `client.txt`. "
                        + "Whether it is server or client depends on which thread the error occurred on.",
                false);
        embed.addField("Latest Log",
                "The latest log is the latest output in the form of a `.log`. This log is much more useful, "
                        + "since it contains a copy of the console output, and therefore can be used to track non-breaking issues as well as breaking "
                        + "ones. The `latest.log` will be located in your root project folder followed by `run` and then `logs`. "
                        + "This folder will also be filled with tonnes of `.log.gz` files. However, you should ignore these.",
                false);
        embed.addField("Debug Log",
                "The debug log is the latest output, but with the debug property set to `true`. "
                        + "This log is significantly more useful for debugging than the `latest.log` since it contains much more debug "
                        + "related information, outputted specifically for this purpose. The debug log should be the one you send if you "
                        + "need help fixing any runtime errors as it serves much more information to go off of. You can find the `debug.log` "
                        + "in your root project folder followed by `run` and then `logs`. Once again, you can just ignore the `.log.gz` files.",
                false);
        ctx.getEvent().deferReply().addEmbeds(embed.build()).queue();
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
