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

import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import io.github.darealturtywurty.turtybot.util.data.InventoryItem;
import io.github.darealturtywurty.turtybot.util.data.RankCard;
import io.vavr.collection.Stream;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LevellingManager extends ListenerAdapter {

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

    private static GuildInfo getGuildInfo(final long guildID) {
        return CoreBotUtils.GUILDS.get(guildID);
    }

    private static float lerp(final float min, final float max, final float numb) {
        return min * (1.0f - numb) + max * numb;
    }

    public Document findCard(final Member member) {
        var collection = getGuildInfo(member.getGuild().getIdLong()).levels;
        if (collection == null) {
            this.database.createCollection(member.getGuild().getName());
            collection = this.database.getCollection(member.getGuild().getName());
            getGuildInfo(member.getGuild().getIdLong()).levels = collection;
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
        return getGuildInfo(guild.getIdLong()).leaderboard;
    }

    public int getRank(final Member member) {
        sortLeaderboard(member.getGuild());
        return Stream.of(getGuildInfo(member.getGuild().getIdLong()).leaderboard.toArray(new Document[0]))
                .indexWhere(doc -> doc.getLong("ID") == member.getIdLong());
    }

    public int getStoredMemberCount(final long guildID) {
        return getGuildInfo(guildID).leaderboard.size();
    }

    public int getUserCooldown(final long guildID, final long userID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guildID);
        if (info.xpCooldown.containsKey(userID))
            return info.xpCooldown.get(userID);
        return 0;
    }

    public int getUserXP(final Member member) {
        MongoCollection<Document> collection = getGuildInfo(member.getGuild().getIdLong()).levels;
        if (collection == null) {
            try {
                collection = this.database.getCollection(member.getGuild().getName());
            } catch (final IllegalArgumentException e) {
                this.database.createCollection(member.getGuild().getName());
                collection = this.database.getCollection(member.getGuild().getName());
            }

            getGuildInfo(member.getGuild().getIdLong()).levels = collection;
        }

        final List<Document> lb = getGuildInfo(member.getGuild().getIdLong()).leaderboard;

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
        // TODO: Fix
        if (BotUtils.notTestServer(event.getGuild()))
            return;
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
                            / Constants.MONTH_TO_MILLI));
            boostMultiplier += months / 12f;
        }

        if (getUserCooldown(member.getGuild().getIdLong(), member.getIdLong()) <= 0) {
            final int level = getLevelForXP(getUserXP(member));
            setUserXP(member, getUserXP(member)
                    + Math.round((Constants.RANDOM.nextInt(25) + 5) * boostMultiplier * messageMultiplier));
            setUserCooldown(member.getGuild().getIdLong(), member.getIdLong(), 15);
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

    public void setUserCooldown(final long guildID, final long userID, final int cooldown) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guildID);
        info.xpCooldown.put(userID, cooldown);
    }

    public void setUserXP(final Member member, final int xp) {
        final var userDoc = new Document("ID", member.getIdLong());

        MongoCollection<Document> collection = getGuildInfo(member.getGuild().getIdLong()).levels;
        if (collection == null) {
            this.database.createCollection(member.getGuild().getName());
            collection = this.database.getCollection(member.getGuild().getName());
            getGuildInfo(member.getGuild().getIdLong()).levels = collection;
        }

        final List<Document> lb = getGuildInfo(member.getGuild().getIdLong()).leaderboard;

        Document user = collection.find(userDoc).first();
        final var update = new Document("$set", new Document("XP", xp));
        if (user == null) {
            user = userDoc;
            collection.insertOne(user);
        }

        lb.remove(user);
        collection.updateOne(userDoc, update);
        lb.add(getGuildInfo(member.getGuild().getIdLong()).levels.find(userDoc).first());

        getOrCreateCard(member);
    }

    public void startTimer(final JDA bot) {
        for (final var collection : this.database.listCollectionNames()) {
            final var guildCollection = this.database.getCollection(collection);
            final var guild = bot.getGuildsByName(collection, false).get(0);
            getGuildInfo(guild.getIdLong()).levels = guildCollection;
            for (final Document document : getGuildInfo(guild.getIdLong()).levels.find()
                    .sort(new Document("XP", -1))) {
                getGuildInfo(guild.getIdLong()).leaderboard.add(document);
            }

            for (final Document document : getGuildInfo(guild.getIdLong()).levels
                    .find(Filters.exists("MemberID"))) {
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
                CoreBotUtils.GUILDS.forEach((guildID, info) -> info.xpCooldown.forEach((userID,
                        cooldown) -> setUserCooldown(guildID, userID, getUserCooldown(guildID, userID) - 1)));
            }
        };

        timer.schedule(timerTask, 1000, 1000);
    }

    public RankCard updateCard(final Member member, RankCard card) {
        final var userDoc = new Document("ID", member.getIdLong());

        MongoCollection<Document> collection = getGuildInfo(member.getGuild().getIdLong()).levels;
        if (collection == null) {
            this.database.createCollection(member.getGuild().getName());
            collection = this.database.getCollection(member.getGuild().getName());
            getGuildInfo(member.getGuild().getIdLong()).levels = collection;
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
        getGuildInfo(member.getGuild().getIdLong()).userRankCards.put(member.getIdLong(), card);
        return card;
    }

    protected RankCard getOrCreateCard(final Member member) {
        final Map<Long, RankCard> cardMap = getGuildInfo(member.getGuild().getIdLong()).userRankCards;
        RankCard rankCard = null;
        if (!cardMap.containsKey(member.getIdLong())) {
            rankCard = getCardFromDB(member);
        } else {
            rankCard = cardMap.get(member.getIdLong());
            if (rankCard == null) {
                rankCard = getCardFromDB(member);
            }
        }
        return updateCard(member, rankCard);
    }

    protected void updateInventory(final Member member) {
        final var userDoc = new Document("ID", member.getIdLong());

        MongoCollection<Document> collection = getGuildInfo(member.getGuild().getIdLong()).levels;
        if (collection == null) {
            this.database.createCollection(member.getGuild().getName());
            collection = this.database.getCollection(member.getGuild().getName());
            getGuildInfo(member.getGuild().getIdLong()).levels = collection;
        }

        Document user = collection.find(userDoc).first();

        RankCard card = null;
        final Map<Long, RankCard> rankCards = getGuildInfo(member.getGuild().getIdLong()).userRankCards;
        if (rankCards == null || !rankCards.containsKey(member.getIdLong())) {
            card = new RankCard(member);
        }

        if (card == null) {
            card = rankCards.get(member.getIdLong());
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
        final List<Document> lb = getGuildInfo(guild.getIdLong()).leaderboard;
        final List<Document> oldBoard = (LinkedList<Document>) ((LinkedList<Document>) lb).clone();
        lb.removeAll(oldBoard);
        Stream.of(oldBoard.toArray(new Document[0]))
                .sorted((doc1, doc2) -> Integer.compare(doc2.getInteger("XP"), doc1.getInteger("XP")))
                .forEach(lb::add);
    }
}
