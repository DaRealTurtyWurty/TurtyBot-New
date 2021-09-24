package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class PingCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Shows the ping between the discord bot and the discord servers.";
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        ctx.getJDA().getRestPing()
                .queue(ping -> ctx.getEvent().replyFormat(String
                        .format("Rest Ping: %sms%nWebsocket Ping: %sms", ping, ctx.getJDA().getGatewayPing()))
                        .mentionRepliedUser(false).queue());
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
