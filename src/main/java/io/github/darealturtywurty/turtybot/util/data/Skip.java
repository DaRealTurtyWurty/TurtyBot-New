package io.github.darealturtywurty.turtybot.util.data;

import java.util.HashSet;
import java.util.Set;

public class Skip {

    public final long guildId, channelId, messageId;
    public final Set<Long> reactions = new HashSet<>();

    public Skip(final long guildId, final long channelId, final long messageId) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.messageId = messageId;
    }
}
