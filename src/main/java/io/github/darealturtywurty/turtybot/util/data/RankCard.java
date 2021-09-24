package io.github.darealturtywurty.turtybot.util.data;

import java.awt.Color;

import io.github.darealturtywurty.turtybot.managers.levelling_system.Inventory;
import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import net.dv8tion.jda.api.entities.Member;

public class RankCard {

    public static final RankCard DEFAULT = new RankCard(null);

    public Member member;
    public Color backgroundColour = Color.BLACK, outlineColour = Color.WHITE, rankTextColour = Color.RED,
            levelTextColour = Color.GREEN, xpOutlineColour = Color.BLUE, xpEmptyColour = Color.YELLOW,
            xpFillColour = Color.CYAN, avatarOutlineColour = Color.MAGENTA, percentTextColour = Color.ORANGE,
            xpTextColour = Color.PINK, nameTextColour = Color.DARK_GRAY;

    public float outlineOpacity = 0.5f;

    // Premium
    public String backgroundImage = "", outlineImage = "", xpOutlineImage = "", xpEmptyImage = "",
            xpFillImage = "", avatarOutlineImage = "";

    public final Inventory inventory = new Inventory(this.member);

    public RankCard(final Member member) {
        this.member = member;
        if (member != null) {
            CoreBotUtils.GUILDS.get(member.getGuild().getIdLong()).userRankCards.put(member.getIdLong(),
                    this);
        }
    }
}
