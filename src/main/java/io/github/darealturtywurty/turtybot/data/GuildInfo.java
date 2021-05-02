package io.github.darealturtywurty.turtybot.data;

import net.dv8tion.jda.api.entities.Guild;

public class GuildInfo {

	public final Guild guild;
	public String prefix = "!";
	public long modRoleID, advModderRoleID, modLogID;

	public GuildInfo(Guild guild) {
		this.guild = guild;
	}
}
