package io.github.darealturtywurty.turtybot.commands.core;

import java.util.Timer;
import java.util.TimerTask;

import io.github.darealturtywurty.turtybot.commands.nsfw.NSFWInitializer;
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
        final Guild guild = event.getGuild();
        this.readTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(guild.getName());
                guild.loadMembers().onSuccess(members -> System.out
                        .println(members.size() + " Members loaded for guild: " + guild.getName()));
                NSFWInitializer.init(guild.getIdLong());
                CoreBotUtils.readGuildInfo(guild);
            }
        }, 5000);

        final CommandListUpdateAction updates = guild.updateCommands();
        updates.addCommands(this.manager.commands.stream()
                .filter(cmd -> cmd.productionReady() || !BotUtils.notTestServer(guild)).map(cmd -> {
                    final CommandData data = new CommandData(cmd.getName(), cmd.getDescription());
                    if (!cmd.getSubcommandGroupData().isEmpty()) {
                        data.addSubcommandGroups(cmd.getSubcommandGroupData());
                    } else if (!cmd.getSubcommandData().isEmpty()) {
                        data.addSubcommands(cmd.getSubcommandData());
                    } else if (!cmd.getOptions().isEmpty()) {
                        data.addOptions(cmd.getOptions());
                    }

                    return data;
                }).toList());
        updates.queue();
    }

    @Override
    public void onSlashCommand(final SlashCommandEvent event) {
        if (event.getUser().isBot() || event.getUser().isSystem())
            return;

        this.manager.handle(event);
    }
}
