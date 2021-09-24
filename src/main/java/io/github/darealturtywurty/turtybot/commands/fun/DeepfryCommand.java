package io.github.darealturtywurty.turtybot.commands.fun;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.core.ImageUtils;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class DeepfryCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Deepfries the image.";
    }

    @Override
    public String getName() {
        return "deepfry";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "image", "The image to deepfry", false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        String imageURL = ctx.getAuthor().getEffectiveAvatarUrl() + "?size=512";
        final OptionMapping imageOption = ctx.getEvent().getOption("image");
        if (imageOption != null) {
            imageURL = imageOption.getAsString();
        }

        try {
            BufferedImage image = ImageIO.read(new URL(imageURL));
            image = ImageUtils.deepfry(image);
            ctx.getEvent().deferReply().addFile(ImageUtils.toInputStream(image), "deepfried.png").queue();
        } catch (final IOException e) {
            ctx.getEvent().deferReply(true).setContent("You must supply a valid image URL!").queue();
        }
    }
}
