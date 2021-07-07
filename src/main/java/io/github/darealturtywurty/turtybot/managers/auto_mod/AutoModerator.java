package io.github.darealturtywurty.turtybot.managers.auto_mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import io.github.darealturtywurty.turtybot.commands.moderation.MuteCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils.WarnUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AutoModerator extends ListenerAdapter {

	public static final Set<Long> USER_MUTE_MAP = new HashSet<>();

	@Override
	public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
		super.onGuildMessageReceived(event);
		if (!event.getAuthor().isBot() && !event.isWebhookMessage() && !event.getMember().isOwner()) {
			final var messages = new AtomicReference<List<Message>>();
			event.getChannel().getHistory().retrievePast(20).queue(messages::set);
			if (messages.get() != null) {
				final int spamCount = messages.get().stream()
						.filter(msg -> event.getMessage().getTimeCreated().toEpochSecond()
								- msg.getTimeCreated().toEpochSecond() < 5)
						.collect(Collectors.toList()).size();
				if (spamCount >= 5) {
					if (!USER_MUTE_MAP.contains(event.getAuthor().getIdLong())) {
						MuteCommand.muteMember(event.getGuild(), event.getGuild().getSelfMember(), event.getMember(), null,
								"Spamming (sent " + spamCount + " messages in 5 seconds!", 1800000L);
						USER_MUTE_MAP.add(event.getAuthor().getIdLong());
					} else {
						WarnUtils.getUserWarns(event.getGuild(), event.getAuthor()).addWarn(event.getGuild(),
								event.getGuild().getSelfMember(), "Spamming (sent " + spamCount + " messages in 5 seconds!");
						USER_MUTE_MAP.remove(event.getAuthor().getIdLong());
					}
				}
			}
		}
	}
}
