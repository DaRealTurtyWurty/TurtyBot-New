package io.github.darealturtywurty.turtybot.commands.core;

import java.util.List;

public interface IRedditCommand extends IGuildCommand {

	List<String> getSubreddits();
}
