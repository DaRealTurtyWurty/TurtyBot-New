package io.github.darealturtywurty.turtybot.commands.core;

import java.util.Set;

public interface RedditCommand extends GuildCommand {

    Set<String> getSubreddits();
}
