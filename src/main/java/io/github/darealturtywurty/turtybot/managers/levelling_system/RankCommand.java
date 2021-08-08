package io.github.darealturtywurty.turtybot.managers.levelling_system;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.entities.Member;

public class RankCommand implements IGuildCommand {

	private static final char[] CHARS = new char[] { 'k', 'm', 'b', 't' };

	public static BufferedImage cutoutImageMiddle(final BufferedImage image, final int baseWidth, final int baseHeight,
			final int cornerRadius) {
		final var output = new BufferedImage(baseWidth, baseHeight, BufferedImage.TYPE_INT_ARGB);

		final var g2 = output.createGraphics();
		final var area = new Area(new Rectangle2D.Double(0, 0, baseWidth, baseHeight));
		final var toSubtract = new Area(new RoundRectangle2D.Double(cornerRadius, cornerRadius, baseWidth - cornerRadius * 2,
				baseHeight - cornerRadius * 2, cornerRadius, cornerRadius));
		area.subtract(toSubtract);
		g2.setPaint(new TexturePaint(image, new Rectangle2D.Double(0, 0, baseWidth, baseHeight)));
		g2.fill(area);
		g2.dispose();
		return output;
	}

	private static void drawAvatar(final BufferedImage userAvatar, final Graphics2D graphics) {
		graphics.setStroke(new BasicStroke(4));
		final var circleBuffer = new BufferedImage(userAvatar.getWidth(), userAvatar.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		final var avatarGraphics = circleBuffer.createGraphics();
		avatarGraphics.setClip(new Ellipse2D.Float(0, 0, userAvatar.getWidth(), userAvatar.getHeight()));
		avatarGraphics.drawImage(userAvatar, 0, 0, userAvatar.getWidth(), userAvatar.getHeight(), null);
		avatarGraphics.dispose();
		graphics.drawImage(circleBuffer, 55, 48, null);
	}

	public static BufferedImage makeRoundedCorner(final BufferedImage image, final float cornerRadius) {
		final int w = image.getWidth();
		final int h = image.getHeight();
		final var output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		final var g2 = output.createGraphics();

		g2.setComposite(AlphaComposite.Src);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
		g2.setComposite(AlphaComposite.SrcAtop);
		g2.drawImage(image, 0, 0, null);
		g2.dispose();

		return output;
	}

	public static void paintTextWithOutline(final Graphics g, final String text, final Font font, final Color outlineColor,
			final Color fillColor, final float outlineWidth) {
		final var outlineStroke = new BasicStroke(outlineWidth);

		if (g instanceof Graphics2D) {
			final Graphics2D g2 = (Graphics2D) g;

			// remember original settings
			final var originalColor = g2.getColor();
			final var originalStroke = g2.getStroke();
			final var originalHints = g2.getRenderingHints();

			// create a glyph vector from your text
			final var glyphVector = font.createGlyphVector(g2.getFontRenderContext(), text);
			// get the shape object
			final var textShape = glyphVector.getOutline();

			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			g2.setColor(outlineColor);
			g2.setStroke(outlineStroke);
			g2.draw(textShape); // draw outline

			g2.setColor(fillColor);
			g2.fill(textShape); // fill the shape

			// reset to original settings after painting
			g2.setColor(originalColor);
			g2.setStroke(originalStroke);
			g2.setRenderingHints(originalHints);
		}
	}

	/**
	 * Recursive implementation, invokes itself for each factor of a thousand,
	 * increasing the class on each invokation.
	 *
	 * @param n         the number to format
	 * @param iteration in fact this is the class from the array c
	 * @return a String representing the number n formatted in a cool looking way.
	 */
	private static String xpFormat(final double n, final int iteration) {
		if (n < 1000)
			return String.valueOf(n);
		final double d = (long) n / 100 / 10.0;
		final boolean isRound = d * 10 % 10 == 0;// true if the decimal part is equal to 0 (then it's trimmed anyway)
		return d < 1000 ? // this determines the class, i.e. 'k', 'm' etc
				(d > 99.9 || isRound && d > 9.99 ? // this decides whether to trim the decimals
						(int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
				) + "" + CHARS[iteration] : xpFormat(d, iteration + 1);

	}

	private Font usedFont = null;

	public RankCommand() {
		try {
			final var graphicsEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			this.usedFont = Font
					.createFont(Font.TRUETYPE_FONT, TurtyBot.class.getResourceAsStream("/fonts/Code New Roman.otf"))
					.deriveFont(12f);
			graphicsEnv.registerFont(this.usedFont);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> getAliases() {
		return List.of("level");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public String getDescription() {
		return "Gets the rank for the specified user!";
	}

	@Override
	public String getName() {
		return "rank";
	}

	@Override
	public void handle(final CommandContext ctx) {
		var member = ctx.getMember();
		if (!ctx.getMessage().getMentionedMembers().isEmpty()) {
			member = ctx.getMessage().getMentionedMembers().get(0);
		}

		if (ctx.getArgs().length > 0) {
			try {
				member = ctx.getGuild().getMemberById(Integer.parseInt(ctx.getArgs()[0]));
			} catch (final NumberFormatException ex) {
				// Nothing
			}
		}
		ctx.getMessage().reply(makeRankCard(member), "rank_card.png").mentionRepliedUser(false).queue();
	}

	@Nullable
	private File makeRankCard(final Member member) {
		final var location = new File("/levels/cards/" + member.getIdLong() + ".png");

		try {
			final var card = Constants.LEVELLING_MANAGER.getOrCreateCard(member);
			final BufferedImage base = ImageIO.read(TurtyBot.class.getResourceAsStream("/levels/background.png"));
			final BufferedImage outline = ImageIO.read(TurtyBot.class.getResourceAsStream("/levels/outline.png"));
			final var rankCardBuffer = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
			final var graphics = (Graphics2D) rankCardBuffer.getGraphics();
			graphics.addRenderingHints(
					new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			// RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

			// Background
			BufferedImage background;
			if (card.backgroundImage.isBlank() || member.getTimeBoosted() == null && !member.isOwner()
					&& member.getGuild().getIdLong() != 819294753732296776L) {
				final var bgBuf = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
				final var bgGraphics = bgBuf.createGraphics();
				bgGraphics.setColor(card.backgroundColour);
				bgGraphics.fillRect(0, 0, base.getWidth(), base.getHeight());
				bgGraphics.dispose();
				background = bgBuf;
			} else {
				background = ImageIO.read(new URL(card.backgroundImage));
			}

			final var bgBuffer = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
			final var bgGraphics = bgBuffer.createGraphics();
			bgGraphics.setClip(new Rectangle2D.Float(0, 0, base.getWidth(), base.getHeight()));
			bgGraphics.drawImage(background, 0, 0, base.getWidth(), base.getHeight(), null);
			bgGraphics.dispose();
			graphics.drawImage(bgBuffer, 0, 0, base.getWidth(), base.getHeight(), null);

			// Outline
			BufferedImage outlineImg;
			if (card.outlineImage.isBlank() || member.getTimeBoosted() == null && !member.isOwner()
					&& member.getGuild().getIdLong() != 819294753732296776L) {
				final var outBuf = new BufferedImage(outline.getWidth(), outline.getHeight(), BufferedImage.TYPE_INT_ARGB);
				final var outGraphics = outBuf.createGraphics();
				outGraphics.setColor(card.outlineColour);
				outGraphics.fillRect(0, 0, outline.getWidth(), outline.getHeight());
				outGraphics.dispose();
				outlineImg = outBuf;
			} else {
				outlineImg = ImageIO.read(new URL(card.outlineImage));
			}

			outlineImg = cutoutImageMiddle(outlineImg, base.getWidth(), base.getHeight(), 20);

			final var outlineAlpha = card.outlineOpacity;
			final var alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, outlineAlpha);
			graphics.setComposite(alphaComp);
			graphics.drawImage(outlineImg, 0, 0, base.getWidth(), base.getHeight(), null);
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

			// Name
			graphics.setStroke(new BasicStroke(3));
			graphics.setColor(card.nameTextColour);

			var nameFontSize = 52f;
			if (member.getEffectiveName().length() > 12) {
				nameFontSize -= member.getEffectiveName().length() * 1.2f - 12;
			}
			graphics.setFont(this.usedFont.deriveFont(nameFontSize));

			graphics.drawString(member.getEffectiveName(), 250, 110);

			// Rank
			graphics.setColor(card.rankTextColour);
			graphics.setFont(this.usedFont.deriveFont(35f));
			int rank = Constants.LEVELLING_MANAGER.getRank(member);

			var xModifier = 0;
			if (rank >= 10) {
				xModifier += 15;
			}

			if (rank >= 100) {
				xModifier += 15;
			}

			if (rank >= 1000) {
				xModifier += 15;
			}

			if (rank >= 10000) {
				xModifier += 15;
			}

			if (rank < 0) {
				rank = 0;
			}

			graphics.drawString("Rank #" + (rank + 1), 690 - xModifier, 110);

			final var fontMetrics = graphics.getFontMetrics();
			final int textWidth = fontMetrics.stringWidth("Rank #" + (rank + 1));
			graphics.drawLine(250, 130 + fontMetrics.getDescent(), 690 - xModifier + textWidth,
					130 + fontMetrics.getDescent());

			// XP and Level
			int xp = Constants.LEVELLING_MANAGER.getUserXP(member);
			int level = LevellingManager.getLevelForXP(xp);
			int nextLevelXP = LevellingManager.getXPForLevel(level + 1);
			xp -= LevellingManager.getXPForLevel(level);
			nextLevelXP -= LevellingManager.getXPForLevel(level);
			float xpPercent = (float) (xp * 100) / (float) nextLevelXP;
			final var decimalFormat = new DecimalFormat("#.#");
			decimalFormat.setRoundingMode(RoundingMode.CEILING);
			xpPercent = Float.parseFloat(decimalFormat.format(xpPercent));
			if (xp < 0) {
				xp = 0;
			}

			if (xpPercent < 0) {
				xpPercent = 0;
			}

			if (nextLevelXP < 0) {
				nextLevelXP = 0;
			}

			if (level < 0) {
				level = 0;
			}

			final var xpStr = xpFormat(xp, 0);
			final var levelStr = String.valueOf(level);
			final var nextLevelXPStr = xpFormat(nextLevelXP, 0);
			graphics.setColor(card.levelTextColour);
			graphics.drawString("Level " + levelStr, 250, 180);
			graphics.setFont(this.usedFont.deriveFont(25f));

			xModifier = 0;
			if (xpStr.length() > 2) {
				xModifier += 10;
			}

			if (xpStr.length() > 3 || nextLevelXPStr.length() > 3) {
				xModifier += 10;
			}

			if (xpStr.length() > 4 || nextLevelXPStr.length() > 4) {
				xModifier += 10;
			}

			graphics.setColor(card.xpTextColour);
			graphics.drawString(xpStr + " / " + nextLevelXPStr, 670 - xModifier, 180);

			// XP Bar
			if (!card.xpOutlineImage.isBlank() && (member.getTimeBoosted() != null || member.isOwner()
					|| member.getGuild().getIdLong() == 819294753732296776L)) {
				final BufferedImage xpBarOutline = makeRoundedCorner(ImageIO.read(new URL(card.xpOutlineImage)), 20);
				graphics.drawImage(xpBarOutline, 250, 200, 570, 40, null);
			} else {
				graphics.setColor(card.xpOutlineColour);
				graphics.drawRoundRect(250, 200, 570, 40, 10, 10);
			}

			if (!card.xpEmptyImage.isBlank() && (member.getTimeBoosted() != null || member.isOwner()
					|| member.getGuild().getIdLong() == 819294753732296776L)) {
				final BufferedImage xpBarEmpty = makeRoundedCorner(ImageIO.read(new URL(card.xpEmptyImage)), 20);
				graphics.drawImage(xpBarEmpty, 250, 200, 570, 40, null);
			} else {
				graphics.setColor(card.xpEmptyColour);
				graphics.fillRoundRect(250, 200, 570, 40, 10, 10);
			}

			if (!card.xpFillImage.isBlank() && (member.getTimeBoosted() != null || member.isOwner()
					|| member.getGuild().getIdLong() == 819294753732296776L)) {
				final BufferedImage xpBarFill = makeRoundedCorner(ImageIO.read(new URL(card.xpFillImage)), 20);
				graphics.drawImage(xpBarFill, 250, 200, (int) (570 * (xpPercent * 0.01f)), 40, null);
			} else {
				graphics.setColor(card.xpFillColour);
				graphics.fillRoundRect(250, 200, (int) (570 * (xpPercent * 0.01f)), 40, 10, 10);
			}

			graphics.setColor(card.percentTextColour);
			graphics.setFont(this.usedFont.deriveFont(30f));
			graphics.drawString(String.valueOf(xpPercent) + "%", 510, 230);

			// User Avatar
			final BufferedImage userAvatar = ImageIO.read(new URL(member.getUser().getEffectiveAvatarUrl()));

			if (!card.avatarOutlineImage.isBlank() && (member.getTimeBoosted() != null || member.isOwner()
					|| member.getGuild().getIdLong() == 819294753732296776L)) {
				final var avatarOut = new BufferedImage(userAvatar.getWidth(), userAvatar.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				final var avatarOutGraphics = avatarOut.createGraphics();
				avatarOutGraphics.setColor(card.backgroundColour);
				avatarOutGraphics.setClip(new Ellipse2D.Float(0, 0, userAvatar.getWidth(), userAvatar.getHeight()));
				avatarOutGraphics.drawImage(ImageIO.read(new URL(card.avatarOutlineImage)), 0, 0, userAvatar.getWidth(),
						userAvatar.getHeight(), null);
				avatarOutGraphics.dispose();
				graphics.drawImage(avatarOut, 40, 33, userAvatar.getWidth() + 30, userAvatar.getHeight() + 30, null);
				drawAvatar(userAvatar, graphics);
			} else {
				drawAvatar(userAvatar, graphics);
				graphics.setColor(card.avatarOutlineColour);
				graphics.drawOval(55, 48, userAvatar.getWidth(), userAvatar.getHeight());
			}
			graphics.dispose();

			if (!location.exists()) {
				location.mkdirs();
			}
			ImageIO.write(rankCardBuffer, "png", location);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return location;
	}
}
