package io.github.darealturtywurty.turtybot.util.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;

import com.mongodb.client.MongoCollection;

import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.managers.music.core.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;

public class GuildInfo {

    public final Guild guild;
    public final Map<Long, UserWarns> userWarns = new HashMap<>();
    public final Map<Long, ShowcaseInfo> showcaseInfos = new HashMap<>();
    public final Map<Long, StarStats> userStarStats = new HashMap<>();
    public final Map<Long, Pair<Integer, Integer>> moddingHelpers = new HashMap<>();
    public final Map<Long, List<Long>> messagesByChannel = new HashMap<>();
    public final Map<Long, Set<Long>> mutedUserRoles = new HashMap<>();
    public final Map<Long, UserEmbedData> userEmbeds = new HashMap<>();
    public final Map<Long, Map<GuildCommand, Instant>> userCooldowns = new HashMap<>();

    public final Set<Long> userMutes = new HashSet<>();
    public final Set<Map<Long, Long>> helpChannels = new HashSet<>();
    public final Set<Skip> skips = new HashSet<>();
    public final List<SuggestionData> suggestionData = new ArrayList<>();

    public GuildMusicManager musicManager;

    // Saved to MongoDB
    public final Map<Long, Integer> xpCooldown = new HashMap<>();
    public final Map<Long, RankCard> userRankCards = new HashMap<>();
    public final List<Document> leaderboard = new LinkedList<>();
    public MongoCollection<Document> levels;

    public String prefix = "!";

    // Roles
    public long modRoleID, advModderRoleID, mutedRoleID;

    // Channels
    public long modLogID, showcasesID, starboardID, suggestionsID, informationID;

    // Warnings
    public int muteThreshold = 2, kickThreshold = 3, banThreshold = 5;

    // Starboard
    public int minimumStars = 5;
    public boolean includeBotStar = false;
    public boolean enableStarboard = true;
    public int[] stages = { 10, 15, 20, 30, 40, 50 };

    public GuildInfo(final Guild guild) {
        this.guild = guild;
    }
}
