package io.github.darealturtywurty.turtybot.commands.core;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class CoreCommandContext implements CommandContext {

	private final SlashCommandEvent event;

	public CoreCommandContext(final SlashCommandEvent event) {
		this.event = event;
	}

	@Override
	public SlashCommandEvent getEvent() {
		return this.event;
	}
}
