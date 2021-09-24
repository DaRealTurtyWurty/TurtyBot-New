package io.github.darealturtywurty.turtybot.managers.auto_mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BotResponseListener extends ListenerAdapter {

    public static int cleanResponses(final TextChannel channel, final int amount) {
        final long guildId = channel.getGuild().getIdLong();
        final long channelId = channel.getIdLong();
        final GuildInfo info = CoreBotUtils.GUILDS.get(guildId);

        final Map<Long, List<Long>> messagesByChannel = info.messagesByChannel;
        if (!messagesByChannel.containsKey(channelId))
            return 0;

        final List<Long> messages = messagesByChannel.get(channelId);
        if (amount >= messages.size()) {
            channel.deleteMessagesByIds(messages.stream().map(String::valueOf).toList()).queue();
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

    @Override
    public void onGuildMessageDelete(final GuildMessageDeleteEvent event) {
        super.onGuildMessageDelete(event);
        final long guildId = event.getGuild().getIdLong();
        final long channelId = event.getChannel().getIdLong();
        final GuildInfo info = CoreBotUtils.GUILDS.get(guildId);

        final Map<Long, List<Long>> messagesByChannel = info.messagesByChannel;
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
            final long channelId = event.getChannel().getIdLong();
            final long messageId = event.getMessageIdLong();
            final GuildInfo info = CoreBotUtils.GUILDS.get(guildId);

            final Map<Long, List<Long>> messagesByChannel = info.messagesByChannel;
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
