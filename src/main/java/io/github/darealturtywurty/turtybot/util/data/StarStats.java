package io.github.darealturtywurty.turtybot.util.data;

import io.github.darealturtywurty.turtybot.managers.starboard.StarboardManager;
import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import net.dv8tion.jda.api.entities.Guild;

public class StarStats {

    public final long userID;

    private int totalStars;
    public int otherStars;

    public StarStats(final long userID) {
        this.userID = userID;
    }

    private static GuildInfo getOrCreateGuildInfo(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
    }

    /**
     * Gets the stars that this user has! Note: This may not be up to date. Use
     * {@link StarStats#retrieveTotalStars(Guild)} to retrieve the latest stats.
     *
     * @return the total stars this user has recieved
     */
    public int getTotalStars() {
        return this.totalStars;
    }

    public void retrieveTotalStars(final Guild guild) {
        final var guildInfo = getOrCreateGuildInfo(guild);

        this.totalStars = 0;
        guildInfo.showcaseInfos.forEach((originalMessageID, showcaseInfo) -> {
            final long author = StarboardManager.getShowcaseUserID(guild, showcaseInfo);
            if (author <= 0L)
                return;
            this.totalStars += showcaseInfo.getStars();
        });

        this.totalStars += this.otherStars;
    }
}
