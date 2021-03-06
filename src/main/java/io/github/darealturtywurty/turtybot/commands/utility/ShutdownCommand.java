package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class ShutdownCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Shuts the bot down.";
    }

    @Override
    public String getName() {
        return "shutdown";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        ctx.getJDA().getGuilds().forEach(guild -> {
            final List<TextChannel> channels = guild.getTextChannels().stream()
                    .filter(channel -> channel.getName().contains("general")).toList();
            if (!channels.isEmpty() && guild.getIdLong() != 819294753732296776L) {
                channels.get(0)
                        .sendMessage(
                                "I am now going offline for maintenance. Apologies for any inconveniences!")
                        .queue();
            }
        });
        ctx.getEvent().deferReply().setContent("I am now shutting down!").queue();
        BotUtils.shutdownApplication(ctx.getJDA());
    }

    @Override
    public boolean isBotOwnerOnly() {
        return true;
    }

    @Override
    public boolean isDevelopmentOnly() {
        return true;
    }

    @Override
    public boolean productionReady() {
        return false;
    }
}
