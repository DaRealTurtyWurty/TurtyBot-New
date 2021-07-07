package io.github.darealturtywurty.turtybot.data;

import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.entities.Guild;

public class GuildInfo {

	public final Guild guild;
	public final Map<Long, UserWarns> userWarnMap = new HashMap<>();
	public final Map<Long, ShowcaseInfo> showcaseInfos = new HashMap<>();
	public final Map<Long, StarStats> userStarStats = new HashMap<>();

	public String prefix = "!";

	// Roles
	public long modRoleID, advModderRoleID, mutedRoleID;

	// Channels
	public long modLogID, showcasesID, starboardID;

	// Warnings
	public int muteThreshold = 2, kickThreshold = 3, banThreshold = 5;

	// Starboard
	public int minimumStars = 5;
	public boolean includeBotStar = false;
	public boolean enableStarboard = true;
	public int[] stages = { 10, 15, 20, 30, 40, 50 };

	public GuildInfo(Guild guild) {
		this.guild = guild;
	}
}
