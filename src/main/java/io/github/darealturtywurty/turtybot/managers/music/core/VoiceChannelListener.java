package io.github.darealturtywurty.turtybot.managers.music.core;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.data.Skip;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

// TODO: Maybe save to MongoDB
public class VoiceChannelListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(final GuildMessageReactionAddEvent event) {
        super.onGuildMessageReactionAdd(event);
        if (event.getUser().isBot())
            return;

        final Set<Skip> skips = CoreBotUtils.GUILDS.get(event.getGuild().getIdLong()).skips;
        final Optional<Skip> oSkip = skips.stream()
                .filter(skip -> skip.guildId == event.getGuild().getIdLong()
                        && skip.channelId == event.getChannel().getIdLong()
                        && skip.messageId == event.getMessageIdLong())
                .findFirst();
        if (oSkip.isPresent()) {
            final var skip = oSkip.get();
            final Set<Long> reactions = skip.reactions;
            if (!event.getReactionEmote().getAsReactionCode().equalsIgnoreCase(":track_next:")
                    && !event.getReactionEmote().getAsReactionCode().equalsIgnoreCase(":next_track:")) {
                event.getReaction().removeReaction().queue();
            } else {
                reactions.add(event.getMember().getIdLong());
                // TODO: Configurable
                if (reactions.size() > 5) {
                    final AtomicReference<Message> message = new AtomicReference<>();
                    event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message::set);
                    if (message.get() != null) {
                        message.get().delete().queue();
                    }

                    skips.remove(skip);
                }
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(final GuildMessageReactionRemoveEvent event) {
        super.onGuildMessageReactionRemove(event);
        if (event.getUser().isBot())
            return;

        final Set<Skip> skips = CoreBotUtils.GUILDS.get(event.getGuild().getIdLong()).skips;
        final Optional<Skip> oSkip = skips.stream()
                .filter(skip -> skip.guildId == event.getGuild().getIdLong()
                        && skip.channelId == event.getChannel().getIdLong()
                        && skip.messageId == event.getMessageIdLong())
                .findFirst();
        if (oSkip.isPresent()) {
            final var skip = oSkip.get();
            final Set<Long> reactions = skip.reactions;
            if (event.getReactionEmote().getAsReactionCode().equalsIgnoreCase(":track_next:")
                    || event.getReactionEmote().getAsReactionCode().equalsIgnoreCase(":next_track:")) {
                reactions.remove(event.getMember().getIdLong());
                final AtomicReference<Message> message = new AtomicReference<>();
                event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message::set);
                if (reactions.isEmpty() && message.get() != null) {
                    message.get().delete().queue();
                }

                if (message.get() == null) {
                    skips.remove(skip);
                }
            }
        }
    }

    @Override
    public void onGuildVoiceLeave(final GuildVoiceLeaveEvent event) {
        super.onGuildVoiceLeave(event);
        if (event.getGuild().getAudioManager().getConnectedChannel() != null && event.getGuild()
                .getAudioManager().getConnectedChannel().getIdLong() == event.getChannelLeft().getIdLong()
                && event.getChannelLeft().getMembers().size() <= 1) {
            event.getGuild().getAudioManager().closeAudioConnection();
            MusicManager.getMusicManager(event.getGuild()).scheduler.getQueue().clear();
        }
    }
}
