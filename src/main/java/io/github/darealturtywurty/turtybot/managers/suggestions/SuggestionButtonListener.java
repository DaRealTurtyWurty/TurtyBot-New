package io.github.darealturtywurty.turtybot.managers.suggestions;

import java.util.List;
import java.util.Locale;

import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.data.SuggestionData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SuggestionButtonListener extends ListenerAdapter {

    @Override
    public void onButtonClick(final ButtonClickEvent event) {
        super.onButtonClick(event);
        final TextChannel suggestionsChannel = BotUtils.getSuggestionsChannel(event.getGuild());
        if (suggestionsChannel == null
                || event.getTextChannel().getIdLong() != suggestionsChannel.getIdLong())
            return;

        final List<SuggestionData> data = CoreBotUtils.GUILDS
                .get(event.getGuild().getIdLong()).suggestionData;
        final String[] idParts = event.getButton().getId().split("_");
        switch (idParts[0].trim().toLowerCase(Locale.getDefault())) {
            case "approve" -> suggestionsChannel.retrieveMessageById(idParts[1]).queue(message -> {
                final MessageEmbed embed = message.getEmbeds().get(0);
                message.editMessageEmbeds(new EmbedBuilder(embed)
                        .addField("Approved by: " + event.getUser().getName(), "No reason.", false).build())
                        .setActionRows().queue();
                data.get(Integer
                        .parseInt(embed.getTitle().split("#")[1])).response = SuggestionResponse.APPROVED;
            });

            case "consider" -> suggestionsChannel.retrieveMessageById(idParts[1]).queue(message -> {
                final MessageEmbed embed = message.getEmbeds().get(0);
                message.editMessageEmbeds(new EmbedBuilder(embed)
                        .addField("Considered by: " + event.getUser().getName(), "No reason.", false).build())
                        .setActionRows().queue();
                data.get(Integer
                        .parseInt(embed.getTitle().split("#")[1])).response = SuggestionResponse.CONSIDERED;
            });

            case "deny" -> suggestionsChannel.retrieveMessageById(idParts[1]).queue(message -> {
                final MessageEmbed embed = message.getEmbeds().get(0);
                message.editMessageEmbeds(new EmbedBuilder(embed)
                        .addField("Denied by: " + event.getUser().getName(), "No reason.", false).build())
                        .setActionRows().queue();
                data.get(Integer
                        .parseInt(embed.getTitle().split("#")[1])).response = SuggestionResponse.DENIED;
            });

            case "delete" -> suggestionsChannel.retrieveMessageById(idParts[1]).queue(message -> {
                final MessageEmbed embed = message.getEmbeds().get(0);
                final long messageId = message.getIdLong();
                message.delete().queue();
                data.remove(Integer.parseInt(embed.getTitle().split("#")[1]));
                SuggestionManager.editAndRepeat(suggestionsChannel, messageId,
                        Integer.parseInt(embed.getTitle().split("#")[1]));
            });

            default -> {
                break;
            }
        }
    }
}
