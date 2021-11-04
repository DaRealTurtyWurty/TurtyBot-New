package io.github.darealturtywurty.turtybot.commands.core;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import net.dv8tion.jda.api.entities.TextChannel;
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
                .setScanners(Scanners.SubTypes, Scanners.TypesAnnotated)
                .filterInputsBy(new FilterBuilder().includePackage("io.github.darealturtywurty.turtybot")));
        reflections.getTypesAnnotatedWith(RegisterBotCmd.class).forEach(command -> {
            try {
                if (command.getAnnotation(RegisterBotCmd.class).needsManager()) {
                    addCommand(
                            (GuildCommand) command.getDeclaredConstructor(this.getClass()).newInstance(this));

                } else {
                    addCommand((GuildCommand) command.getDeclaredConstructor().newInstance());
                }
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        });
    }

    protected void handle(final SlashCommandEvent event) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(event.getGuild().getIdLong());
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

        if ((!command.isNSFW() || !((TextChannel) event.getChannel()).isNSFW()) && command.isNSFW()) {
            allowed = false;
        }

        if (!allowed) {
            event.getHook().deleteOriginal().queue();
        }

        if ((!command.isBotOwnerOnly() || !BotUtils.isBotOwner(event.getUser()))
                && command.isBotOwnerOnly()) {
            event.deferReply().setEphemeral(true).setContent("You must be the bot owner to use this command!")
                    .queue();
            return;
        }

        if ((!command.isModeratorOnly() || !BotUtils.isModerator(event.getGuild(), event.getUser()))
                && command.isModeratorOnly()) {
            event.deferReply().setEphemeral(true).setContent("You must be a moderator to use this command!")
                    .queue();
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

        final var atomicReturn = new AtomicBoolean(true);
        if (command.getCooldownMillis() > 0L) {
            info.userCooldowns.compute(event.getUser().getIdLong(), ($, commands) -> {
                final Instant cooldown = Instant.now().plusMillis(command.getCooldownMillis());
                if (commands != null) {
                    commands.compute(command, ($1, instant) -> {
                        if (instant != null && Instant.now().isBefore(instant)) {
                            event.deferReply(true).setContent(
                                    "You are currently on cooldown! You can run this command again in "
                                            + TimeUnit.MILLISECONDS.toSeconds(
                                                    instant.toEpochMilli() - System.currentTimeMillis())
                                            + " seconds!")
                                    .queue();
                            return instant;
                        }

                        atomicReturn.set(false);
                        return cooldown;
                    });
                    return commands;
                }

                final Map<GuildCommand, Instant> map = new HashMap<>();
                map.put(command, cooldown);
                atomicReturn.set(false);
                return map;
            });
        } else {
            atomicReturn.set(false);
        }

        if (atomicReturn.get())
            return;

        command.handle(new CoreCommandContext(event));
    }
}
