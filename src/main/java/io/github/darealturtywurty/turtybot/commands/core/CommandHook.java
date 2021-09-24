package io.github.darealturtywurty.turtybot.commands.core;

import java.util.Timer;
import java.util.TimerTask;

import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class CommandHook extends ListenerAdapter {

    protected final CommandManager manager = new CommandManager();
    private final Timer readTimer = new Timer();

    @Override
    public void onGuildReady(final GuildReadyEvent event) {
        super.onGuildReady(event);
        final Guild guild = event.getGuild();
        guild.loadMembers();

        final CommandListUpdateAction updates = guild.updateCommands();
        updates.addCommands(this.manager.commands.stream().filter(cmd -> {
            if (cmd.productionReady() || !BotUtils.notTestServer(guild))
                return true;
            return false;
        }).map(cmd -> new CommandData(cmd.getName(), cmd.getDescription()).addOptions(cmd.getOptions()))
                .toList());
        updates.queue();

        this.readTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                CoreBotUtils.readGuildInfo(event.getJDA());
            }
        }, 5000);
    }

    @Override
    public void onSlashCommand(final SlashCommandEvent event) {
        if (event.getUser().isBot() || event.getUser().isSystem())
            return;

        this.manager.handle(event);
    }
}
