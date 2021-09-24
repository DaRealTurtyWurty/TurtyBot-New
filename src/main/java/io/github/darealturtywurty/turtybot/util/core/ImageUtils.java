package io.github.darealturtywurty.turtybot.util.core;

import static io.github.darealturtywurty.turtybot.util.Constants.RANDOM;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import com.jhlabs.image.FlareFilter;
import com.jhlabs.image.SphereFilter;
import com.jhlabs.image.TwirlFilter;
import com.jhlabs.image.WarpFilter;
import com.jhlabs.image.WarpGrid;
import com.jhlabs.image.WaterFilter;

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.StringMetrics;
import io.github.darealturtywurty.turtybot.util.data.EmojiImageType;
import io.github.darealturtywurty.turtybot.util.math.Vector2i;

public class ImageUtils {

    public static BufferedImage buldgeImage(final BufferedImage image, @Nullable final Point2D center,
            final float radius, final float refractionIndex) {
        final var retImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = retImage.createGraphics();

        final var filter = new SphereFilter();
        filter.setRadius(radius);
        filter.setRefractionIndex(refractionIndex);
        if (center != null) {
            filter.setCentre(center);
        }
        final BufferedImage effectImage = filter.filter(image, null);

        graphics.drawImage(effectImage, 0, 0, null, null);
        graphics.dispose();
        return retImage;
    }

    public static float clamp(final float min, final float max, final float value) {
        return Math.max(min, Math.min(max, value));
    }

    public static BufferedImage deepfry(final BufferedImage image) {
        BufferedImage retImage = image;

        for (int run = 0; run < RANDOM.nextInt(5) + 2; run++) {
            retImage = lensFlare(retImage,
                    new Point2D.Double(RANDOM.nextInt(retImage.getWidth()),
                            RANDOM.nextInt(retImage.getHeight())),
                    RANDOM.nextInt(2) == 0 ? Color.RED : Color.CYAN,
                    RANDOM.nextInt(50 * retImage.getWidth())
                            - retImage.getWidth() / (retImage.getWidth() / 16),
                    RANDOM.nextInt(retImage.getWidth() / 16), RANDOM.nextInt(retImage.getWidth() / 16)
                            - retImage.getWidth() / (retImage.getWidth() / 16),
                    0, 1);
        }

        for (int run = 0; run < RANDOM.nextInt(3) + 1; run++) {
            final int xPos = RANDOM.nextInt(retImage.getWidth());
            final int yPos = RANDOM.nextInt(retImage.getHeight());
            retImage = placeOkHands(retImage, new Vector2i(xPos, yPos));
        }

        return retImage;
    }

    public static BufferedImage fromInputStream(final InputStream inputStream) {
        try {
            return ImageIO.read(inputStream);
        } catch (final IOException e) {
            Constants.LOGGER.severe(e.getLocalizedMessage());
            throw new IllegalArgumentException(e);
        }
    }

    public static BufferedImage getEmojiImage(final EmojiImageType type) {
        return fromInputStream(readEmojiImage(type.fileName));
    }

    public static int[] getPixels(final Image image, final int width, final int height, final int startX,
            final int startY) {
        final int[] pixels = new int[width * height];
        final var pixelGrabber = new PixelGrabber(image, startX, startY, width, height, pixels, 0, width);
        try {
            pixelGrabber.grabPixels();
        } catch (final InterruptedException e) {
            Constants.LOGGER.severe("interrupted waiting for pixels!");
            return new int[0];
        }

        if ((pixelGrabber.status() & ImageObserver.ABORT) != 0) {
            Constants.LOGGER.severe("image fetch aborted or errored");
            return new int[0];
        }
        return pixels;
    }

