package io.github.darealturtywurty.turtybot.commands.fun;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.RedditCommand;

public class MemeCommand extends RedditCommand {

	@Override
	public List<String> getAliases() {
		return List.of("memes");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public String getDescription() {
		return "Memes.";
	}

	@Override
	public String getName() {
		return "meme";
	}

	@Override
	public List<String> getSubreddits() {
		return List.of("memes", "dankmemes", "blackpeopletwitter", "memeeconomy", "me_irl", "adviceanimals");
	}
}
