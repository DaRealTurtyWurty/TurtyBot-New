package io.github.darealturtywurty.turtybot.managers.help_system;

import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CloseButtonListener extends ListenerAdapter {

    public static boolean canClose(final TextChannel channel, final Member member) {
        final boolean isModerator = BotUtils.isModerator(channel.getGuild(), member);
        final boolean isOwner = BotUtils.isBotOwner(member.getUser());
        final boolean isServerOwner = channel.getGuild().getOwnerIdLong() == member.getIdLong();
        final Role advModderRole = BotUtils.getAdvModderRole(channel.getGuild());
        final boolean isAdvancedModder = member.getRoles().contains(advModderRole);
        final String channelOwnerId = channel.getTopic().split("\n")[0];
        final boolean isChannelOwner = member.getId().equalsIgnoreCase(channelOwnerId);
        final boolean canManageChannel = member.hasPermission(Permission.MANAGE_CHANNEL)
                || member.hasPermission(channel, Permission.MANAGE_CHANNEL);
        return isModerator || isOwner || isServerOwner || isAdvancedModder || isChannelOwner
                || canManageChannel;
    }

    @Override
    public void onButtonClick(final ButtonClickEvent event) {
        super.onButtonClick(event);
        final TextChannel channel = event.getTextChannel();
        if (event.getComponentId().startsWith("confirm")) {
            if (canClose(channel, event.getMember())) {
                event.getJDA().addEventListener(new HelpManager.HelpEventListener(true));
                HelpManager.handleClosing(channel, null, Long.parseLong(channel.getTopic().split("\n")[0]));
                event.getMessage().delete().queue();
            }
        } else if (event.getComponentId().startsWith("cancel")) {
            event.getChannel().deleteMessageById(event.getMessageIdLong()).queue();
        }
    }
}
