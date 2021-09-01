package io.github.darealturtywurty.turtybot.commands.core;

import io.github.darealturtywurty.turtybot.managers.auto_mod.BotResponseListener;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class CommandHook extends ListenerAdapter {

    protected final CommandManager manager = new CommandManager();

    @Override
    public void onGuildReady(final GuildReadyEvent event) {
        super.onGuildReady(event);
        BotResponseListener.initialize(event.getGuild().getIdLong());
        final CommandListUpdateAction updates = event.getGuild().updateCommands();
        updates.addCommands(this.manager.commands.stream()
                .map(cmd -> new CommandData(cmd.getName(), cmd.getDescription()).addOptions(cmd.getOptions()))
                .toList());
        updates.queue();
    }

    @Override
    public void onSlashCommand(final SlashCommandEvent event) {
        if (event.getUser().isBot() || event.getUser().isSystem())
            return;

        this.manager.handle(event);
    }
}
