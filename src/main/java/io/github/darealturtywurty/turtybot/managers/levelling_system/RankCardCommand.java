package io.github.darealturtywurty.turtybot.managers.levelling_system;

import static io.github.darealturtywurty.turtybot.util.Constants.LEVELLING_MANAGER;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

public class RankCardCommand implements IGuildCommand {

	private static final List<String> IMAGE_EXTS = List.of("png", "jpg", "jpeg", "webm");

	public static String grabImageURL(final Message message) {
		var imageStr = "";
		if (!message.getAttachments().isEmpty()) {
			final var attachment = message.getAttachments().get(0);
			if (attachment.getFileExtension() != null && IMAGE_EXTS.contains(attachment.getFileExtension().toLowerCase())) {
				imageStr = attachment.getUrl();
			}
		}

		final String[] args = message.getContentRaw().split(" ");
		if (imageStr.isBlank() && args.length >= 3) {
			final List<String> list = new ArrayList<>(Arrays.asList(args));
			list.remove(0);
			list.remove(0);
			final var urlStr = String.join("", list.toArray(new String[0]));
			try {
				if (ImageIO.read(new URL(urlStr)) != null) {
					imageStr = urlStr;
				}
			} catch (final IOException e) {
				imageStr = "";
			}
		}

		return imageStr;
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public String getDescription() {
		return "Customize your rank card.";
	}

	@Override
	public String getName() {
		return "rankcard";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var guild = ctx.getGuild();
		final var message = ctx.getMessage();
		if (ctx.getArgs().length < 1) {
			final var embed = new EmbedBuilder().setColor(Color.BLUE).setTimestamp(Instant.now())
					.setAuthor(ctx.getAuthor().getName(), null, ctx.getAuthor().getEffectiveAvatarUrl());
			embed.setDescription("__Example Values__:\n" + "**colorAsHex**: FFAB15\n" + "**numberFrom0to255**: 187\n"
					+ "**fileOrURL**: https://media.discordapp.net/attachments/849775437021839390/857786266216955964/unknown.png");

			embed.addField("Background Color", BotUtils.getPrefixFromGuild(guild) + "rankcard bgColor colorAsHex", true);
			embed.addField("Outline Color", BotUtils.getPrefixFromGuild(guild) + "rankcard outColor colorAsHex", true);
			embed.addField("Rank Text Color", BotUtils.getPrefixFromGuild(guild) + "rankcard rankTextColor colorAsHex",
					true);
			embed.addField("Level Text Color", BotUtils.getPrefixFromGuild(guild) + "rankcard levelTextColor colorAsHex",
					true);
			embed.addField("XP Bar Outline Color", BotUtils.getPrefixFromGuild(guild) + "rankcard xpOutColor colorAsHex",
					true);
			embed.addField("XP Bar Empty Color", BotUtils.getPrefixFromGuild(guild) + "rankcard xpEmptyColor colorAsHex",
					true);
			embed.addField("XP Bar Fill Color", BotUtils.getPrefixFromGuild(guild) + "rankcard xpFillColor colorAsHex",
					true);
			embed.addField("Avatar Outline Color", BotUtils.getPrefixFromGuild(guild) + "rankcard avatarOutColor colorAsHex",
					true);
			embed.addField("Percent Text Color", BotUtils.getPrefixFromGuild(guild) + "rankcard percentTextColor colorAsHex",
					true);
			embed.addField("XP Text Color", BotUtils.getPrefixFromGuild(guild) + "rankcard xpTextColor colorAsHex", true);
			embed.addField("Outline Opacity", BotUtils.getPrefixFromGuild(guild) + "rankcard outAlpha numberFrom0to255",
					true);

			embed.addBlankField(false);

			// Premium
			embed.addField("Background Image (Premium)", BotUtils.getPrefixFromGuild(guild) + "rankcard bgImg fileOrURL",
					true);
			embed.addField("Outline Image (Premium)", BotUtils.getPrefixFromGuild(guild) + "rankcard outImg fileOrURL",
					true);
			embed.addField("XP Bar Outline Image (Premium)",
					BotUtils.getPrefixFromGuild(guild) + "rankcard xpOutImg fileOrURL", true);
			embed.addField("XP Bar Empty Image (Premium)",
					BotUtils.getPrefixFromGuild(guild) + "rankcard xpEmptyImg fileOrURL", true);
			embed.addField("XP Bar Fill Image (Premium)",
					BotUtils.getPrefixFromGuild(guild) + "rankcard xpFillImg fileOrURL", true);
			embed.addField("Avatar Outline Image (Premium)",
					BotUtils.getPrefixFromGuild(guild) + "rankcard avatarOutImg fileOrURL", true);

			embed.addField("Randomize Colors", BotUtils.getPrefixFromGuild(guild) + "rankcard randomize", false);

			message.replyEmbeds(embed.build()).mentionRepliedUser(false)
					.queue(msg -> msg.editMessage("The options for customizing your rank card are: ").queue());
		} else {
			final var member = ctx.getMember();
			final var card = LEVELLING_MANAGER.getOrCreateCard(member);

			switch (ctx.getArgs()[0]) {
			case "bgColor":
				if (ctx.getArgs().length == 1) {
					message.reply(
							"Your Rank Card's Background Colour is (Red: " + card.backgroundColour.getRed() + ", Green: "
									+ card.backgroundColour.getGreen() + ", Blue: " + card.backgroundColour.getBlue() + ")")
							.mentionRepliedUser(false).queue();
					return;
				}

				try {
					card.backgroundColour = Color.decode(ctx.getArgs()[1]);
				} catch (final NumberFormatException e) {
					card.backgroundColour = RankCard.DEFAULT.backgroundColour;
				}

				message.reply("Your Rank Card's Background Colour has been changed to: (Red: "
						+ card.backgroundColour.getRed() + ", Green: " + card.backgroundColour.getGreen() + ", Blue: "
						+ card.backgroundColour.getBlue() + ")").mentionRepliedUser(false).queue();

				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "outColor":
				if (ctx.getArgs().length == 1) {
					message.reply("Your Rank Card's Outline Colour is (Red: " + card.outlineColour.getRed() + ", Green: "
							+ card.outlineColour.getGreen() + ", Blue: " + card.outlineColour.getBlue() + ")")
							.mentionRepliedUser(false).queue();
					return;
				}

				try {
					card.outlineColour = Color.decode(ctx.getArgs()[1]);
				} catch (final NumberFormatException e) {
					card.outlineColour = RankCard.DEFAULT.outlineColour;
				}

				message.reply("Your Rank Card's Outline Colour has been changed to: (Red: " + card.outlineColour.getRed()
						+ ", Green: " + card.outlineColour.getGreen() + ", Blue: " + card.outlineColour.getBlue() + ")")
						.mentionRepliedUser(false).queue();

				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "rankTextColor":
				if (ctx.getArgs().length == 1) {
					message.reply("Your Rank Card's Rank Text Colour is (Red: " + card.rankTextColour.getRed() + ", Green: "
							+ card.rankTextColour.getGreen() + ", Blue: " + card.rankTextColour.getBlue() + ")")
							.mentionRepliedUser(false).queue();
					return;
				}

				try {
					card.rankTextColour = Color.decode(ctx.getArgs()[1]);
				} catch (final NumberFormatException e) {
					card.rankTextColour = RankCard.DEFAULT.rankTextColour;
				}

				message.reply("Your Rank Card's Rank Text Colour has been changed to: (Red: " + card.rankTextColour.getRed()
						+ ", Green: " + card.rankTextColour.getGreen() + ", Blue: " + card.rankTextColour.getBlue() + ")")
						.mentionRepliedUser(false).queue();

				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "levelTextColor":
				if (ctx.getArgs().length == 1) {
					message.reply(
							"Your Rank Card's Level Text Colour is (Red: " + card.levelTextColour.getRed() + ", Green: "
									+ card.levelTextColour.getGreen() + ", Blue: " + card.levelTextColour.getBlue() + ")")
							.mentionRepliedUser(false).queue();
					return;
				}

				try {
					card.levelTextColour = Color.decode(ctx.getArgs()[1]);
				} catch (final NumberFormatException e) {
					card.levelTextColour = RankCard.DEFAULT.levelTextColour;
				}

				message.reply("Your Rank Card's Level Text Colour has been changed to: (Red: "
						+ card.levelTextColour.getRed() + ", Green: " + card.levelTextColour.getGreen() + ", Blue: "
						+ card.levelTextColour.getBlue() + ")").mentionRepliedUser(false).queue();

				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "xpOutColor":
				if (ctx.getArgs().length == 1) {
					message.reply(
							"Your Rank Card's XP Bar Outline Colour is (Red: " + card.xpOutlineColour.getRed() + ", Green: "
									+ card.xpOutlineColour.getGreen() + ", Blue: " + card.xpOutlineColour.getBlue() + ")")
							.mentionRepliedUser(false).queue();
					return;
				}

				try {
					card.xpOutlineColour = Color.decode(ctx.getArgs()[1]);
				} catch (final NumberFormatException e) {
					card.xpOutlineColour = RankCard.DEFAULT.xpOutlineColour;
				}

				message.reply("Your Rank Card's XP Bar Outline Colour has been changed to: (Red: "
						+ card.xpOutlineColour.getRed() + ", Green: " + card.xpOutlineColour.getGreen() + ", Blue: "
						+ card.xpOutlineColour.getBlue() + ")").mentionRepliedUser(false).queue();

				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "xpEmptyColor":
				if (ctx.getArgs().length == 1) {
					message.reply("Your Rank Card's XP Bar Empty Colour is (Red: " + card.xpEmptyColour.getRed()
							+ ", Green: " + card.xpEmptyColour.getGreen() + ", Blue: " + card.xpEmptyColour.getBlue() + ")")
							.mentionRepliedUser(false).queue();
					return;
				}

				try {
					card.xpEmptyColour = Color.decode(ctx.getArgs()[1]);
				} catch (final NumberFormatException e) {
					card.xpEmptyColour = RankCard.DEFAULT.xpEmptyColour;
				}

				message.reply("Your Rank Card's XP Bar Empty Colour has been changed to: (Red: "
						+ card.xpEmptyColour.getRed() + ", Green: " + card.xpEmptyColour.getGreen() + ", Blue: "
						+ card.xpEmptyColour.getBlue() + ")").mentionRepliedUser(false).queue();

				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "xpFillColor":
				if (ctx.getArgs().length == 1) {
					message.reply("Your Rank Card's XP Bar Fill Colour is (Red: " + card.xpFillColour.getRed() + ", Green: "
							+ card.xpFillColour.getGreen() + ", Blue: " + card.xpFillColour.getBlue() + ")")
							.mentionRepliedUser(false).queue();
					return;
				}

				try {
					card.xpFillColour = Color.decode(ctx.getArgs()[1]);
				} catch (final NumberFormatException e) {
					card.xpFillColour = RankCard.DEFAULT.xpFillColour;
				}

				message.reply("Your Rank Card's XP Bar Fill Colour has been changed to: (Red: " + card.xpFillColour.getRed()
						+ ", Green: " + card.xpFillColour.getGreen() + ", Blue: " + card.xpFillColour.getBlue() + ")")
						.mentionRepliedUser(false).queue();

				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "avatarOutColor":
				if (ctx.getArgs().length == 1) {
					message.reply("Your Rank Card's Avatar Outline Colour is (Red: " + card.avatarOutlineColour.getRed()
							+ ", Green: " + card.avatarOutlineColour.getGreen() + ", Blue: "
							+ card.avatarOutlineColour.getBlue() + ")").mentionRepliedUser(false).queue();
					return;
				}

				try {
					card.avatarOutlineColour = Color.decode(ctx.getArgs()[1]);
				} catch (final NumberFormatException e) {
					card.avatarOutlineColour = RankCard.DEFAULT.avatarOutlineColour;
				}

				message.reply("Your Rank Card's Avatar Outline Colour has been changed to: (Red: "
						+ card.avatarOutlineColour.getRed() + ", Green: " + card.avatarOutlineColour.getGreen() + ", Blue: "
						+ card.avatarOutlineColour.getBlue() + ")").mentionRepliedUser(false).queue();

				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "percentTextColor":
				if (ctx.getArgs().length == 1) {
					message.reply("Your Rank Card's Percent Text Colour is " + card.percentTextColour)
							.mentionRepliedUser(false).queue();
					return;
				}

				try {
					card.percentTextColour = Color.decode(ctx.getArgs()[1]);
				} catch (final NumberFormatException e) {
					card.percentTextColour = RankCard.DEFAULT.percentTextColour;
				}

				message.reply("Your Rank Card's Percent Text Colour has been changed to: (Red: "
						+ card.percentTextColour.getRed() + ", Green: " + card.percentTextColour.getGreen() + ", Blue: "
						+ card.percentTextColour.getBlue() + ")").mentionRepliedUser(false).queue();

				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "xpTextColor":
				if (ctx.getArgs().length == 1) {
					message.reply("Your Rank Card's XP Text Colour is " + card.xpTextColour).mentionRepliedUser(false)
							.queue();
					return;
				}

				try {
					card.xpTextColour = Color.decode(ctx.getArgs()[1]);
				} catch (final NumberFormatException e) {
					card.xpTextColour = RankCard.DEFAULT.xpTextColour;
				}

				message.reply("Your Rank Card's XP Text Colour has been changed to: (Red: " + card.xpTextColour.getRed()
						+ ", Green: " + card.xpTextColour.getGreen() + ", Blue: " + card.xpTextColour.getBlue() + ")")
						.mentionRepliedUser(false).queue();

				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "nameTextColour":
				if (ctx.getArgs().length == 1) {
					message.reply("Your Rank Card's Name Text Colour is " + card.nameTextColour).mentionRepliedUser(false)
							.queue();
					return;
				}

				try {
					card.nameTextColour = Color.decode(ctx.getArgs()[1]);
				} catch (final NumberFormatException e) {
					card.nameTextColour = RankCard.DEFAULT.nameTextColour;
				}

				message.reply("Your Rank Card's Name Text Colour has been changed to: (Red: " + card.nameTextColour.getRed()
						+ ", Green: " + card.nameTextColour.getGreen() + ", Blue: " + card.nameTextColour.getBlue() + ")")
						.mentionRepliedUser(false).queue();

				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "outAlpha":
				if (ctx.getArgs().length == 1) {
					message.reply("Your Rank Card's Outline Opacity is " + card.outlineOpacity * 255)
							.mentionRepliedUser(false).queue();
					return;
				}

				var opacity = 0;
				try {
					opacity = Integer.parseInt(ctx.getArgs()[1]);
				} catch (final NumberFormatException e) {
					opacity = 255;
				}

				if (opacity < 0) {
					opacity = 0;
				}
				if (opacity > 255) {
					opacity = 255;
				}

				card.outlineOpacity = opacity / 255f;

				message.reply("Your Rank Card's Outline Opacity has been changed to: " + card.outlineOpacity * 255)
						.mentionRepliedUser(false).queue();

				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "bgImg":
				if (member.getTimeBoosted() != null || member.isOwner()
						|| member.getGuild().getIdLong() == 819294753732296776L) {
					if (ctx.getArgs().length == 1 && message.getAttachments().isEmpty()) {
						message.reply(card.backgroundImage.isBlank() ? "You have no Background Image set!"
								: "Your Background Image is: <" + card.backgroundImage + ">").mentionRepliedUser(false)
								.queue();
						return;
					}

					card.backgroundImage = grabImageURL(message);
					message.reply("Your Rank Card's Background Image has been updated!").mentionRepliedUser(false).queue();
					LEVELLING_MANAGER.updateCard(member, card);
				} else {
					message.reply("You must be a premium member in order to set this value.").mentionRepliedUser(false)
							.queue();
				}
				break;
			case "outImg":
				if (member.getTimeBoosted() != null || member.isOwner()
						|| member.getGuild().getIdLong() == 819294753732296776L) {
					if (ctx.getArgs().length == 1 && message.getAttachments().isEmpty()) {
						message.reply(card.outlineImage.isBlank() ? "You have no Outline Image set!"
								: "Your Outline Image is: <" + card.outlineImage + ">").mentionRepliedUser(false).queue();
						return;
					}

					card.outlineImage = grabImageURL(message);
					message.reply("Your Rank Card's Outline Image has been updated!").mentionRepliedUser(false).queue();
					LEVELLING_MANAGER.updateCard(member, card);
				} else {
					message.reply("You must be a premium member in order to set this value.").mentionRepliedUser(false)
							.queue();
				}
				break;
			case "xpOutImg":
				if (member.getTimeBoosted() != null || member.isOwner()
						|| member.getGuild().getIdLong() == 819294753732296776L) {
					if (ctx.getArgs().length == 1 && message.getAttachments().isEmpty()) {
						message.reply(card.xpOutlineImage.isBlank() ? "You have no XP Bar Outline Image set!"
								: "Your XP Bar Outline Image is: <" + card.xpOutlineImage + ">").mentionRepliedUser(false)
								.queue();
						return;
					}

					card.xpOutlineImage = grabImageURL(message);
					message.reply("Your Rank Card's XP Bar Outline Image has been updated!").mentionRepliedUser(false)
							.queue();
					LEVELLING_MANAGER.updateCard(member, card);
				} else {
					message.reply("You must be a premium member in order to set this value.").mentionRepliedUser(false)
							.queue();
				}
				break;
			case "xpEmptyImg":
				if (member.getTimeBoosted() != null || member.isOwner()
						|| member.getGuild().getIdLong() == 819294753732296776L) {
					if (ctx.getArgs().length == 1 && message.getAttachments().isEmpty()) {
						message.reply(card.xpEmptyImage.isBlank() ? "You have no XP Bar Empty Image set!"
								: "Your XP Bar Empty Image is: <" + card.xpEmptyImage + ">").mentionRepliedUser(false)
								.queue();
						return;
					}

					card.xpEmptyImage = grabImageURL(message);
					message.reply("Your Rank Card's XP Bar Empty Image has been updated!").mentionRepliedUser(false).queue();
					LEVELLING_MANAGER.updateCard(member, card);
				} else {
					message.reply("You must be a premium member in order to set this value.").mentionRepliedUser(false)
							.queue();
				}
				break;
			case "xpFillImg":
				if (member.getTimeBoosted() != null || member.isOwner()
						|| member.getGuild().getIdLong() == 819294753732296776L) {
					if (ctx.getArgs().length == 1 && message.getAttachments().isEmpty()) {
						message.reply(card.xpFillImage.isBlank() ? "You have no XP Bar Fill Image set!"
								: "Your XP Bar Fill Image is: <" + card.xpFillImage + ">").mentionRepliedUser(false).queue();
						return;
					}

					card.xpFillImage = grabImageURL(message);
					message.reply("Your Rank Card's XP Bar Fill Image has been updated!").mentionRepliedUser(false).queue();
					LEVELLING_MANAGER.updateCard(member, card);
				} else {
					message.reply("You must be a premium member in order to set this value.").mentionRepliedUser(false)
							.queue();
				}
				break;
			case "avatarOutImg":
				if (member.getTimeBoosted() != null || member.isOwner()
						|| member.getGuild().getIdLong() == 819294753732296776L) {
					if (ctx.getArgs().length == 1 && message.getAttachments().isEmpty()) {
						message.reply(card.avatarOutlineImage.isBlank() ? "You have no Avatar Outline Image set!"
								: "Your Avatar Outline Image is: <" + card.avatarOutlineImage + ">")
								.mentionRepliedUser(false).queue();
						return;
					}

					card.avatarOutlineImage = grabImageURL(message);
					message.reply("Your Rank Card's Avatar Outline Image has been updated!").mentionRepliedUser(false)
							.queue();
					LEVELLING_MANAGER.updateCard(member, card);
				} else {
					message.reply("You must be a premium member in order to set this value.").mentionRepliedUser(false)
							.queue();
				}
				break;
			case "randomize":
				card.backgroundColour = BotUtils.generateRandomColor();
				card.outlineColour = BotUtils.generateRandomColor();
				card.rankTextColour = BotUtils.generateRandomColor();
				card.levelTextColour = BotUtils.generateRandomColor();
				card.xpOutlineColour = BotUtils.generateRandomColor();
				card.xpEmptyColour = BotUtils.generateRandomColor();
				card.xpFillColour = BotUtils.generateRandomColor();
				card.avatarOutlineColour = BotUtils.generateRandomColor();
				card.percentTextColour = BotUtils.generateRandomColor();
				card.xpTextColour = BotUtils.generateRandomColor();

				message.reply("Your Rank Card's colours have been updated!").mentionRepliedUser(false).queue();
				LEVELLING_MANAGER.updateCard(member, card);
				break;
			case "default":
				break;
			default:
				message.reply("You must supply a valid setting to change. Use `" + BotUtils.getPrefixFromGuild(guild)
						+ "rankcard` for more information!").mentionRepliedUser(false).queue();
				break;
			}
		}
	}
}
