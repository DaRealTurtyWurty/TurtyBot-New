package io.github.darealturtywurty.turtybot.managers.levelling_system;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.entities.Member;

public class RankCard {

	protected static final RankCard DEFAULT = new RankCard(null);

	protected Member member;
	protected Color backgroundColour = Color.BLACK, outlineColour = Color.WHITE, rankTextColour = Color.RED,
			levelTextColour = Color.GREEN, xpOutlineColour = Color.BLUE, xpEmptyColour = Color.YELLOW,
			xpFillColour = Color.CYAN, avatarOutlineColour = Color.MAGENTA, percentTextColour = Color.ORANGE,
			xpTextColour = Color.PINK, nameTextColour = Color.DARK_GRAY;

	protected float outlineOpacity = 0.5f;

	// Premium
	protected String backgroundImage = "", outlineImage = "", xpOutlineImage = "", xpEmptyImage = "", xpFillImage = "",
			avatarOutlineImage = "";

	protected final Inventory inventory = new Inventory(this.member);

	public RankCard(final Member member) {
		this.member = member;
		if (member != null) {
			Map<Member, RankCard> cards = null;
			if (!LevellingManager.RANK_CARD_DATA.containsKey(member.getGuild())) {
				cards = new HashMap<>();
				LevellingManager.RANK_CARD_DATA.put(member.getGuild(), cards);
			} else {
				cards = LevellingManager.RANK_CARD_DATA.get(member.getGuild());
			}

			cards.put(member, this);
		}
	}
}
