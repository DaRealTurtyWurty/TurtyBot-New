package io.github.darealturtywurty.turtybot.commands.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

public class ClearCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		if (ctx.getArgs().length < 1) {
			ctx.getMessage().reply("You must supply the amount of messages that you want to remove!")
					.mentionRepliedUser(false).queue(msg -> msg.delete().queueAfter(20, TimeUnit.SECONDS));
		}

		if (ctx.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			var messagesToDelete = 0;
			try {
				messagesToDelete = Integer.parseInt(ctx.getArgs()[0]);
			} catch (NumberFormatException ex) {
				ctx.getMessage().reply("You must supply a valid number of messages to delete!").mentionRepliedUser(false)
						.queue(msg -> {
							msg.delete().queueAfter(20, TimeUnit.SECONDS);
							ctx.getMessage().delete().queueAfter(20, TimeUnit.SECONDS);
						});
				return;
			}

			if (messagesToDelete < 1) {
				ctx.getMessage().reply("You must supply a number of messages greater than 0!").mentionRepliedUser(false)
						.queue(msg -> {
							msg.delete().queueAfter(20, TimeUnit.SECONDS);
							ctx.getMessage().delete().queueAfter(20, TimeUnit.SECONDS);
						});
				return;
			}

			ctx.getMessage().delete().queue();

			var embed = new EmbedBuilder().setColor(new Color(97, 0, 0)).setTitle("Cleared Messages!")
					.setDescription("**Channel**: " + ctx.getChannel().getAsMention() + "\n**Cleared By**: "
							+ ctx.getMember().getAsMention() + "\n**Amount Cleared**: " + messagesToDelete)
					.setTimestamp(Instant.now());

			deleteMessages(ctx.getChannel(), messagesToDelete, embed);
			return;
		}
		ctx.getMessage().reply("You do not have permission to use this command!").mentionRepliedUser(false).queue(msg -> {
			msg.delete().queueAfter(20, TimeUnit.SECONDS);
			ctx.getMessage().delete().queueAfter(20, TimeUnit.SECONDS);
		});
	}

	public static void deleteMessages(TextChannel channel, int amount, @Nullable EmbedBuilder embed) {
		var loggingChannel = BotUtils.getModLogChannel(channel.getGuild());
		var realAmount = amount > 100 ? 100 : amount;
		channel.getHistory().retrievePast(realAmount).queue(channel::purgeMessages);
		if (amount - 100 > 0)
			deleteMessages(channel, amount - 100, embed);
		else if (embed != null)
			loggingChannel.sendMessage(embed.build()).queue();
	}

	@Override
	public String getName() {
		return "clear";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("purge", "wipe", "clean", "flush");
	}

	@Override
	public String getDescription() {
		return "Clears the last {x} messages in a channel.";
	}

}
