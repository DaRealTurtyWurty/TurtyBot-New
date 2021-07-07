package io.github.darealturtywurty.turtybot.commands.utility;

import static java.lang.String.format;

import java.awt.Color;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.BotUtils.StarboardUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class ConfigCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public String getDescription() {
		return "Allows you to configure certain bot options. Use `<prefix>config` for details on the configurable options.";
	}

	@Override
	public String getName() {
		return "config";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getArgs().length == 3) {
			final String action = ctx.getArgs()[0];
			final String key = ctx.getArgs()[1];
			final String value = ctx.getArgs()[2];
			switch (action.toLowerCase()) {
			case "set":
				handleSetConfig(ctx.getGuild(), key, value,
						callback -> ctx.getMessage()
								.replyEmbeds(new EmbedBuilder().setDescription(callback).setColor(Color.ORANGE).build())
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
				handleGetConfig(ctx.getGuild(), ctx.getArgs()[1],
						callback -> ctx.getMessage()
								.replyEmbeds(new EmbedBuilder().setDescription(callback).setColor(Color.BLUE).build())
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
			ctx.getMessage().replyEmbeds(new EmbedBuilder().setTitle("The list of possible configurables:").setDescription(
					"`mod_role` -> Accepts Role ID; The Role that will be used to check if a user is a moderator."
							+ "\n`mod_log` -> Accepts channel mention; The channel that will be used to log moderator actions."
							+ "\n`prefix` -> Accepts any prefix; The character/phrase that will be used to trigger the bot.")
					.setColor(BotUtils.generateRandomPastelColor()).build()).mentionRepliedUser(false).queue(reply -> {
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

	private void handleGetConfig(final Guild guild, final String key, final Consumer<String> callback) {
		switch (key) {
		case "mod_role":
			final var role = BotUtils.getModeratorRole(guild);
			if (role == null) {
				callback.accept("This guild has no Moderator Role!");
				break;
			}
			callback.accept("The Moderator Role for this guild is: " + role.getAsMention());
			break;
		case "mod_log":
			final TextChannel channel = BotUtils.getModLogChannel(guild);
			if (channel == null) {
				callback.accept("This guild has no Moderation Logging Channel!");
				break;
			}
			callback.accept("The Moderation Logging Channel for this guild is: " + channel.getAsMention());
			break;
		case "adv_modder_role":
			final var role1 = BotUtils.getAdvModderRole(guild);
			if (role1 == null) {
				callback.accept("This guild has no Advanced Modder Role!");
				break;
			}
			callback.accept("The Advanced Modder Role for this guild is: " + role1.getAsMention());
			break;
		case "prefix":
			callback.accept("The Prefix for this guild is: " + BotUtils.getPrefixFromGuild(guild));
			break;
		case "muted_role":
			final var role2 = BotUtils.getMutedRole(guild);
			if (role2 == null) {
				callback.accept("This guild has no Muted Role!");
				break;
			}
			callback.accept("The Muted Role for this guild is: " + role2.getAsMention());
			break;
		case "mute_threshold":
			callback.accept("The Mute Threshold for this guild is: " + BotUtils.getMuteThreshold(guild));
			break;
		case "kick_threshold":
			callback.accept("The Kick Threshold for this guild is: " + BotUtils.getKickThreshold(guild));
			break;
		case "ban_threshold":
			callback.accept("The Ban Threshold for this guild is: " + BotUtils.getBanThreshold(guild));
			break;
		case "showcases":
			final TextChannel channel2 = StarboardUtils.getShowcasesChannel(guild);
			if (channel2 == null) {
				callback.accept("This guild has no Showcases Channel!");
				break;
			}
			callback.accept("The Showcases Channel for this guild is: " + channel2.getAsMention());
			break;
		case "starboard":
			final TextChannel channel3 = StarboardUtils.getStarboardChannel(guild);
			if (channel3 == null) {
				callback.accept("This guild has no Starboard Channel!");
				break;
			}
			callback.accept("The Starboard Channel for this guild is: " + channel3.getAsMention());
			break;
		case "minimum_stars":
			callback.accept("The Minimum Stars for this guild is: " + StarboardUtils.getMinimumStars(guild));
			break;
		case "should_include_bot_stars":
			callback.accept("Should this guild count bot stars: "
					+ BotUtils.trueFalseToYesNo(StarboardUtils.includesBotStar(guild)));
			break;
		case "enable_starboard":
			callback.accept("Is the starboard enabled for this guild: "
					+ BotUtils.trueFalseToYesNo(StarboardUtils.isStarboardEnabled(guild)));
			break;
		case "starboard_stage":
			// TODO: StarboardUtils.getStarboardStages(Guild guild);
			break;
		default:
			callback.accept(format("You must specify a valid option to get! For more information, use `%sconfig`!",
					BotUtils.getPrefixFromGuild(guild)));
			break;
		}
	}

	private void handleSetConfig(final Guild guild, final String key, final String value, final Consumer<String> callback) {
		switch (key.toLowerCase()) {
		case "mod_role":
			final var role = guild.getRoleById(value);
			if (role == null) {
				callback.accept("You must provide a valid role id!");
				break;
			}
			BotUtils.setModRoleForGuild(guild, role.getIdLong());
			callback.accept("Set Moderator Role to: " + role.getAsMention());
			break;
		case "mod_log":
			final String channelID = value.replace("<", "").replace("#", "").replace("!", "").replace(">", "");
			final var channel = guild.getTextChannelById(channelID);
			if (channel == null) {
				callback.accept("You must provide a valid channel mention!");
				break;
			}
			BotUtils.setModLogForGuild(guild, channel.getIdLong());
			callback.accept("Set Moderation Logging Channel to: " + channel.getAsMention());
			break;
		case "adv_modder_role":
			final var role1 = guild.getRoleById(value);
			if (role1 == null) {
				callback.accept("You must provide a valid role id!");
				break;
			}
			BotUtils.setAdvModderRoleForGuild(guild, role1.getIdLong());
			callback.accept("Set Advanced Modder Role to: " + role1.getAsMention());
			break;
		case "prefix":
			BotUtils.setPrefixForGuild(guild, value);
			callback.accept("Set guild prefix to: " + value);
			break;
		case "muted_role":
			final var role2 = guild.getRoleById(value);
			if (role2 == null) {
				callback.accept("You must provide a valid role id!");
				break;
			}
			BotUtils.setMutedRoleForGuild(guild, role2.getIdLong());
			callback.accept("Set Muted Role to: " + role2.getAsMention());
			break;
		case "mute_threshold":
			BotUtils.setMuteThreshold(guild, Integer.parseInt(value));
			callback.accept("Set Mute Threshold to: " + value);
			break;
		case "kick_threshold":
			BotUtils.setKickThreshold(guild, Integer.parseInt(value));
			callback.accept("Set Kick Threshold to: " + value);
			break;
		case "ban_threshold":
			BotUtils.setBanThreshold(guild, Integer.parseInt(value));
			callback.accept("Set Ban Threshold to: " + value);
			break;
		case "showcases":
			final String channelID2 = value.replace("<", "").replace("#", "").replace("!", "").replace(">", "");
			final var channel2 = guild.getTextChannelById(channelID2);
			if (channel2 == null) {
				callback.accept("You must provide a valid channel mention!");
				break;
			}
			StarboardUtils.setShowcasesChannel(guild, channel2.getIdLong());
			callback.accept("Set Showcases Channel to: " + channel2.getAsMention());
			break;
		case "starboard":
			final String channelID3 = value.replace("<", "").replace("#", "").replace("!", "").replace(">", "");
			final var channel3 = guild.getTextChannelById(channelID3);
			if (channel3 == null) {
				callback.accept("You must provide a valid channel mention!");
				break;
			}
			StarboardUtils.setStarboardChannel(guild, channel3.getIdLong());
			callback.accept("Set Starboard Channel to: " + channel3.getAsMention());
			break;
		case "minimum_stars":
			StarboardUtils.setMinimumStars(guild, Integer.parseInt(value));
			callback.accept("Set Minimum Stars to: " + value);
			break;
		case "should_include_bot_stars":
			StarboardUtils.setIncludeBotStar(guild, Boolean.parseBoolean(value));
			callback.accept("Set Include Stars to: " + value);
			break;
		case "enable_starboard":
			StarboardUtils.setStarboardEnabled(guild, Boolean.parseBoolean(value));
			callback.accept("Set Starboard Enabled to: " + value);
			break;
		case "starboard_stage":
			// TODO: StarboardUtils.setStage(guild, int stage);
			break;
		default:
			callback.accept(format("You must specify a valid option to configure! For more information, use `%sconfig`!",
					BotUtils.getPrefixFromGuild(guild)));
			break;
		}
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}
}
