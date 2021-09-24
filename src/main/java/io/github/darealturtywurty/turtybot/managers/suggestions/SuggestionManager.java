package io.github.darealturtywurty.turtybot.managers.suggestions;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.github.darealturtywurty.turtybot.util.Constants.ColorConstants;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import io.github.darealturtywurty.turtybot.util.data.SuggestionData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

public class SuggestionManager extends ListenerAdapter {

    public SuggestionManager(final JDA bot) {
        bot.addEventListener(new SuggestionButtonListener());
    }

    public static SuggestionData createSuggestion(final Guild guild, final String suggestion,
            final String mediaLink, final long authorId) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong());
        final TextChannel suggestionsChannel = BotUtils.getSuggestionsChannel(guild);
        final var embed = new EmbedBuilder();
        embed.setTitle("Sugestion #" + info.suggestionData.size());
        embed.setDescription(suggestion);
        embed.setImage(mediaLink);
        embed.setColor(ColorConstants.DARK_BLUE);
        embed.setTimestamp(Instant.now());
        embed.setFooter(
                guild.getJDA().getUserById(authorId).getName() + "#"
                        + guild.getJDA().getUserById(authorId).getDiscriminator(),
                guild.getJDA().getUserById(authorId).getEffectiveAvatarUrl());
        final var atomicMessage = new AtomicReference<Message>();
        suggestionsChannel.sendMessageEmbeds(embed.build()).queue(msg -> {
            atomicMessage.set(msg);
            msg.editMessage("** **")
                    .setActionRows(ActionRow.of(
                            Button.success("approve_" + msg.getIdLong(), Emoji.fromMarkdown("☑")),
                            Button.secondary("consider_" + msg.getIdLong(), Emoji.fromMarkdown("🤔")),
                            Button.danger("deny_" + msg.getIdLong(), Emoji.fromMarkdown("✖")),
                            Button.danger("delete_" + msg.getIdLong(), Emoji.fromMarkdown("⚠"))))
                    .queue();
        });

        final var data = new SuggestionData(SuggestionResponse.NONE, info.suggestionData.size(), authorId,
                () -> atomicMessage.get().getIdLong());
        info.suggestionData.add(data);
        return data;
    }

    public static void editAndRepeat(final TextChannel channel, final long latestMessageId,
            final int previousNumber) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(channel.getGuild().getIdLong());
        channel.getHistoryAfter(latestMessageId, 100).queue(history -> {
            final List<Message> retreived = history.getRetrievedHistory();
            long messageId = latestMessageId;
            final var atomicNumber = new AtomicInteger(previousNumber);
            boolean complete = false;
            if (retreived.size() < 100) {
                complete = true;
            }

            for (final Message message : retreived) {
                messageId = message.getIdLong();
                final MessageEmbed embed = message.getEmbeds().get(0);
                channel.editMessageEmbedsById(message.getIdLong(),
                        new EmbedBuilder(embed).setTitle("Suggestion #" + atomicNumber.get()).build())
                        .queue();
                info.suggestionData.stream().filter(data -> data.suggestionNumber == atomicNumber.get())
                        .forEachOrdered(data -> data.suggestionNumber = atomicNumber.getAndIncrement());
            }

            if (!complete) {
                editAndRepeat(channel, messageId, atomicNumber.get());
            }
        });
    }

    @Override
    public void onGuildMessageDelete(final GuildMessageDeleteEvent event) {
        super.onGuildMessageDelete(event);
        final TextChannel suggestionsChannel = BotUtils.getSuggestionsChannel(event.getGuild());
        if (suggestionsChannel == null || event.getChannel().getIdLong() != suggestionsChannel.getIdLong())
            return;

        BotUtils.getMessageBeforeId(suggestionsChannel, event.getMessageIdLong(), message -> {
            final MessageEmbed embed = message.getEmbeds().get(0);
            final long messageId = message.getIdLong();
            editAndRepeat(BotUtils.getSuggestionsChannel(event.getGuild()), messageId,
                    Integer.parseInt(embed.getTitle().split("#")[1]));
            return null;
        });
    }
}
