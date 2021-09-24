package io.github.darealturtywurty.turtybot.commands.fun.siege;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;

import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class R6StatsCommand implements GuildCommand {

    public static EmbedBuilder createEmbed(final JsonObject result) {
        final var embed = new EmbedBuilder();
        embed.setTitle("Stats for: " + result.get("username").getAsString() + " ("
                + result.get("platform").getAsString() + ")");

        embed.setThumbnail(result.get("avatar_url_256").getAsString());

        final JsonObject alias = result.get("aliases").getAsJsonArray().get(0).getAsJsonObject();
        try {
            embed.setAuthor(alias.get("username").getAsString() + " | Last Seen At: "
                    + Constants.DATE_FORMAT.parse(alias.get("last_seen_at").getAsString()));
        } catch (final ParseException e) {
            embed.setAuthor(alias.get("username").getAsString());
        }

        final JsonObject stats = result.getAsJsonObject("stats");
        final JsonObject overallStats = stats.getAsJsonObject("general");
        // @formatter:off
        embed.setDescription("__**Overall Stats:**__"
                + "\nKills: " + overallStats.get("kills").getAsInt()
                + "\nDeaths: " + overallStats.get("deaths").getAsInt()
                + "\nGames Played: " + overallStats.get("games_played").getAsInt()
                + "\nWins: " + overallStats.get("wins").getAsInt()
                + "\nLosses: " + overallStats.get("losses").getAsInt()
                + "\nDraws: " + overallStats.get("draws").getAsInt()
                + "\nAssists: " + overallStats.get("assists").getAsInt()
                + "\nBullets Hit: " + overallStats.get("bullets_hit").getAsInt()
                + "\nHeadshots: " + overallStats.get("headshots").getAsInt()
                + "\nMelee Kills: " + overallStats.get("melee_kills").getAsInt()
                + "\nPenetration Kills: " + overallStats.get("penetration_kills").getAsInt()
                + "\nBlind Kills: " + overallStats.get("blind_kills").getAsInt()
                + "\nDBNOs(Down But Not Out): " + overallStats.get("dbnos").getAsInt()
                + "\nKD: " + overallStats.get("kd").getAsDouble()
                + "\nWL: " + overallStats.get("wl").getAsDouble()
                + "\nBarricades Deployed: " + overallStats.get("barricades_deployed").getAsInt()
                + "\nReinforcements Deployed: " + overallStats.get("reinforcements_deployed").getAsInt()
                + "\nGadgets Destroyed: " + overallStats.get("gadgets_destroyed").getAsInt()
                + "\nRappel Breaches: " + overallStats.get("rappel_breaches").getAsInt()
                + "\nRevives: " + overallStats.get("revives").getAsInt()
                + "\nSuicides: " + overallStats.get("suicides").getAsInt()
                + "\nTime Played: " + BotUtils.millisecondsFormatted(overallStats.get("playtime").getAsLong() * 1000L)
                + "\nDistance Travelled: " + overallStats.get("distance_travelled").getAsLong() + "m");

        // @formatter:on
        final JsonObject progression = result.getAsJsonObject("progression");
        embed.addField("Progression Information:",
                "Level: " + progression.get("level").getAsInt() + "\nTotal XP: "
                        + progression.get("total_xp").getAsInt() + "\nAlpha Pack Chance: "
                        + progression.get("lootbox_probability").getAsInt(),
                false);

        final JsonObject queueStats = stats.getAsJsonObject("queue");
        final JsonObject casualStats = queueStats.getAsJsonObject("casual");
        final JsonObject rankedStats = queueStats.getAsJsonObject("ranked");
        final JsonObject otherStats = queueStats.getAsJsonObject("other");
        embed.addField("Casual Stats:", getQueueStats(casualStats), false);
        embed.addField("Ranked Stats:", getQueueStats(rankedStats), false);
        embed.addField("Unranked and Event Stats:", getQueueStats(otherStats), false);

        embed.setFooter("Ubisoft ID: " + result.get("ubisoft_id").getAsString() + " | Uplay ID: "
                + result.get("uplay_id").getAsString());
        return embed;
    }

    private static String getQueueStats(final JsonObject queue) {
        // @formatter:off
        return "Kills: " + queue.get("kills").getAsInt()
                + "\nDeaths: " + queue.get("deaths").getAsInt()
                + "\nGames Played: " + queue.get("games_played").getAsInt()
                + "\nWins: " + queue.get("wins").getAsInt()
                + "\nLosses: " + queue.get("losses").getAsInt()
                + "\nDraws: " + queue.get("draws").getAsInt()
                + "\nKD: " + queue.get("kd").getAsDouble()
                + "\nWL: " + queue.get("wl").getAsDouble()
                + "\nTime Played: " + BotUtils.millisecondsFormatted(queue.get("playtime").getAsLong() * 1000L);
        // @formatter:on
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Gets the siege stats for a user.";
    }

    @Override
    public String getName() {
        return "r6-stats";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "platform", "The platform that this account is on", true)
                        .addChoice("xbox", "xbox").addChoice("pc", "pc")
                        .addChoice("playstation", "playstation"),
                new OptionData(OptionType.STRING, "username", "The username of the account", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String platform = ctx.getEvent().getOption("platform").getAsString();
        if (!List.of("xbox", "pc", "playstation").contains(platform.trim().toLowerCase())) {
            ctx.getEvent().deferReply(true)
                    .setContent("You must supply a valid platform: `xbox`, `pc`, `playstation`!").queue();
            return;
        }

        final String username = ctx.getEvent().getOption("username").getAsString();
        try {
            final URLConnection urlc = new URL(
                    Constants.R6STATS_URL + "stats/" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                            + "/" + URLEncoder.encode(platform, StandardCharsets.UTF_8) + "/generic")
                                    .openConnection();
            urlc.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            urlc.addRequestProperty("Authorization", "Bearer " + BotUtils.getR6StatsKey());
            urlc.connect();
            final var strBuilder = new StringBuilder();
            new BufferedReader(new InputStreamReader(urlc.getInputStream())).lines()
                    .forEach(line -> strBuilder.append(line + "\n"));
            urlc.getInputStream().close();
            final String json = strBuilder.toString();
            System.out.println(json);
            final JsonObject result = Constants.GSON.fromJson(json, JsonObject.class);
            ctx.getEvent().deferReply().addEmbeds(createEmbed(result).build()).queue();
        } catch (final IOException e) {
            if (e instanceof FileNotFoundException || e.getMessage().contains("response code: 500")) {
                e.printStackTrace();
                ctx.getEvent().deferReply(true)
                        .setContent("You must supply a valid username for this platform!").queue();
                return;
            }

            ctx.getEvent().deferReply(true).setContent(
                    "There was an issue processing this command. Please report this to the bot owner!\n`"
                            + e.toString() + "`")
                    .queue();
            Constants.LOGGER.severe(e.getLocalizedMessage());
        }
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
