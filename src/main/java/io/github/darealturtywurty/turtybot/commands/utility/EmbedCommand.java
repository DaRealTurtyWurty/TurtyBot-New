package io.github.darealturtywurty.turtybot.commands.utility;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import io.github.darealturtywurty.turtybot.util.data.UserEmbedData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

//@RegisterBotCmd
public class EmbedCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Run an action on the supplied embed name.";
    }

    @Override
    public String getName() {
        return "embeds";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "action", "The action to run this command with.", true)
                        .addChoice("list", "list").addChoice("create", "create").addChoice("edit", "edit")
                        .addChoice("remove", "remove").addChoice("send", "send"),
                new OptionData(OptionType.STRING, "name",
                        "The name (unique identifier) of the embed to apply this action to.", false),
                new OptionData(OptionType.BOOLEAN, "showEveryone",
                        "Whether or not to show the embed to everyone (only works for `send`).", false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final SlashCommandEvent event = ctx.getEvent();
        final String action = event.getOption("action").getAsString();
        final GuildInfo info = CoreBotUtils.GUILDS.get(ctx.getGuild().getIdLong());
        final long authorId = ctx.getAuthor().getIdLong();
        final OptionMapping nameOption = event.getOption("name");
        switch (action) {
            case "list":
                if (!info.userEmbeds.containsKey(authorId)) {
                    event.deferReply(true).setContent(
                            "You do not have any embeds stored! Use `/embeds create <name>` to start making one.")
                            .queue();
                    return;
                }
                final var embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("List of \"" + ctx.getAuthor().getName() + "\"s saved embeds.");
                embedBuilder.setColor(ctx.getMember().getColorRaw());
                embedBuilder.setTimestamp(Instant.now());
                info.userEmbeds.get(authorId).embeds.entrySet().stream().map(Entry::getKey)
                        .sorted(Comparator.naturalOrder())
                        .forEachOrdered(name -> embedBuilder.appendDescription(", `" + name + "`"));
                event.deferReply().addEmbeds(embedBuilder.build()).queue();
                break;
            case "create":
                if (nameOption == null) {
                    event.deferReply(true)
                            .setContent("You must supply the name of the embed you want to create!").queue();
                    return;
                }

                final String createName = nameOption.getAsString();
                UserEmbedData createEmbedData;
                if (!info.userEmbeds.containsKey(authorId)) {
                    createEmbedData = new UserEmbedData(ctx.getAuthor());
                    info.userEmbeds.put(authorId, createEmbedData);
                } else {
                    createEmbedData = info.userEmbeds.get(authorId);
                }

                final boolean createSuccess = createEmbedData.createEmbed(createName, new EmbedBuilder());
                final String createAlteredName = createName.trim().toLowerCase();
                if (createSuccess) {
                    event.deferReply(true)
                            .setContent("An embed by the name of: `" + createAlteredName
                                    + "` has successfully been created! You can now use `/editembed`"
                                    + " to have your embed contain information.")
                            .queue();
                    return;
                }

                event.deferReply(true).setContent(
                        "There was an issue processing your request. Make sure that you have put a name for your embed, "
                                + "and that an embed with this name does not already exist!")
                        .queue();
                break;
            case "edit":
                if (!info.userEmbeds.containsKey(authorId)) {
                    event.deferReply(true).setContent(
                            "You do not have any embeds stored! Use `/embeds create <name>` to start making one.")
                            .queue();
                    return;
                }

                if (nameOption == null && info.userEmbeds.get(authorId).currentlyEditing == null) {
                    event.deferReply(true)
                            .setContent("You must supply the name of the embed you want to edit!").queue();
                    return;
                }

                final String editName = nameOption == null
                        ? info.userEmbeds.get(authorId).currentlyEditing.getKey()
                        : nameOption.getAsString();
                UserEmbedData editEmbedData;
                if (!info.userEmbeds.containsKey(authorId)) {
                    editEmbedData = new UserEmbedData(ctx.getAuthor());
                    info.userEmbeds.put(authorId, editEmbedData);
                } else {
                    editEmbedData = info.userEmbeds.get(authorId);
                }

                final boolean editSuccess = editEmbedData.editEmbed(editName) != null;
                final String editAlteredName = editName.trim().toLowerCase();
                if (editSuccess) {
                    event.deferReply(true).setContent("You have now started editing: `" + editAlteredName
                            + "`! You can now use `/editembed` to have your embed contain information.")
                            .queue();
                    return;
                }

                event.deferReply(true).setContent("There was an issue processing your request. "
                        + "Make sure that you have entered a name of the embed that you want to edit, "
                        + "and that an embed with this name exists.").queue();
                break;
            case "remove":
                if (!info.userEmbeds.containsKey(authorId)) {
                    event.deferReply(true).setContent(
                            "You do not have any embeds stored! Use `/embeds create <name>` to start making one.")
                            .queue();
                    return;
                }

                if (nameOption == null && info.userEmbeds.get(authorId).currentlyEditing == null) {
                    event.deferReply(true)
                            .setContent("You must supply the name of the embed you want to remove!").queue();
                    return;
                }

                final String removeName = nameOption == null
                        ? info.userEmbeds.get(authorId).currentlyEditing.getKey()
                        : nameOption.getAsString();
                final String removeAlteredName = removeName.trim().toLowerCase();
                UserEmbedData removeEmbedData;
                if (!info.userEmbeds.containsKey(authorId)) {
                    removeEmbedData = new UserEmbedData(ctx.getAuthor());
                    info.userEmbeds.put(authorId, removeEmbedData);
                } else {
                    removeEmbedData = info.userEmbeds.get(authorId);
                }

                final boolean removeSuccess = removeEmbedData.removeEmbed(removeAlteredName);
                if (removeSuccess) {
                    event.deferReply(true).setContent("I have successfully removed embed: `"
                            + removeAlteredName + "` from your embeds.").queue();
                    return;
                }

                event.deferReply(true).setContent("There was an issue processing your request. "
                        + "Make sure that you have entered a name of the embed that you want to remove, "
                        + "and that an embed with this name exists.").queue();
                break;
            case "send":
                if (!info.userEmbeds.containsKey(authorId)) {
                    event.deferReply(true).setContent(
                            "You do not have any embeds stored! Use `/embeds create <name>` to start making one.")
                            .queue();
                    return;
                }

                if (nameOption == null && info.userEmbeds.get(authorId).currentlyEditing == null) {
                    event.deferReply(true)
                            .setContent("You must supply the name of the embed you want to send!").queue();
                    return;
                }

                final String sendName = nameOption == null
                        ? info.userEmbeds.get(authorId).currentlyEditing.getKey()
                        : nameOption.getAsString();
                UserEmbedData sendEmbedData;
                if (!info.userEmbeds.containsKey(authorId)) {
                    sendEmbedData = new UserEmbedData(ctx.getAuthor());
                    info.userEmbeds.put(authorId, sendEmbedData);
                } else {
                    sendEmbedData = info.userEmbeds.get(authorId);
                }

                final OptionMapping showEveryone = event.getOption("showEveryone");
                final EmbedBuilder embed = sendEmbedData.embeds.get(sendName);
                event.deferReply(showEveryone == null || showEveryone.getAsBoolean()).addEmbeds(embed.build())
                        .queue(hook -> sendEmbedData.stopEditing());
                break;
            default:
                event.deferReply(true).setContent("You must supply a valid action!").queue();
                break;
        }
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }
}
