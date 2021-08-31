package io.github.darealturtywurty.turtybot.commands.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class CommandManager {

    public final Set<GuildCommand> commands = new HashSet<>();

    public CommandManager() {
        registerCommands();
    }

    public void addCommand(final GuildCommand cmd) {
        this.commands.add(cmd);
    }

    public void addCommands(final GuildCommand... cmds) {
        Collections.addAll(this.commands, cmds);
    }

    public GuildCommand getCommand(final String name) {
        final Optional<GuildCommand> optional = this.commands.stream()
                .filter(cmd -> cmd.getName().equalsIgnoreCase(name)).findFirst();
        return optional.isPresent() ? optional.get() : null;
    }

    public void registerCommands() {
        final var reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("io.github.darealturtywurty.turtybot"))
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner())
                .filterInputsBy(new FilterBuilder().includePackage("io.github.darealturtywurty.turtybot")));
        reflections.getTypesAnnotatedWith(RegisterBotCmd.class).forEach(command -> {
            try {
                addCommand((GuildCommand) command.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                try {
                    addCommand(
                            (GuildCommand) command.getDeclaredConstructor(this.getClass()).newInstance(this));
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    protected void handle(final SlashCommandEvent event) {
        final GuildCommand command = getCommand(event.getName());
        if (command == null)
            return;

        var allowed = true;
        if (command.blacklistChannels().contains(event.getChannel().getName().toLowerCase().trim())) {
            allowed = false;
        }

        if (command.whitelistChannels().contains(event.getChannel().getName().toLowerCase().trim())) {
            allowed = true;
        }

        if ((!command.isNSFW() || !((GuildCommand) event.getChannel()).isNSFW()) && command.isNSFW()) {
            allowed = false;
        }

        if (!allowed) {
            event.getHook().deleteOriginal().queue();
            return;
        }

        if ((!command.isBotOwnerOnly() || !BotUtils.isBotOwner(event.getUser()))
                && command.isBotOwnerOnly()) {
            event.deferReply().setEphemeral(true).setContent("You must be the bot owner to use this command!")
                    .queue(hook -> {
                        hook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS);
                        event.getHook().deleteOriginal().queueAfter(15, TimeUnit.SECONDS);
                    });
            return;
        }

        if ((!command.isModeratorOnly() || !BotUtils.isModerator(event.getGuild(), event.getUser()))
                && command.isModeratorOnly()) {
            event.deferReply().setEphemeral(true).setContent("You must be a moderator to use this command!")
                    .queue(hook -> {
                        hook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS);
                        event.getHook().deleteOriginal().queueAfter(15, TimeUnit.SECONDS);
                    });
            return;
        }

        /*
         * if ((!command.isBoosterOnly() || event.getMember().getTimeBoosted() != null)
         * && event.getUser().getIdLong() != BotUtils.getOwnerID() ||
         * command.isBoosterOnly()) { event.deferReply().setEphemeral(true)
         * .setContent("You must be a server booster to use this command!").queue(hook
         * -> { hook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS);
         * event.getHook().deleteOriginal().queueAfter(15, TimeUnit.SECONDS); });
         * return; }
         */

        command.handle(new CoreCommandContext(event));
    }
}
