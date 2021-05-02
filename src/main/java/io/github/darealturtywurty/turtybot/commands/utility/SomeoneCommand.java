package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;

public class SomeoneCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		List<Member> members = ctx.getGuild().getMembers().stream()
				.filter(member -> member.getOnlineStatus() == OnlineStatus.ONLINE
						|| member.getOnlineStatus() == OnlineStatus.IDLE
						|| member.getOnlineStatus() == OnlineStatus.INVISIBLE
						|| member.getOnlineStatus() == OnlineStatus.DO_NOT_DISTURB
						|| member.getOnlineStatus() == OnlineStatus.UNKNOWN)
				.collect(Collectors.toList());
		ctx.getGuild().getMembers().forEach(member -> System.out.println(member.getEffectiveName() + " " + member.getOnlineStatus()));
		ctx.getChannel().sendMessage(members.get(ThreadLocalRandom.current().nextInt(members.size())).getAsMention())
				.queue();
	}

	@Override
	public String getName() {
		return "someone";
	}

	@Override
	public String getDescription() {
		return "Mentions a random online user (@someone).";
	}
	
	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
