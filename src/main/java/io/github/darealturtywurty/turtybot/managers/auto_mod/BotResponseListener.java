package io.github.darealturtywurty.turtybot.managers.auto_mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BotResponseListener extends ListenerAdapter {

    // Map<GuildID, Map<ChannelID, List<MessageID>>>
    public static final Map<Long, Map<Long, List<Long>>> MESSAGES_BY_GUILD_CHANNEL = new HashMap<>();

    public static int cleanResponses(final TextChannel channel, final int amount) {
        final long guildId = channel.getGuild().getIdLong();
        final long channelId = channel.getIdLong();
        if (!MESSAGES_BY_GUILD_CHANNEL.containsKey(guildId)) {
            initialize(guildId);
            return 0;
        }

        final Map<Long, List<Long>> messagesByChannel = MESSAGES_BY_GUILD_CHANNEL.get(guildId);
        if (!messagesByChannel.containsKey(channelId))
            return 0;

        final List<Long> messages = messagesByChannel.get(channelId);
        if (amount >= messages.size()) {
            channel.deleteMessagesByIds(messages.stream().map(String::valueOf).collect(Collectors.toList()))
                    .queue();
            final int count = messages.size();
            messages.clear();
            return count;
        }

        int counter = 0;
        for (var messageIndex = 0; messageIndex < amount; messageIndex++) {
            if (messages.get(messageIndex) == null || messages.get(messageIndex) == 0L)
                return messageIndex;

            channel.deleteMessageById(messages.get(messageIndex)).queue();
            counter++;
        }

        messages.subList(messages.size() - counter, messages.size()).clear();

        return counter;
    }

    public static void initialize(final long guildId) {
        MESSAGES_BY_GUILD_CHANNEL.put(guildId, new HashMap<>());
    }

    @Override
    public void onGuildMessageDelete(final GuildMessageDeleteEvent event) {
        super.onGuildMessageDelete(event);
        final long guildId = event.getGuild().getIdLong();
        if (!MESSAGES_BY_GUILD_CHANNEL.containsKey(guildId)) {
            initialize(guildId);
            return;
        }

        final long channelId = event.getChannel().getIdLong();

        final Map<Long, List<Long>> messagesByChannel = MESSAGES_BY_GUILD_CHANNEL.get(guildId);
        if (!messagesByChannel.containsKey(channelId))
            return;

        final long messageId = event.getMessageIdLong();

        final List<Long> messages = messagesByChannel.get(channelId);
        if (messages.contains(messageId)) {
            messages.remove(messageId);
        }
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
        final long guildId = event.getGuild().getIdLong();

        if (event.getAuthor().getIdLong() == event.getGuild().getSelfMember().getIdLong()) {
            if (!MESSAGES_BY_GUILD_CHANNEL.containsKey(guildId)) {
                initialize(guildId);
            }

            final long channelId = event.getChannel().getIdLong();
            final long messageId = event.getMessageIdLong();

            final Map<Long, List<Long>> messagesByChannel = MESSAGES_BY_GUILD_CHANNEL.get(guildId);
            if (!messagesByChannel.containsKey(channelId)) {
                final var messages = new ArrayList<Long>();
                messages.add(messageId);
                messagesByChannel.put(channelId, messages);
                return;
            }

            final List<Long> messages = messagesByChannel.get(channelId);
            if (messages.size() >= 99) {
                messages.remove(0);
            }

            messages.add(messageId);
        }
    }
}
