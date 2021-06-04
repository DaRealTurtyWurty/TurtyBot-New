package io.github.darealturtywurty.turtybot.help_system;

import static io.github.darealturtywurty.turtybot.help_system.HelpManager.deleteProblemMessage;
import static io.github.darealturtywurty.turtybot.help_system.HelpManager.removeChannel;

import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HelpReactionEventListener extends ListenerAdapter {

	private final TextChannel helpChannel;
	private final long messageID;

	public HelpReactionEventListener(TextChannel channel, long messageID) {
		this.helpChannel = channel;
		this.messageID = messageID;
	}

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		if (event.getUser().isBot())
			return;

		switch (event.getReactionEmote().getEmoji()) {
		case "🦺":
			if (event.getMessageIdLong() != this.messageID)
				return;
			event.retrieveUser().queue(user -> event.retrieveMember().queue(member -> {
				if (this.helpChannel == null)
					return;
				this.helpChannel.upsertPermissionOverride(member).setAllow(Permission.VIEW_CHANNEL).queue();
			}));
			break;
		case "✔":
			if (event.getMessageIdLong() != this.messageID || !this.helpChannel.equals(event.getChannel())
					&& (!BotUtils.isModerator(event.getGuild(), event.getUser())
							|| !this.helpChannel.getTopic().split("\n")[0].equalsIgnoreCase(event.getUser().getId())))
				return;
			event.retrieveUser().queue(user -> event.retrieveMember().queue(member -> {
				deleteProblemMessage(event.getGuild(), this.helpChannel.getTopic().split("\n")[0]);
				this.helpChannel.delete().queue();
			}));
			event.getJDA().removeEventListener(this);
			break;
		case "❌":
			if (event.getMessageIdLong() != this.messageID || !this.helpChannel.equals(event.getChannel())
					&& shouldIgnoreMember(event.getChannel(), event.getMember()))
				return;
			event.getChannel().deleteMessageById(messageID).queue();
			event.getJDA().removeEventListener(this);
			removeChannel(this.helpChannel);
			break;
		default:
			break;
		}
	}

	protected static boolean shouldIgnoreMember(TextChannel channel, Member member) {
		var advancedModder = BotUtils.getAdvModderRole(channel.getGuild());
		return BotUtils.isModerator(channel.getGuild(), member)
				|| channel.getTopic().split("\n")[0].toLowerCase().trim().equalsIgnoreCase(member.getId())
				|| member.getRoles().contains(advancedModder) || member.getUser().isBot() || member.isPending();
	}

	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
		event.retrieveUser().queue(user -> {
			if (event.getMessageIdLong() != this.messageID || !event.getReactionEmote().getEmoji().equalsIgnoreCase("🦺")
					|| this.helpChannel == null)
				return;
			event.retrieveMember().queue(member -> {
				if (!shouldIgnoreMember(event.getChannel(), event.getMember()))
					this.helpChannel.upsertPermissionOverride(member).setDeny(Permission.VIEW_CHANNEL).queue();
			});
		});
	}
}
