/*
 * Copyright 2018 Daniel Gredler
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.DataMatrix;
import uk.org.okapibarcode.backend.MaxiCode;
import uk.org.okapibarcode.backend.SymbolTest;

/**
 * Tests for {@link JavaFXRenderer}.
 */
public class JavaFXRendererTest extends ApplicationTest {
  final Group sceneRoot = new Group();

  @Override
  public void start(Stage stage) {

    Scene scene = new Scene(sceneRoot, 750, 650);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  public void testPaperColor() throws Exception {

    Code128 code128 = new Code128();
    code128.setFontName(SymbolTest.DEJA_VU_SANS.getFontName());
    code128.setContent("123456");

    DataMatrix datamatrix = new DataMatrix();
    datamatrix.setContent("ABCDEFG");

    MaxiCode maxicode = new MaxiCode();
    maxicode.setMode(4);
    maxicode.setContent("ABCDEFG");

    int width = 750;
    int height = 650;

    Canvas canvas = new Canvas(width, height);
    canvas.getGraphicsContext2D()
        .setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.ORANGE), new Stop(1, Color.GREENYELLOW)));
    canvas.getGraphicsContext2D().fillRect(0, 0, width, height);

    JavaFXRenderer renderer = new JavaFXRenderer(canvas.getGraphicsContext2D(), 4, null, Color.BLACK);
    canvas.getGraphicsContext2D().translate(25, 25);
    renderer.render(code128);
    canvas.getGraphicsContext2D().translate(300, 0);
    renderer.render(datamatrix);
    canvas.getGraphicsContext2D().translate(100, 0);
    renderer.render(maxicode);

    JavaFXRenderer renderer2 = new JavaFXRenderer(canvas.getGraphicsContext2D(), 4, Color.WHITE, Color.BLACK);
    canvas.getGraphicsContext2D().translate(-400, 300);
    renderer2.render(code128);
    canvas.getGraphicsContext2D().translate(300, 0);
    renderer2.render(datamatrix);
    canvas.getGraphicsContext2D().translate(100, 0);
    renderer2.render(maxicode);

    Platform.runLater(() -> sceneRoot.getChildren().add(canvas));
    WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

    Platform.runLater(() -> {
      WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
      try {
        String filename = "javafx-paper-color.png";
        BufferedImage expected = ImageIO.read(getClass().getResourceAsStream(filename));
        String dirName = filename.substring(0, filename.lastIndexOf('.'));
        SymbolTest.assertEqual(expected, SwingFXUtils.fromFXImage(image, null), dirName);
      } catch (IOException e) {
        fail(e.getMessage());
      }
    });
  }

  @Test
  public void testCustomFont() throws Exception {

    Font font = null;
    String path = "/uk/org/okapibarcode/fonts/OkapiDejaVuSans.ttf";
    try (InputStream fontStream = JavaFXRendererTest.class.getResourceAsStream(path)) {
      font = Font.loadFont(fontStream, 18);
    }
    assertNotNull(font);

    Code128 code128 = new Code128();
    code128.setContent("123456");

    int magnification = 4;
    int w = code128.getWidth() * magnification;
    int h = code128.getHeight() * magnification;

    Canvas canvas = new Canvas(w, h);
    canvas.getGraphicsContext2D().setFont(font);
    JavaFXRenderer renderer = new JavaFXRenderer(canvas.getGraphicsContext2D(), magnification, Color.WHITE, Color.BLACK);
    renderer.render(code128);

    Platform.runLater(() -> sceneRoot.getChildren().add(canvas));
    WaitForAsyncUtils.sleep(2, TimeUnit.SECONDS);
  }

}
