package io.github.darealturtywurty.turtybot.util;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

public class StringMetrics {

    private final Font font;
    private final FontRenderContext context;

    public StringMetrics(final Graphics2D g2) {
        this.font = g2.getFont();
        this.context = g2.getFontRenderContext();
    }

    public Rectangle2D getBounds(final String message) {
        return this.font.getStringBounds(message, this.context);
    }

    public double getHeight(final String message) {
        final Rectangle2D bounds = getBounds(message);
        return bounds.getHeight();
    }

    public double getWidth(final String message) {
        final Rectangle2D bounds = getBounds(message);
        return bounds.getWidth();
    }
}