package io.github.darealturtywurty.turtybot.commands.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class StrawpollCommand implements GuildCommand {

    public static String create(final StrawpollEntry data) {
        try {
            final var connection = (HttpsURLConnection) new URL(Constants.STRAWPOLL_URL).openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setUseCaches(false);

            final var parent = new JsonObject();
            final var poll = new JsonObject();
            poll.addProperty("title", data.title());
            if (!data.description().isBlank()) {
                poll.addProperty("description", data.description());
            }

            poll.addProperty("priv", data.isPrivate());
            poll.addProperty("ma", data.multiple());
            poll.addProperty("enter_name", data.enterName());
            poll.addProperty("captcha", data.reCAPTCHA());
            poll.addProperty("vpn", data.allowVPN());
            poll.addProperty("co", data.allowComments());
            poll.addProperty("mip", data.duplicationCheck() == DuplicationType.BROWSER);
            if (data.deadline() != null) {
                poll.addProperty("deadline", Constants.DATE_FORMAT.format(data.deadline()));
            }

            final var answers = new JsonArray();
            for (final String option : data.options()) {
                answers.add(option);
            }
            poll.add("answers", answers);
            parent.add("poll", poll);

            final String json = Constants.GSON.toJson(parent);
            connection.getOutputStream().write(json.getBytes());

            final var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            final JsonObject response = Constants.GSON.fromJson(read(reader), JsonObject.class);
            System.out.println(response);
            return "https://strawpoll.com/" + response.get("content_id").getAsString();
        } catch (final IOException | JsonSyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String parseDate(final String[] parts) {
        final var deadlineStrBuilder = new StringBuilder();
        deadlineStrBuilder.append(parts[0] + "-");
        deadlineStrBuilder.append(parts[1] + "-");
        deadlineStrBuilder.append(parts[2] + "T");
        if (parts.length == 3) {
            deadlineStrBuilder.append("00:00:00.000Z");
            return deadlineStrBuilder.toString();
        }

        if (parts.length >= 4) {
            deadlineStrBuilder.append(parts[3] + ":");
            if (parts.length == 4) {
                deadlineStrBuilder.append("00:00.000Z");
                return deadlineStrBuilder.toString();
            }
        }

        if (parts.length >= 5) {
            deadlineStrBuilder.append(parts[4] + ":");
            if (parts.length == 5) {
                deadlineStrBuilder.append("00.000Z");
                return deadlineStrBuilder.toString();
            }
        }

        if (parts.length >= 6) {
            deadlineStrBuilder.append(parts[5] + ".");
            if (parts.length == 5) {
                deadlineStrBuilder.append("000Z");
                return deadlineStrBuilder.toString();
            }
        }

        if (parts.length >= 7) {
            deadlineStrBuilder.append(parts[6] + "Z");
        }

        return deadlineStrBuilder.toString();
    }

    private static String read(final Reader reader) throws IOException {
        final var stringBuilder = new StringBuilder();

        int i;
        while ((i = reader.read()) != -1) {
            stringBuilder.append((char) i);
        }
        return stringBuilder.toString();
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Creates a strawpoll!";
    }

    @Override
    public String getName() {
        return "strawpoll";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "title", "The question to submit.", true),
                new OptionData(OptionType.STRING, "option1", "Option 1", true),
                new OptionData(OptionType.STRING, "option2", "Option 2", true),
                new OptionData(OptionType.STRING, "option3", "Option 3", false),
                new OptionData(OptionType.STRING, "option4", "Option 4", false),
                new OptionData(OptionType.STRING, "option5", "Option 5", false),
                new OptionData(OptionType.STRING, "description", "A description of the question.", false),
                new OptionData(OptionType.BOOLEAN, "private", "Whether or not the question is private.",
                        false),
                new OptionData(OptionType.BOOLEAN, "multiple_choice",
                        "Whether or not the question is multiple choice.", false),
                new OptionData(OptionType.BOOLEAN, "name_required",
                        "Whether or not the participant's name is required.", false),
                new OptionData(OptionType.BOOLEAN, "re-captcha", "Whether or not a reCAPTCHA is required.",
                        false),
                new OptionData(OptionType.BOOLEAN, "allow_vpn", "Whether or not VPNs are allowed.", false),
                new OptionData(OptionType.BOOLEAN, "allow_comments", "Whether or not comments are allowed.",
                        false),
                new OptionData(OptionType.STRING, "duplication_type",
                        "The type of duplication checking that will be used.", false).addChoice("ip", "ip")
                                .addChoice("browser", "browser"),
                new OptionData(OptionType.STRING, "deadline",
                        "\"year-month-day-hour-minute-second-millisecond\", "
                                + "\"-hour-minute-second-millisecond\" is optional"));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final SlashCommandEvent event = ctx.getEvent();
        final String title = event.getOption("title").getAsString();

        final List<String> options = Arrays
                .asList(event.getOption("option1"), event.getOption("option2"), event.getOption("option3"),
                        event.getOption("option4"), event.getOption("option5"))
                .stream().filter(Objects::nonNull).map(OptionMapping::getAsString).toList();

        final OptionMapping descrOption = event.getOption("description");
        final String description = descrOption != null ? descrOption.getAsString() : "";
        final OptionMapping privateOption = event.getOption("private");
        final boolean isPrivate = privateOption != null && privateOption.getAsBoolean();
        final OptionMapping multipleOption = event.getOption("multiple");
        final boolean multiple = multipleOption != null && multipleOption.getAsBoolean();
        final OptionMapping nameRequiredOption = event.getOption("name_required");
        final boolean nameRequired = nameRequiredOption != null && nameRequiredOption.getAsBoolean();
        final OptionMapping reCAPTCHAOption = event.getOption("re-captcha");
        final boolean reCAPTCHA = reCAPTCHAOption == null || reCAPTCHAOption.getAsBoolean();
        final OptionMapping allowVPNOption = event.getOption("allow_vpn");
        final boolean allowVPN = allowVPNOption != null && allowVPNOption.getAsBoolean();
        final OptionMapping allowCommentsOption = event.getOption("allow_comments");
        final boolean allowComments = allowCommentsOption != null && allowCommentsOption.getAsBoolean();
        final OptionMapping duplicationOption = event.getOption("duplication_type");
        final DuplicationType duplicationCheck = duplicationOption != null
                ? DuplicationType.valueOf(duplicationOption.getAsString().toUpperCase())
                : DuplicationType.IP;
        final OptionMapping dateOption = event.getOption("deadline");
        Date deadline = null;
        if (dateOption != null) {
            final String dateStr = dateOption.getAsString();
            final String[] parts = dateStr.split("-");
            if (parts.length < 3) {
                ctx.getEvent().deferReply(true)
                        .setContent("You must supply a valid date format, for example `2020-02-27`.").queue();
                return;
            }

            try {
                deadline = Constants.DATE_FORMAT.parse(parseDate(parts));
            } catch (final ParseException e) {
                Constants.LOGGER.log(Level.WARNING, e.getLocalizedMessage());
            }
        }

        event.deferReply()
                .setContent(create(new StrawpollEntry(title, description, isPrivate, multiple, nameRequired,
                        reCAPTCHA, allowVPN, allowComments, duplicationCheck, deadline,
                        options.toArray(new String[0]))))
                .queue();
    }

    @Override
    public boolean productionReady() {
        return true;
    }

    public enum DuplicationType {
        IP, BROWSER;
    }

    public record StrawpollEntry(String title, String description, boolean isPrivate, boolean multiple,
            boolean enterName, boolean reCAPTCHA, boolean allowVPN, boolean allowComments,
            DuplicationType duplicationCheck, Date deadline, String... options) {
    }
}
