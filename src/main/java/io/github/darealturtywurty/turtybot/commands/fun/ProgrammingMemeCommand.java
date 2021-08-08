package io.github.darealturtywurty.turtybot.commands.fun;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.RedditCommand;

public class ProgrammingMemeCommand extends RedditCommand {

	@Override
	public List<String> getAliases() {
		return List.of("programmingmemes", "codermeme", "codermemes", "devmeme", "developermeme", "devmemes",
				"developermemes");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public String getDescription() {
		return "Memes only programmers will understand.";
	}

	@Override
	public String getName() {
		return "programmingmeme";
	}

	@Override
	public List<String> getSubreddits() {
		return List.of("ProgrammerHumor", "programmingmemes");
	}
}
