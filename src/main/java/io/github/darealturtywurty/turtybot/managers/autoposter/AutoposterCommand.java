package io.github.darealturtywurty.turtybot.managers.autoposter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.darealturtywurty.turtybot.commands.core.CommandManager;
import io.github.darealturtywurty.turtybot.commands.nsfw.NSFWCommandListener;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AutoposterCommand extends ListenerAdapter {

    private final CommandManager commandManager;

    public AutoposterCommand(final CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
        if (event.isWebhookMessage() || event.getAuthor().isBot()
                || event.getAuthor().getIdLong() != event.getGuild().getOwnerIdLong())
            return;

        final String content = event.getMessage().getContentRaw();
        if (!content.startsWith("!") && !content.startsWith(event.getJDA().getSelfUser().getAsMention()))
            return;

        final String[] args = content.split(" ");
        if (args.length < 3)
            return;

        if (!args[0].replace('!', ' ').replace(event.getJDA().getSelfUser().getAsMention(), "").trim()
                .equalsIgnoreCase("autopost"))
            return;

        final String subcommand = args[1].toLowerCase().trim();
        final String command = args[2].toLowerCase().trim();
        TextChannel channel = event.getChannel();
        if (subcommand.equals("add")) {
            long cooldown = 300;
            try {
                if (args.length > 3) {
                    final long input = Long.parseLong(args[3]);
                    if (input > 0) {
                        cooldown = input;
                    }
                }
            } catch (final NumberFormatException exception) {
                // Hi! ;)
            }

            try {
                if (args.length > 4) {
                    final String channelId = args[4].replace("<#", "").replace(">", "").trim().toLowerCase();
                    if (!channelId.isBlank()) {
                        channel = event.getGuild().getTextChannelById(channelId);
                    }
                }
            } catch (final NumberFormatException exception) {
                // Hi! ;)
            }

            if (this.commandManager.commands.stream().noneMatch(cmd -> cmd.getName().equals(command))
                    && (!NSFWCommandListener.COMMANDS.containsKey(event.getGuild().getIdLong())
                            || NSFWCommandListener.COMMANDS.get(event.getGuild().getIdLong()).stream()
                                    .noneMatch(cmd -> cmd.name.equals(command)))
                    && !command.equalsIgnoreCase("hentai"))
                return;

            if (NSFWCommandListener.COMMANDS.containsKey(event.getGuild().getIdLong())
                    && NSFWCommandListener.COMMANDS.get(event.getGuild().getIdLong()).stream()
                            .anyMatch(cmd -> cmd.name.equals(command))
                    || command.equalsIgnoreCase("hentai")) {
                Autoposter.Manager.AUTOPOSTERS.add(new NSFWAutoposter(channel, cooldown, command));
                Autoposter.Manager.init();
                event.getMessage().delete().queue();
            }
        } else if (subcommand.equals("remove")) {
            if (NSFWCommandListener.COMMANDS.containsKey(event.getGuild().getIdLong())
                    && NSFWCommandListener.COMMANDS.get(event.getGuild().getIdLong()).stream()
                            .anyMatch(cmd -> cmd.name.equals(command))
                    || command.equalsIgnoreCase("hentai")) {
                final List<NSFWAutoposter> autoposters = Autoposter.Manager.AUTOPOSTERS.stream()
                        .filter(autoposter -> autoposter.guildId == event.getGuild().getIdLong()
                                && autoposter.channelId == event.getChannel().getIdLong())
                        .map(NSFWAutoposter.class::cast)
                        .filter(autoposter -> autoposter.commandName.equals(command)).toList();
                if (!autoposters.isEmpty()) {
                    Autoposter.Manager.AUTOPOSTERS.removeAll(autoposters);
                    Autoposter.Manager.init();
                    event.getMessage().delete().queue();
                } else {
                    event.getMessage().reply("There are no autoposters that go by this name!")
                            .mentionRepliedUser(false).queue(msg -> {
                                msg.delete().queueAfter(15, TimeUnit.SECONDS);
                                event.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
                            });
                }
            }
        } else if (subcommand.equals("edit")) {
            if (args.length <= 2)
                return;

            try {
                if (NSFWCommandListener.COMMANDS.containsKey(event.getGuild().getIdLong())
                        && NSFWCommandListener.COMMANDS.get(event.getGuild().getIdLong()).stream()
                                .anyMatch(cmd -> cmd.name.equals(command))
                        || command.equalsIgnoreCase("hentai")) {
                    final List<NSFWAutoposter> autoposters = Autoposter.Manager.AUTOPOSTERS.stream()
                            .filter(autoposter -> autoposter.guildId == event.getGuild().getIdLong()
                                    && autoposter.channelId == event.getChannel().getIdLong())
                            .map(NSFWAutoposter.class::cast)
                            .filter(autoposter -> autoposter.commandName.equals(command)).toList();
                    if (!autoposters.isEmpty()) {
                        autoposters.forEach(autoposter -> autoposter.setTimeout(Long.parseLong(args[2])));
                        event.getMessage().delete().queue();
                    } else {
                        event.getMessage().reply("There are no autoposters that go by this name!")
                                .mentionRepliedUser(false).queue(msg -> {
                                    msg.delete().queueAfter(15, TimeUnit.SECONDS);
                                    event.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
                                });
                    }
                }
            } catch (final NumberFormatException exception) {
                event.getMessage().reply("You must supply a valid cooldown!").mentionRepliedUser(false)
                        .queue(msg -> {
                            msg.delete().queueAfter(15, TimeUnit.SECONDS);
                            event.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
                        });
            }
        }
    }
}
