package io.github.darealturtywurty.turtybot.commands.core;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandContext implements ICommandContext {

	private final String[] args;
	private final GuildMessageReceivedEvent event;

	public CommandContext(final GuildMessageReceivedEvent event, final String[] args) {
		this.event = event;
		this.args = args;
	}

	@Override
	public GuildMessageReceivedEvent getEvent() {
		return this.event;
	}

	public String[] getArgs() {
		return this.args;
	}
}