    public static BufferedImage lensFlare(final BufferedImage image, @Nullable final Point2D center,
            @Nullable final Color color, final float radius, final float baseAmount, final float rayAmount,
            final float ringAmount, final float ringWidth) {
        final var retImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = retImage.createGraphics();

        final var filter = new FlareFilter();
        filter.setRadius(radius);
        filter.setBaseAmount(baseAmount);
        filter.setRayAmount(rayAmount);
        filter.setRingAmount(ringAmount);
        filter.setRingWidth(ringWidth);
        if (center != null) {
            filter.setCentre(center);
        }

        if (color != null) {
            filter.setColor(color.getRGB());
        }

        final BufferedImage effectImage = filter.filter(image, null);
        graphics.drawImage(effectImage, 0, 0, null, null);
        graphics.dispose();
        return retImage;
    }

    public static BufferedImage overlayImage(final BufferedImage mainImage, final BufferedImage overlayImage,
            final Vector2i position, final Vector2i scale) {
        final var retImage = new BufferedImage(mainImage.getWidth(), mainImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = retImage.createGraphics();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        graphics.drawImage(mainImage, 0, 0, null, null);
        graphics.drawImage(overlayImage, position.x, position.y, scale.x, scale.y, null, null);
        graphics.dispose();
        return retImage;
    }

    public static BufferedImage placeOkHands(final BufferedImage image, @Nullable Vector2i center) {
        if (center == null) {
            center = new Vector2i(RANDOM.nextInt(image.getWidth()), RANDOM.nextInt(image.getHeight()));
        }

        final BufferedImage emoji = getEmojiImage(EmojiImageType.OK_HAND);
        final int size = RANDOM.nextInt(image.getWidth() / 4) + image.getWidth() / 4;
        return overlayImage(image, emoji, center, new Vector2i(size, size));
    }

    public static InputStream readEmojiImage(final String name) {
        return Objects.requireNonNull(TurtyBot.class.getResourceAsStream("/emojis/" + name + ".png"));
    }

    public static BufferedImage renderMemeText(final BufferedImage image, final String text) {
        final var bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(image, null, 0, 0);
        graphics.setColor(Color.WHITE);
        final var font = new Font("Impact", Font.BOLD, 20);
        final var strMetrics = new StringMetrics(graphics);
        graphics.setFont(font);
        graphics.drawString(text, (int) (image.getWidth() / 2 - strMetrics.getWidth(text)),
                image.getHeight() / 3);
        return bufferedImage;
    }

    public static InputStream toInputStream(final RenderedImage image) throws IOException {
        final var outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public static BufferedImage twirlImage(final BufferedImage image, @Nullable final Point2D center,
            final float radius, final float angle) {
        final var retImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = retImage.createGraphics();

        final var filter = new TwirlFilter();
        filter.setRadius(radius);
        filter.setAngle(angle);
        if (center != null) {
            filter.setCentre(center);
        }
        final BufferedImage effectImage = filter.filter(image, null);

        graphics.drawImage(effectImage, 0, 0, null, null);
        graphics.dispose();
        return retImage;
    }

    public static BufferedImage warpImage(final BufferedImage image, final int rows, final int columns) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        final var retImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = retImage.createGraphics();

        final var filter = new WarpFilter();
        final int[] srcPixels = getPixels(image, width, height, 0, 0);
        final int[] destPixels = getPixels(image, width * 2, height * 2, width / 2, height / 2);
        final int[] outPixels = new int[width * height];
        filter.morph(srcPixels, destPixels, outPixels, new WarpGrid(rows, columns, width, height),
                new WarpGrid(rows, columns, width, height), width, height, 1);
        final var effectImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        effectImage.setRGB(0, 0, width, height, outPixels, 0, width);

        graphics.drawImage(effectImage, 0, 0, null, null);
        graphics.dispose();
        return retImage;
    }

    public static BufferedImage waterFilter(final BufferedImage image, @Nullable final Point2D center,
            final float amplitude, final float radius, final float wavelength, final float phase) {
        final var retImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = retImage.createGraphics();

        final var filter = new WaterFilter();
        filter.setAmplitude(amplitude);
        filter.setRadius(radius);
        filter.setWavelength(wavelength);
        filter.setPhase(phase);
        if (center != null) {
            filter.setCentre(center);
        }
        final BufferedImage effectImage = filter.filter(image, null);

        graphics.drawImage(effectImage, 0, 0, null, null);
        graphics.dispose();
        return retImage;
    }
}
