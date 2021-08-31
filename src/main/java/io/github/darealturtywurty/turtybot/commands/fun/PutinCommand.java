package io.github.darealturtywurty.turtybot.commands.fun;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class PutinCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "The most important command of them all!";
    }

    @Override
    public String getName() {
        return "putin";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var embed = new EmbedBuilder();
        embed.setTitle("The Russian Lord");
        embed.setColor(0xFF3C86);
        embed.setImage("https://media.tenor.com/images/1f70e6cb05211bc481af145bfe67bc64/tenor.gif");
        ctx.getEvent().deferReply().addEmbeds(embed.build()).queue();
    }

    @Override
    public boolean isBoosterOnly() {
        return true;
    }
}
