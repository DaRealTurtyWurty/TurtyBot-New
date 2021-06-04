package io.github.darealturtywurty.turtybot.help_system;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class RequestHelpCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		var title = String.join(" ", ctx.getArgs());
		if (title.isBlank()) {
			ctx.getAuthor().openPrivateChannel().queue(
					channel -> channel.sendMessage("You must provide the main title of your question!\n\nFor example: "
							+ "`An issue with my item texture being magenta and black`\n\n"
							+ "You do not need to be extremely descriptive, as you will be required to state extra information "
							+ "once the channel has been created.").queue());
			return;
		}

		HelpManager.requireCategory(ctx.getGuild(), 1);
		if (!HelpManager.hasChannel(ctx.getGuild(), ctx.getAuthor())) {
			HelpManager.createChannel(ctx.getGuild(), ctx.getAuthor(), title);
			ctx.getMessage().delete().queue();

			final var timer = new Timer();
			final var timerTask = new TimerTask() {
				@Override
				public void run() {
					TextChannel channel = ctx.getGuild().getTextChannelsByName(
							ctx.getAuthor().getName() + "-" + ctx.getAuthor().getDiscriminator(), true).get(0);
					channel.sendMessage("Please provide a detailed description of your problem, what you have tried "
							+ "and what your aim is. You do not need to provide any logs or images/videos as of this stage.")
							.queue();
					HelpManager.setStage(channel, ctx.getAuthor().getIdLong(), 0);
				}
			};

			timer.schedule(timerTask, 10000);
			return;
		}

		ctx.getAuthor().openPrivateChannel()
				.queue(channel -> channel.sendMessage("You cannot open a help channel, you already have one open!").queue());
		ctx.getMessage().delete().queue();
	}

	@Override
	public String getName() {
		return "requesthelp";
	}

	@Override
	public String getDescription() {
		return "Use this command to get your personal help channel setup.";
	}

	@Override
	public Pair<Boolean, List<String>> validChannels() {
		return Pair.of(true, Arrays.asList("request-help"));
	}
}
