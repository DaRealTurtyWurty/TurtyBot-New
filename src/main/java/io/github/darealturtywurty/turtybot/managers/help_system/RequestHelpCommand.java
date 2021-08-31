package io.github.darealturtywurty.turtybot.managers.help_system;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class RequestHelpCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Use this command to get your personal help channel setup.";
    }

    @Override
    public String getName() {
        return "requesthelp";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "question",
                "A title for the question that you would like to ask.", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var title = ctx.getEvent().getOption("question").getAsString();
        if (title.isBlank()) {
            ctx.getEvent().deferReply(true)
                    .setContent("You must provide the main title of your question!\n\nFor example: "
                            + "`An issue with my item texture being magenta and black`\n\n"
                            + "You do not need to be extremely descriptive, as you will be required to state extra information "
                            + "once the channel has been created.")
                    .queue();
            return;
        }

        HelpManager.requireCategory(ctx.getGuild(), 1);
        if (!HelpManager.hasChannel(ctx.getGuild(), ctx.getAuthor())) {
            HelpManager.createChannel(ctx.getGuild(), ctx.getAuthor(), title);

            final var timer = new Timer();
            final var timerTask = new TimerTask() {
                @Override
                public void run() {
                    final TextChannel channel = ctx.getGuild()
                            .getTextChannelsByName(
                                    ctx.getAuthor().getName() + "-" + ctx.getAuthor().getDiscriminator(),
                                    true)
                            .get(0);
                    ctx.getEvent().deferReply(true).setContent(
                            "Please provide a detailed description of your problem, what you have tried "
                                    + "and what your aim is. You do not need to provide any logs or images/videos as of this stage.")
                            .queue();
                    HelpManager.setStage(channel, ctx.getAuthor().getIdLong(), 0);
                }
            };

            timer.schedule(timerTask, 10000);
            return;
        }

        ctx.getAuthor().openPrivateChannel().queue(channel -> channel
                .sendMessage("You cannot open a help channel, you already have one open!").queue());
    }

    @Override
    public List<String> whitelistChannels() {
        return List.of("request-help");
    }
}
