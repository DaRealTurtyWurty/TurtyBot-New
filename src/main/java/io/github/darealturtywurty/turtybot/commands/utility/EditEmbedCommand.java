package io.github.darealturtywurty.turtybot.commands.utility;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import io.github.darealturtywurty.turtybot.util.data.UserEmbedData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

//@RegisterBotCmd
public class EditEmbedCommand implements GuildCommand {

    private static final Choice[] COLOR_CHOICES = { new Choice("blue", "blue"), new Choice("brown", "brown"),
            new Choice("cyan", "cyan"), new Choice("dark_blue", "dark_blue"),
            new Choice("dark_brown", "dark_brown"), new Choice("dark_gray", "dark_gray"),
            new Choice("dark_green", "dark_green"), new Choice("dark_red", "dark_red"),
            new Choice("gray", "gray"), new Choice("green", "green"), new Choice("gold", "gold"),
            new Choice("light_blue", "light_blue"), new Choice("light_brown", "light_green"),
            new Choice("light_gray", "light_gray"), new Choice("light_orange", "light_orange"),
            new Choice("light_yellow", "light_yellow"), new Choice("magenta", "magenta"),
            new Choice("orange", "orange"), new Choice("pink", "pink"), new Choice("purple", "purple"),
            new Choice("red", "red"), new Choice("white", "white"), new Choice("yellow", "yellow"),
            new Choice("author", "author") };

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Allows you to edit the content of the currently editing embed.";
    }

    @Override
    public String getName() {
        return "editembed";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.SUB_COMMAND, "type", "The type.", true));
    }

    @Override
    public List<SubcommandData> getSubcommandData() {
        return List.of(
                new SubcommandData("title", "Title").addOption(OptionType.STRING, "text",
                        "The text that should be used as the title.", true)
                        .addOption(OptionType.STRING, "url",
                                "The clickable URL used for the title of this embed.", false),
                new SubcommandData("description", "Description")
                        .addOption(OptionType.STRING, "text", "The text to add to the description.", true)
                        .addOption(OptionType.BOOLEAN, "append",
                                "Whether to append to or overwrite any existing description (default: overwrite).",
                                false),
                new SubcommandData("color", "Color").addOptions(
                        new OptionData(OptionType.STRING, "embed_color", "The color of the embed.", true)
                                .addChoices(COLOR_CHOICES)),
                new SubcommandData("timestamp", "Timestamp").addOptions(new OptionData(OptionType.STRING,
                        "time", "The timestamp of this embed. Must be separated with commas.", true)
                                .addChoice("now", "now")),
                new SubcommandData("author", "Author")
                        .addOption(OptionType.STRING, "name", "The name of the author.", true)
                        .addOption(OptionType.STRING, "url", "The clickable URL used for the author.", false)
                        .addOption(OptionType.STRING, "icon_url", "The URL image of the icon.", false),
                new SubcommandData("thumbnail", "Thumbnail").addOption(OptionType.STRING, "url",
                        "The URL of this embed's thumbnail image.", true),
                new SubcommandData("image", "Image").addOption(OptionType.STRING, "url",
                        "The URL of this embed's image.", true),
                new SubcommandData("footer", "Footer")
                        .addOption(OptionType.STRING, "text", "The text in this embed's footer.", true)
                        .addOption(OptionType.STRING, "url", "The URL image of the icon.", false),
                new SubcommandData("field", "Field")
                        .addOption(OptionType.STRING, "title", "The title of this embed field.", true)
                        .addOption(OptionType.STRING, "description", "The description of this embed field.",
                                true)
                        .addOption(OptionType.BOOLEAN, "inline",
                                "Whether or not this field will inline with other fields.", false),
                new SubcommandData("blank_field", "Blank Field").addOption(OptionType.BOOLEAN, "inline",
                        "Whether or not this field will inline with other fields.", false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final SlashCommandEvent event = ctx.getEvent();
        final String subcommand = event.getSubcommandName();
        final GuildInfo info = CoreBotUtils.GUILDS.get(ctx.getGuild().getIdLong());
        if (!info.userEmbeds.containsKey(ctx.getAuthor().getIdLong())) {
            event.deferReply(true).setContent("You must have an embed to edit! "
                    + "You can use `/embeds create <name>` to start making an embed.").queue();
            return;
        }

        final UserEmbedData embedData = info.userEmbeds.get(ctx.getAuthor().getIdLong());
        if (embedData.currentlyEditing == null || embedData.currentlyEditing.getRight() == null) {
            event.deferReply(true).setContent("You must be currently editing an embed to use this command! "
                    + "You can use `/embeds edit <name>` to start editing an embed.").queue();
            return;
        }

        final EmbedBuilder embed = embedData.currentlyEditing.getRight();

        switch (subcommand) {
            case "title" -> {
                final String text = event.getOption("text").getAsString();
                final OptionMapping url = event.getOption("url");
                embed.setTitle(text,
                        url != null ? URLEncoder.encode(url.getAsString(), StandardCharsets.UTF_8) : null);
            }

            case "description" -> {
                final String text = event.getOption("text").getAsString();
                final OptionMapping shouldAppendOption = event.getOption("append");
                final boolean shouldAppend = shouldAppendOption != null && shouldAppendOption.getAsBoolean();
                if (shouldAppend) {
                    embed.appendDescription(text);
                } else {
                    embed.setDescription(text);
                }
            }

            case "color" -> {
                final String color = event.getOption("embed_color").getAsString();
                if ("author".equalsIgnoreCase(color)) {
                    embed.setColor(ctx.getMember().getColorRaw());
                } else {
                    embed.setColor(BotUtils.parseColor(color));
                }
            }

            case "timestamp" -> {
                final String timestamp = event.getOption("time").getAsString();
                if ("now".equalsIgnoreCase(timestamp)) {
                    embed.setTimestamp(Instant.now());
                } else {
                    embed.setTimestamp(Instant.ofEpochMilli(BotUtils.parseTime(timestamp)));
                }
            }

            case "author" -> {
                final String author = event.getOption("name").getAsString();
                final OptionMapping url = event.getOption("url");
                final OptionMapping iconURL = event.getOption("icon_url");
                embed.setAuthor(author,
                        url != null ? URLEncoder.encode(url.getAsString(), StandardCharsets.UTF_8) : null,
                        iconURL != null ? URLEncoder.encode(iconURL.getAsString(), StandardCharsets.UTF_8)
                                : null);
            }

            case "thumbnail" -> {
                final String url = event.getOption("url").getAsString();
                embed.setThumbnail(URLEncoder.encode(url, StandardCharsets.UTF_8));
            }

            case "image" -> {
                final String url = event.getOption("url").getAsString();
                embed.setImage(URLEncoder.encode(url, StandardCharsets.UTF_8));
            }

            case "footer" -> {
                final String text = event.getOption("text").getAsString();
                final OptionMapping url = event.getOption("url");
                embed.setFooter(text,
                        url != null ? URLEncoder.encode(url.getAsString(), StandardCharsets.UTF_8) : null);
            }

            case "field" -> {
                final String title = event.getOption("title").getAsString();
                final String description = event.getOption("description").getAsString();
                final OptionMapping inline = event.getOption("inline");
                embed.addField(title, description, inline != null && inline.getAsBoolean());
            }

            case "blank_field" -> {
                final OptionMapping inline = event.getOption("inline");
                embed.addBlankField(inline != null && inline.getAsBoolean());
            }

            default -> {
                event.deferReply(true).setContent("You must supply a valid type!").queue();
                return;
            }
        }

        embedData.updateEmbed(embedData.currentlyEditing.getLeft(), embed);
        event.deferReply(true).addEmbeds(embed.build()).queue();
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }
}
