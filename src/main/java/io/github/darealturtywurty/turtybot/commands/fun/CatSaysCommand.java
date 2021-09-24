package io.github.darealturtywurty.turtybot.commands.fun;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;

import javax.imageio.ImageIO;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import io.github.darealturtywurty.turtybot.util.core.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class CatSaysCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Random cat image with the supplied text";
    }

    @Override
    public String getName() {
        return "catsays";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "to_say", "What the cat should say?", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String text = ctx.getEvent().getOption("to_say").getAsString();
        if (text.length() > 256) {
            ctx.getEvent().deferReply(true)
                    .setContent("You cannot use this command with more than 256 characters!").queue();
            return;
        }

        final var embed = new EmbedBuilder();
        embed.setColor(BotUtils.generateRandomPastelColor());
        embed.setTimestamp(Instant.now());
        embed.setTitle("Cat says: `" + text + "`");
        embed.setImage("attachment://cat.png");
        final String randQuery = "?" + Constants.RANDOM.nextInt() + "=" + Constants.RANDOM.nextInt();
        try {
            BufferedImage image = ImageIO.read(new URL("https://cataas.com/cat" + randQuery));
            image = ImageUtils.renderMemeText(image, text);
            final var byteStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteStream);
            ctx.getEvent().deferReply().addFile(byteStream.toByteArray(), "cat.png").addEmbeds(embed.build())
                    .queue();
        } catch (final IOException e) {
            ctx.getEvent().deferReply(true)
                    .setContent("There appears to have been an error sending the image to discord. "
                            + "Please report this to the bot author!")
                    .queue();
            e.printStackTrace();
        }
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
