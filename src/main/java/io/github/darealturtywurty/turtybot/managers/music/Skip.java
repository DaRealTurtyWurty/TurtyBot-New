package io.github.darealturtywurty.turtybot.managers.music;

import java.util.HashSet;
import java.util.Set;

public class Skip {

	protected final long guildId, channelId, messageId;
	protected final Set<Long> reactions = new HashSet<>();

	public Skip(final long guildId, final long channelId, final long messageId) {
		this.guildId = guildId;
		this.channelId = channelId;
		this.messageId = messageId;
	}
}
