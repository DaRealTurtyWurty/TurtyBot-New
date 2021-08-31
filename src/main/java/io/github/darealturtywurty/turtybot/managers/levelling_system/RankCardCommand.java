package io.github.darealturtywurty.turtybot.managers.levelling_system;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.imageio.ImageIO;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

//@RegisterBotCmd
public class RankCardCommand implements GuildCommand {

    private static final Choice[] CHOICES = { new Choice("background_color", "background_color"),
            new Choice("outline_color", "outline_color"), new Choice("rank_text_color", "rank_text_color"),
            new Choice("level_text_color", "level_text_color"), new Choice("xp_text_color", "xp_text_color"),
            new Choice("name_text_color", "name_text_color"),
            new Choice("xp_outline_color", "xp_outline_color"),
            new Choice("xp_empty_color", "xp_empty_color"), new Choice("xp_fill_color", "xp_fill_color"),
            new Choice("avatar_outline_color", "avatar_outline_color"),
            new Choice("percent_text_color", "percent_text_color"),
            new Choice("xp_text_color", "xp_text_color"), new Choice("outline_alpha", "outline_alpha"),
            new Choice("background_image", "background_image"), new Choice("outline_image", "outline_image"),
            new Choice("xp_outline_image", "xp_outline_image"),
            new Choice("xp_empty_image", "xp_empty_image"), new Choice("xp_fill_image", "xp_fill_image"),
            new Choice("avatar_outline_image", "avatar_outline_image"), new Choice("wrapper", "wrapper"),
            new Choice("wrapper_opacity", "wrapper_opacity") };

