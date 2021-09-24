package io.github.darealturtywurty.turtybot.managers.help_system;

import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HelpReactionEventListener extends ListenerAdapter {

    private final TextChannel helpChannel;

    private final long messageID;

    public HelpReactionEventListener(final TextChannel channel, final long messageID) {
        this.helpChannel = channel;
        this.messageID = messageID;
    }

    protected static boolean shouldIgnoreMember(final TextChannel channel, final Member member) {
        final var advancedModder = BotUtils.getAdvModderRole(channel.getGuild());
        if (channel.getTopic() == null)
            return false;

        return BotUtils.isModerator(channel.getGuild(), member)
                || channel.getTopic().split("\n")[0].toLowerCase().trim().equalsIgnoreCase(member.getId())
                || member.getRoles().contains(advancedModder) || member.getUser().isBot()
                || member.isPending();
    }

    @Override
    public void onGuildMessageReactionAdd(final GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot())
            return;

        if (event.getReactionEmote().getEmoji().equals("🦺")) {
            if (event.getMessageIdLong() != this.messageID)
                return;
            event.retrieveUser().queue(user -> event.retrieveMember().queue(member -> {
                if (this.helpChannel == null)
                    return;
                this.helpChannel.upsertPermissionOverride(member).setAllow(Permission.VIEW_CHANNEL).queue();
            }));
        }
    }

    @Override
    public void onGuildMessageReactionRemove(final GuildMessageReactionRemoveEvent event) {
        event.retrieveUser().queue(user -> {
            if (event.getMessageIdLong() != this.messageID
                    || !event.getReactionEmote().getEmoji().equalsIgnoreCase("🦺")
                    || this.helpChannel == null)
                return;
            event.retrieveMember().queue(member -> {
                if (!shouldIgnoreMember(event.getChannel(), event.getMember())) {
                    this.helpChannel.upsertPermissionOverride(member).setDeny(Permission.VIEW_CHANNEL)
                            .queue();
                }
            });
        });
    }
}
