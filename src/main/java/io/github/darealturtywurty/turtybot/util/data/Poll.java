package io.github.darealturtywurty.turtybot.util.data;

import java.util.HashMap;
import java.util.Map;

public class Poll {
    public final long guildId, channelId, messageId;
    public final Map<String, Long> reactions = new HashMap<>();

    public Poll(final long guildId, final long channelId, final long messageId) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.messageId = messageId;
    }
}
