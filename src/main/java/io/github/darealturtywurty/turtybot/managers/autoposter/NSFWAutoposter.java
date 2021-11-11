package io.github.darealturtywurty.turtybot.managers.autoposter;

import java.util.Optional;
import java.util.Set;

import io.github.darealturtywurty.turtybot.commands.nsfw.BaseNSFWCommand;
import io.github.darealturtywurty.turtybot.commands.nsfw.NSFWCommandListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;

public class NSFWAutoposter extends Autoposter {

    private long timeout;
    public final String commandName;

    protected NSFWAutoposter(final GuildChannel channel, final long timeout, final String commandName) {
        super(channel);
        this.timeout = timeout;
        this.commandName = commandName;
    }

    @Override
    public long getTimeout() {
        return this.timeout;
    }

    @Override
    public void run() {
        final Guild guild = this.jda.getGuildById(this.guildId);
        final GuildChannel guildChannel = guild.getGuildChannelById(this.channelId);
        if (!(guildChannel instanceof TextChannel))
            return;

        final var channel = (TextChannel) guildChannel;
        if (!channel.isNSFW() || !NSFWCommandListener.COMMANDS.containsKey(this.guildId))
            return;

        final Set<BaseNSFWCommand> commands = NSFWCommandListener.COMMANDS.get(this.guildId);
        final Optional<BaseNSFWCommand> optionalCommand = commands.stream()
                .filter(cmd -> cmd.name.equalsIgnoreCase(this.commandName)).findFirst();
        boolean isHentai = false;
        if (!optionalCommand.isPresent()) {
            if (!this.commandName.equalsIgnoreCase("hentai"))
                return;
            isHentai = true;
        }

        final BaseNSFWCommand command = optionalCommand.orElse(null);
        if (isHentai) {
            NSFWCommandListener.sendHentai(channel, null);
        } else {
            NSFWCommandListener.execute(command, channel, null);
        }
    }

    public void setTimeout(long timeout) {
        if (timeout <= 0) {
            timeout = 300;
        }

        this.timeout = timeout;
    }
}
