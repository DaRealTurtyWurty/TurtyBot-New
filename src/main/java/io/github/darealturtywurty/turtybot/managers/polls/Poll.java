package io.github.darealturtywurty.turtybot.managers.polls;

import java.util.HashMap;
import java.util.Map;

public class Poll {
	protected final long guildId, channelId, messageId;
	protected final Map<String, Long> reactions = new HashMap<>();

	public Poll(final long guildId, final long channelId, final long messageId) {
		this.guildId = guildId;
		this.channelId = channelId;
		this.messageId = messageId;
	}
}
