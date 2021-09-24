package io.github.darealturtywurty.turtybot.managers.suggestions;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import io.github.darealturtywurty.turtybot.util.data.SuggestionData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class SuggestCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Suggests something to the suggestion channel.";
    }

    @Override
    public String getName() {
        return "suggest";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "suggestion", "The suggestion that you would like to make.",
                        true),
                new OptionData(OptionType.STRING, "media_link", "An optional media link.", false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String suggestion = ctx.getEvent().getOption("suggestion").getAsString();
        final String mediaLink = ctx.getEvent().getOption("media_link") != null
                ? ctx.getEvent().getOption("media_link").getAsString()
                : null;
        final SuggestionData suggestionData = SuggestionManager.createSuggestion(ctx.getGuild(), suggestion,
                mediaLink, ctx.getAuthor().getIdLong());
        final var embed = new EmbedBuilder();
        embed.setTitle("Your suggestion has been added as number " + suggestionData.suggestionNumber + "!");
        embed.setDescription(
                "[Jump to suggestion](<https://discord.com/channels/" + ctx.getGuild().getIdLong() + "/"
                        + BotUtils.getSuggestionsChannel(ctx.getGuild()).getIdLong() + "/"
                        + suggestionData.messageId.getAsLong() + ">)");
        ctx.getEvent().deferReply(true).addEmbeds(embed.build()).queue();
    }
}