    public static String grabImageURL(final String txt) {
        var imageStr = "";
        final var urlStr = URLEncoder.encode(txt, StandardCharsets.UTF_8);
        try {
            if (ImageIO.read(new URL(urlStr)) != null) {
                imageStr = urlStr;
            }
        } catch (final IOException e) {
            imageStr = "";
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
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "key", "The rank card configuration option", true)
                .addChoices(CHOICES));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        /*
         * final OptionMapping keyOption = ctx.getEvent().getOption("key"); String key =
         * ""; if (keyOption != null) { key = keyOption.getAsString(); }
         * 
         * if (key.isBlank()) { final var embed = new
         * EmbedBuilder().setColor(Color.BLUE).setTimestamp(Instant.now())
         * .setAuthor(ctx.getAuthor().getName(), null,
         * ctx.getAuthor().getEffectiveAvatarUrl());
         * embed.setDescription("__Example Values__:\n" + "**colorAsHex**: FFAB15\n" +
         * "**numberFrom0to255**: 187\n" +
         * "**fileOrURL**: https://media.discordapp.net/attachments/849775437021839390/857786266216955964/unknown.png"
         * );
         * 
         * embed.addField("Background Color", "/rankcard bgColor colorAsHex", true);
         * embed.addField("Outline Color", "/rankcard outColor colorAsHex", true);
         * embed.addField("Rank Text Color", "/rankcard rankTextColor colorAsHex",
         * true); embed.addField("Level Text Color",
         * "/rankcard levelTextColor colorAsHex", true);
         * embed.addField("XP Bar Outline Color", "/rankcard xpOutColor colorAsHex",
         * true); embed.addField("XP Bar Empty Color",
         * "/rankcard xpEmptyColor colorAsHex", true);
         * embed.addField("XP Bar Fill Color", "/rankcard xpFillColor colorAsHex",
         * true); embed.addField("Avatar Outline Color",
         * "/rankcard avatarOutColor colorAsHex", true);
         * embed.addField("Percent Text Color", "/rankcard percentTextColor colorAsHex",
         * true); embed.addField("XP Text Color", "/rankcard xpTextColor colorAsHex",
         * true); embed.addField("Outline Opacity",
         * "/rankcard outAlpha numberFrom0to255", true);
         * 
         * embed.addBlankField(false);
         * 
         * // Premium embed.addField("Background Image (Premium)",
         * "/rankcard bgImg fileOrURL", true); embed.addField("Outline Image (Premium)",
         * "/rankcard outImg fileOrURL", true);
         * embed.addField("XP Bar Outline Image (Premium)",
         * "/rankcard xpOutImg fileOrURL", true);
         * embed.addField("XP Bar Empty Image (Premium)",
         * "/rankcard xpEmptyImg fileOrURL", true);
         * embed.addField("XP Bar Fill Image (Premium)",
         * "/rankcard xpFillImg fileOrURL", true);
         * embed.addField("Avatar Outline Image (Premium)",
         * "/rankcard avatarOutImg fileOrURL", true);
         * 
         * embed.addField("Randomize Colors", "/rankcard randomize", false);
         * 
         * ctx.getEvent().deferReply().addEmbeds(embed.build()).mentionRepliedUser(false
         * ).queue( hook ->
         * hook.editOriginal("The options for customizing your rank card are: ").queue()
         * ); } else { final var member = ctx.getMember(); final var card =
         * LEVELLING_MANAGER.getOrCreateCard(member);
         * 
         * switch (key) { case "bgColor": if (args.length == 1) {
         * ctx.getEvent().deferReply(true).
         * setContent("Your Rank Card's Background Colour is (Red: " +
         * card.backgroundColour.getRed() + ", Green: " +
         * card.backgroundColour.getGreen() + ", Blue: " +
         * card.backgroundColour.getBlue() + ")").mentionRepliedUser(false) .queue();
         * return; }
         * 
         * try { card.backgroundColour = Color.decode(args[1]); } catch (final
         * NumberFormatException e) { card.backgroundColour =
         * RankCard.DEFAULT.backgroundColour; }
         * 
         * ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Background Colour has been changed to: (Red: "
         * + card.backgroundColour.getRed() + ", Green: " +
         * card.backgroundColour.getGreen() + ", Blue: " +
         * card.backgroundColour.getBlue() + ")") .mentionRepliedUser(false).queue();
         * 
         * LEVELLING_MANAGER.updateCard(member, card); break; case "outColor": if
         * (args.length == 1) { ctx.getEvent().deferReply(true).
         * setContent("Your Rank Card's Outline Colour is (Red: " +
         * card.outlineColour.getRed() + ", Green: " + card.outlineColour.getGreen() +
         * ", Blue: " + card.outlineColour.getBlue() + ")").mentionRepliedUser(false)
         * .queue(); return; }
         * 
         * try { card.outlineColour = Color.decode(args[1]); } catch (final
         * NumberFormatException e) { card.outlineColour =
         * RankCard.DEFAULT.outlineColour; }
         * 
         * ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Outline Colour has been changed to: (Red: " +
         * card.outlineColour.getRed() + ", Green: " + card.outlineColour.getGreen() +
         * ", Blue: " + card.outlineColour.getBlue() + ")")
         * .mentionRepliedUser(false).queue();
         * 
         * LEVELLING_MANAGER.updateCard(member, card); break; case "rankTextColor": if
         * (args.length == 1) { ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Rank Text Colour is (Red: " +
         * card.rankTextColour.getRed() + ", Green: " + card.rankTextColour.getGreen() +
         * ", Blue: " + card.rankTextColour.getBlue() + ")")
         * .mentionRepliedUser(false).queue(); return; }
         * 
         * try { card.rankTextColour = Color.decode(args[1]); } catch (final
         * NumberFormatException e) { card.rankTextColour =
         * RankCard.DEFAULT.rankTextColour; }
         * 
         * ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Rank Text Colour has been changed to: (Red: " +
         * card.rankTextColour.getRed() + ", Green: " + card.rankTextColour.getGreen() +
         * ", Blue: " + card.rankTextColour.getBlue() + ")")
         * .mentionRepliedUser(false).queue();
         * 
         * LEVELLING_MANAGER.updateCard(member, card); break; case "levelTextColor": if
         * (args.length == 1) { ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Level Text Colour is (Red: " +
         * card.levelTextColour.getRed() + ", Green: " + card.levelTextColour.getGreen()
         * + ", Blue: " + card.levelTextColour.getBlue() + ")")
         * .mentionRepliedUser(false).queue(); return; }
         * 
         * try { card.levelTextColour = Color.decode(args[1]); } catch (final
         * NumberFormatException e) { card.levelTextColour =
         * RankCard.DEFAULT.levelTextColour; }
         * 
         * ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Level Text Colour has been changed to: (Red: "
         * + card.levelTextColour.getRed() + ", Green: " +
         * card.levelTextColour.getGreen() + ", Blue: " + card.levelTextColour.getBlue()
         * + ")") .mentionRepliedUser(false).queue();
         * 
         * LEVELLING_MANAGER.updateCard(member, card); break; case "xpOutColor": if
         * (args.length == 1) { ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's XP Bar Outline Colour is (Red: " +
         * card.xpOutlineColour.getRed() + ", Green: " + card.xpOutlineColour.getGreen()
         * + ", Blue: " + card.xpOutlineColour.getBlue() + ")")
         * .mentionRepliedUser(false).queue(); return; }
         * 
         * try { card.xpOutlineColour = Color.decode(args[1]); } catch (final
         * NumberFormatException e) { card.xpOutlineColour =
         * RankCard.DEFAULT.xpOutlineColour; }
         * 
         * ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's XP Bar Outline Colour has been changed to: (Red: "
         * + card.xpOutlineColour.getRed() + ", Green: " +
         * card.xpOutlineColour.getGreen() + ", Blue: " + card.xpOutlineColour.getBlue()
         * + ")") .mentionRepliedUser(false).queue();
         * 
         * LEVELLING_MANAGER.updateCard(member, card); break; case "xpEmptyColor": if
         * (args.length == 1) { ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's XP Bar Empty Colour is (Red: " +
         * card.xpEmptyColour.getRed() + ", Green: " + card.xpEmptyColour.getGreen() +
         * ", Blue: " + card.xpEmptyColour.getBlue() + ")")
         * .mentionRepliedUser(false).queue(); return; }
         * 
         * try { card.xpEmptyColour = Color.decode(args[1]); } catch (final
         * NumberFormatException e) { card.xpEmptyColour =
         * RankCard.DEFAULT.xpEmptyColour; }
         * 
         * ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's XP Bar Empty Colour has been changed to: (Red: "
         * + card.xpEmptyColour.getRed() + ", Green: " + card.xpEmptyColour.getGreen() +
         * ", Blue: " + card.xpEmptyColour.getBlue() + ")")
         * .mentionRepliedUser(false).queue();
         * 
         * LEVELLING_MANAGER.updateCard(member, card); break; case "xpFillColor": if
         * (args.length == 1) { ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's XP Bar Fill Colour is (Red: " +
         * card.xpFillColour.getRed() + ", Green: " + card.xpFillColour.getGreen() +
         * ", Blue: " + card.xpFillColour.getBlue() + ")")
         * .mentionRepliedUser(false).queue(); return; }
         * 
         * try { card.xpFillColour = Color.decode(args[1]); } catch (final
         * NumberFormatException e) { card.xpFillColour = RankCard.DEFAULT.xpFillColour;
         * }
         * 
         * ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's XP Bar Fill Colour has been changed to: (Red: "
         * + card.xpFillColour.getRed() + ", Green: " + card.xpFillColour.getGreen() +
         * ", Blue: " + card.xpFillColour.getBlue() + ")")
         * .mentionRepliedUser(false).queue();
         * 
         * LEVELLING_MANAGER.updateCard(member, card); break; case "avatarOutColor": if
         * (args.length == 1) { ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Avatar Outline Colour is (Red: " +
         * card.avatarOutlineColour.getRed() + ", Green: " +
         * card.avatarOutlineColour.getGreen() + ", Blue: " +
         * card.avatarOutlineColour.getBlue() + ")") .mentionRepliedUser(false).queue();
         * return; }
         * 
         * try { card.avatarOutlineColour = Color.decode(args[1]); } catch (final
         * NumberFormatException e) { card.avatarOutlineColour =
         * RankCard.DEFAULT.avatarOutlineColour; }
         * 
         * ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Avatar Outline Colour has been changed to: (Red: "
         * + card.avatarOutlineColour.getRed() + ", Green: " +
         * card.avatarOutlineColour.getGreen() + ", Blue: " +
         * card.avatarOutlineColour.getBlue() + ")") .mentionRepliedUser(false).queue();
         * 
         * LEVELLING_MANAGER.updateCard(member, card); break; case "percentTextColor":
         * if (args.length == 1) { ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Percent Text Colour is " +
         * card.percentTextColour) .mentionRepliedUser(false).queue(); return; }
         * 
         * try { card.percentTextColour = Color.decode(args[1]); } catch (final
         * NumberFormatException e) { card.percentTextColour =
         * RankCard.DEFAULT.percentTextColour; }
         * 
         * ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Percent Text Colour has been changed to: (Red: "
         * + card.percentTextColour.getRed() + ", Green: " +
         * card.percentTextColour.getGreen() + ", Blue: " +
         * card.percentTextColour.getBlue() + ")") .mentionRepliedUser(false).queue();
         * 
         * LEVELLING_MANAGER.updateCard(member, card); break; case "xpTextColor": if
         * (args.length == 1) { ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's XP Text Colour is " + card.xpTextColour)
         * .mentionRepliedUser(false).queue(); return; }
         * 
         * try { card.xpTextColour = Color.decode(args[1]); } catch (final
         * NumberFormatException e) { card.xpTextColour = RankCard.DEFAULT.xpTextColour;
         * }
         * 
         * ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's XP Text Colour has been changed to: (Red: " +
         * card.xpTextColour.getRed() + ", Green: " + card.xpTextColour.getGreen() +
         * ", Blue: " + card.xpTextColour.getBlue() + ")")
         * .mentionRepliedUser(false).queue();
         * 
         * LEVELLING_MANAGER.updateCard(member, card); break; case "nameTextColour": if
         * (args.length == 1) { ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Name Text Colour is " + card.nameTextColour)
         * .mentionRepliedUser(false).queue(); return; }
         * 
         * try { card.nameTextColour = Color.decode(args[1]); } catch (final
         * NumberFormatException e) { card.nameTextColour =
         * RankCard.DEFAULT.nameTextColour; }
         * 
         * ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Name Text Colour has been changed to: (Red: " +
         * card.nameTextColour.getRed() + ", Green: " + card.nameTextColour.getGreen() +
         * ", Blue: " + card.nameTextColour.getBlue() + ")")
         * .mentionRepliedUser(false).queue();
         * 
         * LEVELLING_MANAGER.updateCard(member, card); break; case "outAlpha": if
         * (args.length == 1) { ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Outline Opacity is " + card.outlineOpacity *
         * 255) .mentionRepliedUser(false).queue(); return; }
         * 
         * var opacity = 0; try { opacity = Integer.parseInt(args[1]); } catch (final
         * NumberFormatException e) { opacity = 255; }
         * 
         * if (opacity < 0) { opacity = 0; } if (opacity > 255) { opacity = 255; }
         * 
         * card.outlineOpacity = opacity / 255f;
         * 
         * ctx.getEvent().deferReply(true).setContent(
         * "Your Rank Card's Outline Opacity has been changed to: " +
         * card.outlineOpacity * 255) .mentionRepliedUser(false).queue();
         * 
         * LEVELLING_MANAGER.updateCard(member, card); break; case "bgImg": if
         * (member.getTimeBoosted() != null || member.isOwner() ||
         * member.getGuild().getIdLong() == 819294753732296776L) { if (args.length == 1)
         * { ctx.getEvent().deferReply(true) .setContent( card.backgroundImage.isBlank()
         * ? "You have no Background Image set!" : "Your Background Image is: <" +
         * card.backgroundImage + ">") .mentionRepliedUser(false).queue(); return; }
         * 
         * card.backgroundImage = grabImageURL(args[2]); ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Background Image has been updated!")
         * .mentionRepliedUser(false).queue(); LEVELLING_MANAGER.updateCard(member,
         * card); } else { ctx.getEvent().deferReply(true)
         * .setContent("You must be a premium member in order to set this value.")
         * .mentionRepliedUser(false).queue(); } break; case "outImg": if
         * (member.getTimeBoosted() != null || member.isOwner() ||
         * member.getGuild().getIdLong() == 819294753732296776L) { if (args.length == 1)
         * { ctx.getEvent().deferReply(true) .setContent(card.outlineImage.isBlank() ?
         * "You have no Outline Image set!" : "Your Outline Image is: <" +
         * card.outlineImage + ">") .mentionRepliedUser(false).queue(); return; }
         * 
         * card.outlineImage = grabImageURL(args[2]); ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Outline Image has been updated!")
         * .mentionRepliedUser(false).queue(); LEVELLING_MANAGER.updateCard(member,
         * card); } else { ctx.getEvent().deferReply(true)
         * .setContent("You must be a premium member in order to set this value.")
         * .mentionRepliedUser(false).queue(); } break; case "xpOutImg": if
         * (member.getTimeBoosted() != null || member.isOwner() ||
         * member.getGuild().getIdLong() == 819294753732296776L) { if (args.length == 1)
         * { ctx.getEvent().deferReply(true) .setContent(card.xpOutlineImage.isBlank() ?
         * "You have no XP Bar Outline Image set!" : "Your XP Bar Outline Image is: <" +
         * card.xpOutlineImage + ">") .mentionRepliedUser(false).queue(); return; }
         * 
         * card.xpOutlineImage = grabImageURL(args[2]); ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's XP Bar Outline Image has been updated!")
         * .mentionRepliedUser(false).queue(); LEVELLING_MANAGER.updateCard(member,
         * card); } else { ctx.getEvent().deferReply(true)
         * .setContent("You must be a premium member in order to set this value.")
         * .mentionRepliedUser(false).queue(); } break; case "xpEmptyImg": if
         * (member.getTimeBoosted() != null || member.isOwner() ||
         * member.getGuild().getIdLong() == 819294753732296776L) { if (args.length == 1)
         * { ctx.getEvent().deferReply(true) .setContent( card.xpEmptyImage.isBlank() ?
         * "You have no XP Bar Empty Image set!" : "Your XP Bar Empty Image is: <" +
         * card.xpEmptyImage + ">") .mentionRepliedUser(false).queue(); return; }
         * 
         * card.xpEmptyImage = grabImageURL(args[2]); ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's XP Bar Empty Image has been updated!")
         * .mentionRepliedUser(false).queue(); LEVELLING_MANAGER.updateCard(member,
         * card); } else { ctx.getEvent().deferReply(true)
         * .setContent("You must be a premium member in order to set this value.")
         * .mentionRepliedUser(false).queue(); } break; case "xpFillImg": if
         * (member.getTimeBoosted() != null || member.isOwner() ||
         * member.getGuild().getIdLong() == 819294753732296776L) { if (args.length == 1)
         * { ctx.getEvent().deferReply(true) .setContent(card.xpFillImage.isBlank() ?
         * "You have no XP Bar Fill Image set!" : "Your XP Bar Fill Image is: <" +
         * card.xpFillImage + ">") .mentionRepliedUser(false).queue(); return; }
         * 
         * card.xpFillImage = grabImageURL(args[2]); ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's XP Bar Fill Image has been updated!")
         * .mentionRepliedUser(false).queue(); LEVELLING_MANAGER.updateCard(member,
         * card); } else { ctx.getEvent().deferReply(true)
         * .setContent("You must be a premium member in order to set this value.")
         * .mentionRepliedUser(false).queue(); } break; case "avatarOutImg": if
         * (member.getTimeBoosted() != null || member.isOwner() ||
         * member.getGuild().getIdLong() == 819294753732296776L) { if (args.length == 1)
         * { ctx.getEvent().deferReply(true)
         * .setContent(card.avatarOutlineImage.isBlank() ?
         * "You have no Avatar Outline Image set!" : "Your Avatar Outline Image is: <" +
         * card.avatarOutlineImage + ">") .mentionRepliedUser(false).queue(); return; }
         * 
         * card.avatarOutlineImage = grabImageURL(args[2]);
         * ctx.getEvent().deferReply(true)
         * .setContent("Your Rank Card's Avatar Outline Image has been updated!")
         * .mentionRepliedUser(false).queue(); LEVELLING_MANAGER.updateCard(member,
         * card); } else { ctx.getEvent().deferReply(true)
         * .setContent("You must be a premium member in order to set this value.")
         * .mentionRepliedUser(false).queue(); } break; case "randomize":
         * card.backgroundColour = BotUtils.generateRandomColor(); card.outlineColour =
         * BotUtils.generateRandomColor(); card.rankTextColour =
         * BotUtils.generateRandomColor(); card.levelTextColour =
         * BotUtils.generateRandomColor(); card.xpOutlineColour =
         * BotUtils.generateRandomColor(); card.xpEmptyColour =
         * BotUtils.generateRandomColor(); card.xpFillColour =
         * BotUtils.generateRandomColor(); card.avatarOutlineColour =
         * BotUtils.generateRandomColor(); card.percentTextColour =
         * BotUtils.generateRandomColor(); card.xpTextColour =
         * BotUtils.generateRandomColor();
         * 
         * ctx.getEvent().deferReply(true).
         * setContent("Your Rank Card's colours have been updated!")
         * .mentionRepliedUser(false).queue(); LEVELLING_MANAGER.updateCard(member,
         * card); break; case "default": break; default:
         * ctx.getEvent().deferReply(true).
         * setContent("You must supply a valid setting to change. Use `" +
         * "/rankcard` for more information!").mentionRepliedUser(false).queue(); break;
         * } }
         */
    }
}
