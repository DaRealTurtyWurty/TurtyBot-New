package io.github.darealturtywurty.turtybot.commands.bot;

import static java.lang.String.format;

import java.awt.Color;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class ConfigCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		if (ctx.getArgs().length == 3) {
			String action = ctx.getArgs()[0];
			String key = ctx.getArgs()[1];
			String value = ctx.getArgs()[2];
			switch (action.toLowerCase()) {
			case "set":
				this.handleSetConfig(ctx.getGuild(), key, value,
						callback -> ctx.getMessage()
								.reply(new EmbedBuilder().setDescription(callback).setColor(Color.ORANGE).build())
								.mentionRepliedUser(false).queue(reply -> {
									ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
									reply.delete().queueAfter(15, TimeUnit.SECONDS);
								}));
				break;
			case "get":
				ctx.getMessage()
						.replyFormat(
								"`%sconfig get` only accepts 1 following argument! For more information, use `%sconfig`!",
								BotUtils.getPrefixFromGuild(ctx.getGuild()), BotUtils.getPrefixFromGuild(ctx.getGuild()))
						.mentionRepliedUser(false).queue(reply -> {
							ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
							reply.delete().queueAfter(15, TimeUnit.SECONDS);
						});
				break;
			default:
				ctx.getMessage().replyFormat(
						"You must specify whether you want to set or get a value. For more information, use `%sconfig`!",
						BotUtils.getPrefixFromGuild(ctx.getGuild())).mentionRepliedUser(false).queue(reply -> {
							reply.delete().queueAfter(15, TimeUnit.SECONDS);
							ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
						});
				break;
			}

		} else if (ctx.getArgs().length == 2) {
			if (ctx.getArgs()[0].equalsIgnoreCase("get")) {
				this.handleGetConfig(ctx.getGuild(), ctx.getArgs()[1],
						callback -> ctx.getMessage()
								.reply(new EmbedBuilder().setDescription(callback).setColor(Color.BLUE).build())
								.mentionRepliedUser(false).queue(reply -> {
									ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
									reply.delete().queueAfter(15, TimeUnit.SECONDS);
								}));
			} else if (ctx.getArgs()[0].equalsIgnoreCase("set")) {
				ctx.getMessage().reply("You must specify the value that you want to set this option to!")
						.mentionRepliedUser(false).queue(reply -> {
							reply.delete().queueAfter(15, TimeUnit.SECONDS);
							ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
						});
			} else {
				ctx.getMessage().replyFormat("You must specify a valid action! For more information, use `%sconfig`!",
						BotUtils.getPrefixFromGuild(ctx.getGuild())).mentionRepliedUser(false).queue(reply -> {
							reply.delete().queueAfter(15, TimeUnit.SECONDS);
							ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
						});
			}
		} else if (ctx.getArgs().length == 1) {
			ctx.getMessage()
					.replyFormat(
							"You must specify the option that you want to configure! For more information, use `%sconfig`!",
							BotUtils.getPrefixFromGuild(ctx.getGuild()))
					.mentionRepliedUser(false).queue(reply -> {
						reply.delete().queueAfter(15, TimeUnit.SECONDS);
						ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
					});
		} else if (ctx.getArgs().length <= 0) {
			ctx.getMessage().reply(new EmbedBuilder().setTitle("The list of possible configurables:").setDescription(
					"`mod_role` -> Accepts Role ID; The Role that will be used to check if a user is a moderator."
							+ "\n`mod_log` -> Accepts channel mention; The channel that will be used to log moderator actions."
							+ "\n`prefix` -> Accepts any prefix; The character/phrase that will be used to trigger the bot.")
					.setColor(BotUtils.generateRandomColor()).build()).mentionRepliedUser(false).queue(reply -> {
						reply.delete().queueAfter(30, TimeUnit.SECONDS);
						ctx.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);
					});
		} else {
			ctx.getMessage().replyFormat("This command only takes in 2 - 3 arguments! For more information, use `%sconfig`!",
					BotUtils.getPrefixFromGuild(ctx.getGuild())).mentionRepliedUser(false).queue(reply -> {
						reply.delete().queueAfter(15, TimeUnit.SECONDS);
						ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
					});
		}
	}

	private void handleSetConfig(Guild guild, String key, String value, Consumer<String> callback) {
		switch (key.toLowerCase()) {
		case "mod_role":
			Role role = guild.getRoleById(value);
			if (role == null) {
				callback.accept("You must provide a valid role id!");
				break;
			}
			BotUtils.setModRoleForGuild(guild, role.getIdLong());
			callback.accept("Set moderator role to: " + role.getAsMention());
			break;
		case "mod_log":
			String channelID = value.replace("<", "").replace("#", "").replace("!", "").replace(">", "");
			TextChannel channel = guild.getTextChannelById(channelID);
			if (channel == null) {
				callback.accept("You must provide a valid channel mention!");
				break;
			}
			BotUtils.setModLogForGuild(guild, channel.getIdLong());
			callback.accept("Set mod log channel to: " + channel.getAsMention());
			break;
		case "adv_modder_role":
			Role role1 = guild.getRoleById(value);
			if (role1 == null) {
				callback.accept("You must provide a valid role id!");
				break;
			}
			BotUtils.setAdvModderRoleForGuild(guild, role1.getIdLong());
			callback.accept("Set Advanced Modder role to: " + role1.getAsMention());
			break;
		case "prefix":
			BotUtils.setPrefixForGuild(guild, value);
			callback.accept("Set guild prefix to: " + value);
			break;
		default:
			callback.accept(format("You must specify a valid option to configure! For more information, use `%sconfig`!",
					BotUtils.getPrefixFromGuild(guild)));
			break;
		}
	}

	private void handleGetConfig(Guild guild, String key, Consumer<String> callback) {
		switch (key) {
		case "mod_role":
			Role role = BotUtils.getModeratorRole(guild);
			if (role == null) {
				callback.accept("This guild has no moderator role!");
				break;
			}
			callback.accept("The moderator role for this guild is: " + role.getAsMention());
			break;
		case "mod_log":
			TextChannel channel = BotUtils.getModLogChannel(guild);
			if (channel == null) {
				callback.accept("This guild has no moderator logging channel!");
				break;
			}
			callback.accept("The moderator logging channel for this guild is: " + channel.getAsMention());
			break;
		case "adv_modder_role":
			Role role1 = BotUtils.getAdvModderRole(guild);
			if (role1 == null) {
				callback.accept("This guild has no moderator role!");
				break;
			}
			callback.accept("The moderator role for this guild is: " + role1.getAsMention());
			break;
		case "prefix":
			callback.accept("The prefix for this guild is: " + BotUtils.getPrefixFromGuild(guild));
			break;
		default:
			callback.accept(format("You must specify a valid option to get! For more information, use `%sconfig`!",
					BotUtils.getPrefixFromGuild(guild)));
			break;
		}
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}

	@Override
	public String getName() {
		return "config";
	}

	@Override
	public String getDescription() {
		return "Allows you to configure certain bot options. Use `<prefix>config` for details on the configurable options.";
	}
}
