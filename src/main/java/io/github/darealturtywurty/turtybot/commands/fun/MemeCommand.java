package io.github.darealturtywurty.turtybot.commands.fun;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import io.github.darealturtywurty.turtybot.commands.core.BaseRedditCommand;
import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class MemeCommand extends BaseRedditCommand {

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
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public Set<String> getSubreddits() {
        return Sets.newHashSet("memes", "dankmemes", "blackpeopletwitter", "memeeconomy", "me_irl",
                "adviceanimals");
    }
}
