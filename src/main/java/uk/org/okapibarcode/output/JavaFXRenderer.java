/*
 * Copyright 2014-2015 Robin Stuart, Robert Elliott, Daniel Gredler
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package uk.org.okapibarcode.output;

import static uk.org.okapibarcode.backend.HumanReadableAlignment.CENTER;
import static uk.org.okapibarcode.backend.HumanReadableAlignment.JUSTIFY;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import uk.org.okapibarcode.backend.Hexagon;
import uk.org.okapibarcode.backend.HumanReadableAlignment;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.backend.TextBox;

/**
 * Renders symbologies using the Java FX API.
 * 
 * @author comtel2000
 * 
 */
public class JavaFXRenderer implements SymbolRenderer {

  /** The graphics to render to. */
  private final GraphicsContext context;

  /** The magnification factor to apply. */
  private final double magnification;

  /** The paper (background) color. */
  private final Color paper;

  /** The ink (foreground) color. */
  private final Color ink;

  /**
   * Creates a new Java FX renderer. If the specified paper color is <tt>null</tt>, the symbol is
   * drawn without clearing the existing <tt>g2d</tt> background.
   *
   * @param context the graphics to render to
   * @param magnification the magnification factor to apply
   * @param paper the paper (background) color (may be <tt>null</tt>)
   * @param ink the ink (foreground) color
   */
  public JavaFXRenderer(GraphicsContext context, double magnification, Color paper, Color ink) {
    this.context = context;
    this.magnification = magnification;
    this.paper = paper;
    this.ink = ink;
  }

  /** {@inheritDoc} */
  @Override
  public void render(Symbol symbol) {

    int marginX = (int) (symbol.getQuietZoneHorizontal() * magnification);
    int marginY = (int) (symbol.getQuietZoneVertical() * magnification);

    Font f = Font.font(symbol.getFontName(), FontWeight.NORMAL, (symbol.getFontSize() * magnification));

    context.save();

    if (paper != null) {
      int w = (int) (symbol.getWidth() * magnification);
      int h = (int) (symbol.getHeight() * magnification);
      context.setFill(paper);
      context.fillRect(0, 0, w, h);
    }
    context.setFill(ink);

    for (Rectangle2D.Double rect : symbol.getRectangles()) {
      double x = (rect.x * magnification) + marginX;
      double y = (rect.y * magnification) + marginY;
      double w = rect.width * magnification;
      double h = rect.height * magnification;
      context.fillRect(x, y, w, h);
    }

    for (TextBox text : symbol.getTexts()) {
      HumanReadableAlignment alignment = (text.alignment == JUSTIFY && text.text.length() == 1 ? CENTER : text.alignment);
      Font font = (alignment != JUSTIFY ? f : addTracking(f, text.width * magnification, text.text));
      context.setFont(font);

      Bounds bounds = getBounds(font, text.text);
      float y = (float) (text.y * magnification) + marginY;
      float x;
      switch (alignment) {
        case LEFT:
        case JUSTIFY:
          x = (float) ((magnification * text.x) + marginX);
          break;
        case RIGHT:
          x = (float) ((magnification * text.x) + (magnification * text.width) - bounds.getWidth() + marginX);
          break;
        case CENTER:
          x = (float) ((magnification * text.x) + (magnification * text.width / 2) - (bounds.getWidth() / 2) + marginX);
          break;
        default:
          throw new IllegalStateException("Unknown alignment: " + alignment);
      }
      context.fillText(text.text, x, y);
    }

    for (Hexagon hexagon : symbol.getHexagons()) {
      int nPoints = 6;
      double[] xPoints = new double[nPoints];
      double[] yPoints = new double[nPoints];
      for (int j = 0; j < nPoints; j++) {
        xPoints[j] = ((hexagon.pointX[j] * magnification) + marginX);
        yPoints[j] = ((hexagon.pointY[j] * magnification) + marginY);
      }
      context.fillPolygon(xPoints, yPoints, nPoints);
    }

    for (int i = 0; i < symbol.getTarget().size(); i++) {
      Ellipse2D.Double ellipse = adjust(symbol.getTarget().get(i), magnification, marginX, marginY);
      context.setFill((i & 1) == 0 ? ink : paper);
      context.fillOval(ellipse.x, ellipse.y, ellipse.width, ellipse.height);
    }

    context.restore();
  }

  private static Ellipse2D.Double adjust(Ellipse2D.Double ellipse, double magnification, int marginX, int marginY) {
    double x = (ellipse.x * magnification) + marginX;
    double y = (ellipse.y * magnification) + marginY;
    double w = (ellipse.width * magnification) + marginX;
    double h = (ellipse.height * magnification) + marginY;
    return new Ellipse2D.Double(x, y, w, h);
  }

  private static Font addTracking(Font baseFont, double maxTextWidth, String text) {
    // double originalWidth = getBounds(baseFont, text).getWidth();
    // double extraSpace = maxTextWidth - originalWidth;
    // double extraSpacePerGap = extraSpace / (text.length() - 1);
    // double tracking = extraSpacePerGap / baseFont.getSize();
    return baseFont;
  }

  /** no FX FontMetrics available yet */
  private static Bounds getBounds(Font font, String text) {
    Text t = new Text();
    t.setFont(font);
    t.setText(text);
    return t.getBoundsInLocal();
  }
}
