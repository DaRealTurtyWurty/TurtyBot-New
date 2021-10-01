package io.github.darealturtywurty.turtybot.commands.nsfw;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BaseNSFWCommand {
    public final String name, description;
    public final Set<String> subreddits = new HashSet<>();

    public BaseNSFWCommand(final long guildId, final String name, final String description,
            final String... subreddits) {
        this.name = name;
        this.description = description;
        Collections.addAll(this.subreddits, subreddits);
        if (!NSFWCommandListener.COMMANDS.containsKey(guildId)) {
            NSFWCommandListener.COMMANDS.put(guildId, new HashSet<>());
        }

        NSFWCommandListener.COMMANDS.get(guildId).add(this);
    }
}
