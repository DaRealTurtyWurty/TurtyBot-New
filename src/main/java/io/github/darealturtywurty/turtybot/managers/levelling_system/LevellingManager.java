package io.github.darealturtywurty.turtybot.managers.levelling_system;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bson.Document;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.vavr.collection.Stream;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LevellingManager extends ListenerAdapter {

    protected static final Map<Guild, Map<Member, RankCard>> RANK_CARD_DATA = new HashMap<>();

    private final Map<Guild, MongoCollection<Document>> levels = new HashMap<>();

    private final Map<Guild, LinkedList<Document>> leaderboard = new HashMap<>();

    private final Map<Guild, Map<Member, Integer>> memberCooldown = new HashMap<>();

    private final MongoDatabase database;

    public LevellingManager() {
        final var mongoClient = MongoClients.create(BotUtils.getMongoConnection());
        this.database = mongoClient.getDatabase("TurtyBot");
        ItemRegistry.register("rickroll");
        ItemRegistry.register("beans_avatar");
    }

    public static int getLevelForXP(final int xp) {
        return (int) ((-25 + Math.sqrt(5 * (120 + xp))) / 5);
    }

    public static int getXPForLevel(final int level) {
        return (int) (5 * Math.pow(level, 2) + 50 * level + 5);
    }

    private static float lerp(final float min, final float max, final float numb) {
        return min * (1.0f - numb) + max * numb;
    }

    public Document findCard(final Member member) {
        var collection = this.levels.get(member.getGuild());
        if (collection == null) {
            this.database.createCollection(member.getGuild().getName());
            collection = this.database.getCollection(member.getGuild().getName());
            this.levels.put(member.getGuild(), collection);
        }

        final FindIterable<Document> documents = collection.find(Filters.eq("ID", member.getIdLong()));
        final var document = documents.first();
        if (document == null)
            return document;

        return (Document) document.get("RankCard");
    }

    public RankCard getCardFromDB(final Member member) {
        final var document = findCard(member);
        final var card = new RankCard(member);
        if (document == null)
            return card;

        card.backgroundColour = new Color(document.getInteger("BackgroundColour"));
        card.outlineColour = new Color(document.getInteger("OutlineColour"));
        card.rankTextColour = new Color(document.getInteger("RankTextColour"));
        card.levelTextColour = new Color(document.getInteger("LevelTextColour"));
        card.xpOutlineColour = new Color(document.getInteger("XPBarOutlineColour"));
        card.xpEmptyColour = new Color(document.getInteger("XPBarEmptyColour"));
        card.xpFillColour = new Color(document.getInteger("XPBarFillColour"));
        card.avatarOutlineColour = new Color(document.getInteger("AvatarOutlineColour"));
        card.percentTextColour = new Color(document.getInteger("PercentTextColour"));
        card.xpTextColour = new Color(document.getInteger("XPTextColour"));
        card.outlineOpacity = document.getDouble("OutlineOpacity").floatValue();

        card.backgroundImage = document.getString("BackgroundImage");
        card.outlineImage = document.getString("OutlineImage");
        card.xpOutlineImage = document.getString("XPBarOutlineImage");
        card.xpEmptyImage = document.getString("XPBarEmptyImage");
        card.xpFillImage = document.getString("XPBarFillImage");
        card.avatarOutlineImage = document.getString("AvatarOutlineImage");

        return updateCard(member, card);
    }

    public List<Document> getLeaderboard(final Guild guild) {
        sortLeaderboard(guild);
        return this.leaderboard.get(guild);
    }

    public int getRank(final Member member) {
        sortLeaderboard(member.getGuild());
        return Stream.of(this.leaderboard.get(member.getGuild()).toArray(new Document[0]))
                .indexWhere(doc -> doc.getLong("ID") == member.getIdLong());
    }

    public int getStoredMemberCount() {
        return this.leaderboard.size();
    }

    public int getUserCooldown(final Member member) {
        Map<Member, Integer> cooldowns = null;
        if (!this.memberCooldown.containsKey(member.getGuild())) {
            cooldowns = new HashMap<>();
            this.memberCooldown.put(member.getGuild(), cooldowns);
        }

        if (cooldowns == null) {
            cooldowns = this.memberCooldown.get(member.getGuild());
        }

        if (cooldowns.containsKey(member))
            return cooldowns.get(member);
        return 0;
    }

    public int getUserXP(final Member member) {
        MongoCollection<Document> collection = this.levels.get(member.getGuild());
        if (collection == null) {
            try {
                collection = this.database.getCollection(member.getGuild().getName());
            } catch (final IllegalArgumentException e) {
                this.database.createCollection(member.getGuild().getName());
                collection = this.database.getCollection(member.getGuild().getName());
            }

            this.levels.put(member.getGuild(), collection);
            if (!this.leaderboard.containsKey(member.getGuild())) {
                this.leaderboard.put(member.getGuild(), new LinkedList<>());
            }
        }

        final LinkedList<Document> lb = this.leaderboard.get(member.getGuild());

        Document user = collection.find(new Document("ID", member.getIdLong())).first();
        if (user == null) {
            user = new Document("ID", member.getIdLong());
            user.append("XP", 0);
            collection.insertOne(user);
            if (!lb.contains(user)) {
                lb.add(user);
            }
        }
        return user.getInteger("XP", 0);
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
        if (event.isWebhookMessage() || event.getMember().getUser().isBot())
            return;

        randXp(event.getMessage(), event.getMember());
    }

    public void randXp(final Message message, final Member member) {
        float messageMultiplier = message.getContentRaw().length() > 64 ? 1.5f : 1f;
        if (message.getContentRaw().length() <= 0) {
            messageMultiplier = 0f;
        }

        var boostMultiplier = 1f;

        if (member.getTimeBoosted() != null) {
            final var decimalFormat = new DecimalFormat("#.####");
            final var months = Float.parseFloat(decimalFormat
                    .format((member.getTimeBoosted().toInstant().toEpochMilli() - System.currentTimeMillis())
                            / Constants.MILLI_TO_MONTH));
            boostMultiplier += months / 12f;
        }

        if (getUserCooldown(member) <= 0) {
            final int level = getLevelForXP(getUserXP(member));
            setUserXP(member, getUserXP(member)
                    + Math.round((Constants.RANDOM.nextInt(25) + 5) * boostMultiplier * messageMultiplier));
            setUserCooldown(member, 15);
            final int newLevel = getLevelForXP(getUserXP(member));
            if (newLevel > level) {
                message.getChannel().sendMessage("Congrats " + member.getAsMention()
                        + " you levelled up to level: " + newLevel + "! 🎉").queue();
                if (newLevel % 5 == 0) {
                    final var card = getOrCreateCard(member);
                    card.inventory.addItem(ItemRegistry.scaledRandom(), (TextChannel) message.getChannel());
                }
            }
        }

    }

    public void setUserCooldown(final Member member, final int cooldown) {
        if (!this.memberCooldown.containsKey(member.getGuild())) {
            this.memberCooldown.put(member.getGuild(), new HashMap<>());
        }

        this.memberCooldown.get(member.getGuild()).put(member, cooldown);
    }

    public void setUserXP(final Member member, final int xp) {
        final var userDoc = new Document("ID", member.getIdLong());

        MongoCollection<Document> collection = this.levels.get(member.getGuild());
        if (collection == null) {
            this.database.createCollection(member.getGuild().getName());
            collection = this.database.getCollection(member.getGuild().getName());
            this.levels.put(member.getGuild(), collection);
        }

        LinkedList<Document> lb = this.leaderboard.get(member.getGuild());
        if (lb == null) {
            lb = new LinkedList<>();
            this.leaderboard.put(member.getGuild(), lb);
        }

        Document user = collection.find(userDoc).first();
        final var update = new Document("$set", new Document("XP", xp));
        if (user == null) {
            user = userDoc;
            collection.insertOne(user);
        }

        lb.remove(user);
        collection.updateOne(userDoc, update);
        lb.add(this.levels.get(member.getGuild()).find(userDoc).first());

        getOrCreateCard(member);
    }

    public void startTimer(final JDA bot) {
        for (final var collection : this.database.listCollectionNames()) {
            final var guildCollection = this.database.getCollection(collection);
            final var guild = bot.getGuildsByName(collection, false).get(0);
            this.levels.put(guild, guildCollection);
            this.leaderboard.put(guild, new LinkedList<>());
            for (final Document document : this.levels.get(guild).find().sort(new Document("XP", -1))) {
                this.leaderboard.get(guild).add(document);
            }

            for (final Document document : this.levels.get(guild).find(Filters.exists("MemberID"))) {
                final var member = guild.getMemberById(document.getString("MemberID"));
                final var card = new RankCard(member);

                card.backgroundColour = new Color(document.getInteger("BackgroundColour"));
                card.outlineColour = new Color(document.getInteger("OutlineColour"));
                card.rankTextColour = new Color(document.getInteger("RankTextColour"));
                card.levelTextColour = new Color(document.getInteger("LevelTextColour"));
                card.xpOutlineColour = new Color(document.getInteger("XPBarOutlineColour"));
                card.xpEmptyColour = new Color(document.getInteger("XPBarEmptyColour"));
                card.xpFillColour = new Color(document.getInteger("XPBarFillColour"));
                card.avatarOutlineColour = new Color(document.getInteger("AvatarOutlineColour"));
                card.percentTextColour = new Color(document.getInteger("PercentTextColour"));
                card.xpTextColour = new Color(document.getInteger("XPTextColour"));
                card.outlineOpacity = document.getDouble("OutlineOpacity").floatValue();

                card.backgroundImage = document.getString("BackgroundImage");
                card.outlineImage = document.getString("OutlineImage");
                card.xpOutlineImage = document.getString("XPBarOutlineImage");
                card.xpEmptyImage = document.getString("XPBarEmptyImage");
                card.xpFillImage = document.getString("XPBarFillImage");
                card.avatarOutlineImage = document.getString("AvatarOutlineImage");

                final JsonArray inv = document.get("Inventory", JsonArray.class);
                for (final JsonElement elem : inv) {
                    card.inventory.addItem(InventoryItem.parse(elem.getAsJsonObject(), "MongoDB").build(),
                            null);
                }

                updateCard(member, card);
            }
        }

        final var timer = new Timer();
        final var timerTask = new TimerTask() {
            @Override
            public void run() {
                LevellingManager.this.memberCooldown.forEach((guild, map) -> map.keySet()
                        .forEach(member -> setUserCooldown(member, getUserCooldown(member) - 1)));
            }
        };

        timer.schedule(timerTask, 1000, 1000);
    }

    public RankCard updateCard(final Member member, RankCard card) {
        final var userDoc = new Document("ID", member.getIdLong());

        MongoCollection<Document> collection = this.levels.get(member.getGuild());
        if (collection == null) {
            this.database.createCollection(member.getGuild().getName());
            collection = this.database.getCollection(member.getGuild().getName());
            this.levels.put(member.getGuild(), collection);
        }

        LinkedList<Document> lb = this.leaderboard.get(member.getGuild());
        if (lb == null) {
            lb = new LinkedList<>();
            this.leaderboard.put(member.getGuild(), lb);
        }

        Document user = collection.find(userDoc).first();
        if (card == null) {
            card = new RankCard(member);
        }
        final Map<String, Object> items = new HashMap<>();
        items.put("MemberID", card.member.getId());
        items.put("BackgroundColour", card.backgroundColour.getRGB());
        items.put("OutlineColour", card.outlineColour.getRGB());
        items.put("RankTextColour", card.rankTextColour.getRGB());
        items.put("LevelTextColour", card.levelTextColour.getRGB());
        items.put("XPBarOutlineColour", card.xpOutlineColour.getRGB());
        items.put("XPBarEmptyColour", card.xpEmptyColour.getRGB());
        items.put("XPBarFillColour", card.xpFillColour.getRGB());
        items.put("AvatarOutlineColour", card.avatarOutlineColour.getRGB());
        items.put("PercentTextColour", card.percentTextColour.getRGB());
        items.put("XPTextColour", card.xpTextColour.getRGB());
        items.put("OutlineOpacity", card.outlineOpacity);

        items.put("BackgroundImage", card.backgroundImage);
        items.put("OutlineImage", card.outlineImage);
        items.put("XPBarOutlineImage", card.xpOutlineImage);
        items.put("XPBarEmptyImage", card.xpEmptyImage);
        items.put("XPBarFillImage", card.xpFillImage);
        items.put("AvatarOutlineImage", card.avatarOutlineImage);

        final var update = new Document("$set", new Document("RankCard", items));
        if (user == null) {
            user = userDoc;
            collection.insertOne(user);
        }

        collection.updateOne(userDoc, update);
        if (!RANK_CARD_DATA.get(member.getGuild()).containsKey(member)) {
            RANK_CARD_DATA.get(member.getGuild()).put(member, card);
        } else {
            final var map = new HashMap<Member, RankCard>();
            map.put(member, card);
            RANK_CARD_DATA.put(member.getGuild(), map);
        }
        return card;
    }

    protected RankCard getOrCreateCard(final Member member) {
        RankCard rankCard = null;
        if (!RANK_CARD_DATA.containsKey(member.getGuild())) {
            rankCard = getCardFromDB(member);
        } else {
            rankCard = RANK_CARD_DATA.get(member.getGuild()).get(member);
            if (rankCard == null) {
                rankCard = getCardFromDB(member);
            }
        }
        return updateCard(member, rankCard);
    }

    protected void updateInventory(final Member member) {
        final var userDoc = new Document("ID", member.getIdLong());

        MongoCollection<Document> collection = this.levels.get(member.getGuild());
        if (collection == null) {
            this.database.createCollection(member.getGuild().getName());
            collection = this.database.getCollection(member.getGuild().getName());
            this.levels.put(member.getGuild(), collection);
        }

        LinkedList<Document> lb = this.leaderboard.get(member.getGuild());
        if (lb == null) {
            lb = new LinkedList<>();
            this.leaderboard.put(member.getGuild(), lb);
        }

        Document user = collection.find(userDoc).first();

        RankCard card = null;
        final Map<Member, RankCard> guildData = RANK_CARD_DATA.get(member.getGuild());
        if (guildData == null || !guildData.containsKey(member)) {
            card = new RankCard(member);
        }

        if (card == null) {
            card = guildData.get(member);
        }

        final var invJson = new JsonArray();
        card.inventory.items.forEach(item -> invJson.add(InventoryItem.serialize(item)));

        final var update = new Document("$set", new Document("Inventory", invJson));
        if (user == null) {
            user = userDoc;
            collection.insertOne(user);
        }

        collection.updateOne(userDoc, update);
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    private void sortLeaderboard(final Guild guild) {
        LinkedList<Document> lb = this.leaderboard.get(guild);
        if (lb == null) {
            lb = new LinkedList<>();
            this.leaderboard.put(guild, lb);
        }

        final LinkedList<Document> oldBoard = (LinkedList<Document>) lb.clone();
        lb.removeAll(oldBoard);
        Stream.of(oldBoard.toArray(new Document[0]))
                .sorted((doc1, doc2) -> Integer.compare(doc2.getInteger("XP"), doc1.getInteger("XP")))
                .forEach(lb::add);
    }
}
