package io.github.darealturtywurty.turtybot.commands.fun;

import java.time.Instant;
import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class CatSaysCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Random cat image with the supplied text";
    }

    @Override
    public String getName() {
        return "catsays";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "to_say", "What the cat should say?", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String text = ctx.getEvent().getOption("to_say").getAsString();
        final var embed = new EmbedBuilder();
        embed.setColor(BotUtils.generateRandomPastelColor());
        embed.setTimestamp(Instant.now());
        embed.setTitle("Cat says: `" + text + "`");
        final String randQuery = "?" + Constants.RANDOM.nextInt() + "=" + Constants.RANDOM.nextInt();
        embed.setImage("https://cataas.com/cat/says/" + text + randQuery);
        ctx.getEvent().deferReply().addEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }
}
