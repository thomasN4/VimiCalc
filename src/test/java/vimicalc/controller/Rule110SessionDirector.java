package vimicalc.controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import vimicalc.Main;
import vimicalc.model.Cell;

import javax.imageio.ImageIO;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless director that <em>implements</em> Rule 110 live in the app
 * (seed + typed RPN formula + yank/paste + paced macro growth) and asserts
 * the sheet matches pure Rule 110.
 *
 * <p>Not part of the day-to-day unit filter in {@code AGENTS.md}. Run under
 * Xvfb when recording:</p>
 *
 * <pre>
 *   Xvfb :99 -screen 0 1280x720x24 &amp;
 *   export DISPLAY=:99
 *   xset -r off
 *   ffmpeg -y -video_size 1280x720 -framerate 25 -f x11grab -i :99 \
 *     -c:v libx264 -pix_fmt yuv420p /tmp/rule110-session.mp4 &amp;
 *   ./gradlew test --tests 'vimicalc.controller.Rule110SessionDirector'
 * </pre>
 *
 * <p>Checkpoint PNGs land in {@code build/rule110-session/}.</p>
 */
@ExtendWith(ApplicationExtension.class)
class Rule110SessionDirector {

    /** Blank-safe Rule 110 next-state RPN (same as {@code Rule110DemoTest}). */
    static final String NEXT =
        "k 0 + kl 0 + + k 0 + kl 0 + * + kh 0 + k 0 + * kl 0 + * + 2 mod";

    static final int WIDTH = 16;
    static final int GENS = 12;
    /** Seed column (right edge). */
    static final int SEED_COL = WIDTH;
    static final int KEY_MS = 55;
    static final int MACRO_DELAY_MS = 90;

    private Controller controller;
    private Scene scene;
    private Path shotDir;

    @Start
    void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("GUI.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        scene = new Scene(root, 900, 600);
        scene.setOnKeyPressed(controller::onKeyPressed);
        stage.setScene(scene);
        stage.setWidth(900);
        stage.setHeight(600);
        stage.setTitle("VimiCalc — Rule 110 session");
        stage.show();
    }

    @Test
    void implementAndRunRule110(FxRobot robot) throws Exception {
        shotDir = Path.of("build/rule110-session");
        Files.createDirectories(shotDir);

        // Pure CA reference grid
        int[][] grid = new int[GENS + 1][WIDTH + 2];
        grid[1][SEED_COL] = 1;
        for (int y = 1; y < GENS; y++) {
            for (int x = 1; x <= WIDTH; x++) {
                grid[y + 1][x] = rule110(grid[y][x - 1], grid[y][x], grid[y][x + 1]);
            }
        }

        robot.sleep(400);
        snapshot(robot, "00-start");

        // Cursor starts at B2 → A1
        typeKey(robot, KeyCode.H);
        typeKey(robot, KeyCode.K);
        assertEquals(1, controller.cellSelector.getXCoord());
        assertEquals(1, controller.cellSelector.getYCoord());

        // Narrow columns so the triangle is visible
        for (int i = 0; i < WIDTH + 2; i++) {
            colon(robot, "resCol -72");
            typeKey(robot, KeyCode.L);
        }
        // back to A1
        for (int i = 0; i < WIDTH + 2; i++) typeKey(robot, KeyCode.H);
        typeKey(robot, KeyCode.K); // may already be row 1
        goHome(robot);
        snapshot(robot, "01-narrowed");

        // Seed: single 1 on right edge of row 1
        for (int i = 1; i < SEED_COL; i++) typeKey(robot, KeyCode.L);
        assertEquals(SEED_COL, controller.cellSelector.getXCoord());
        assertEquals(1, controller.cellSelector.getYCoord());
        typeKey(robot, KeyCode.I);
        typeKey(robot, KeyCode.DIGIT1);
        typeKey(robot, KeyCode.ESCAPE);
        robot.sleep(200);
        Cell seed = controller.sheet.simplyFindCell(SEED_COL, 1);
        assertNotNull(seed, "seed cell should exist");
        assertEquals("1", seed.txt(), "seed must be literal 1, not leaked insert keys");
        snapshot(robot, "02-seed");

        // Go to A2 and type the Rule 110 formula
        goHome(robot);
        typeKey(robot, KeyCode.J);
        assertEquals(1, controller.cellSelector.getXCoord());
        assertEquals(2, controller.cellSelector.getYCoord());

        typeKey(robot, KeyCode.EQUALS);
        assertEquals(Mode.FORMULA, controller.currMode);
        typeFormula(robot, NEXT);
        typeKey(robot, KeyCode.ENTER);
        robot.sleep(150);
        assertEquals(Mode.NORMAL, controller.currMode);
        Cell a2 = controller.sheet.simplyFindCell(1, 2);
        assertNotNull(a2);
        assertNotNull(a2.formula(), "A2 must hold the Rule 110 formula");
        assertEquals(NEXT, a2.formula().getTxt());
        assertEquals(Integer.toString(grid[2][1]), a2.txt(), "A2 formula result");
        snapshot(robot, "03-formula-a2");

        // Yank formula and fill the rest of generation 2
        typeKey(robot, KeyCode.Y);
        typeKey(robot, KeyCode.Y);
        for (int x = 2; x <= WIDTH; x++) {
            typeKey(robot, KeyCode.L);
            typeKey(robot, KeyCode.P);
            typeKey(robot, KeyCode.P);
        }
        robot.sleep(200);
        assertGeneration(grid, 2);
        snapshot(robot, "04-gen2-filled");

        // Record macro f = paste + move right (Vim-style: executes while recording).
        // Starts on the last filled cell of gen 2 → ends one column past WIDTH.
        typeKey(robot, KeyCode.Q);
        typeKey(robot, KeyCode.F);
        typeKey(robot, KeyCode.P);
        typeKey(robot, KeyCode.P);
        typeKey(robot, KeyCode.L);
        typeKey(robot, KeyCode.Q);
        robot.sleep(100);
        assertEquals(WIDTH + 1, controller.cellSelector.getXCoord(),
            "after recording f, cursor should sit one past the row");

        // Record macro n while performing generation 3 once, starting from col WIDTH+1:
        //   j, WIDTH h (→ col A), WIDTH @ f (fill; ends again at WIDTH+1 so @n chains)
        typeKey(robot, KeyCode.Q);
        typeKey(robot, KeyCode.N);
        typeKey(robot, KeyCode.J);
        typeDigits(robot, WIDTH);
        typeKey(robot, KeyCode.H);
        typeDigits(robot, WIDTH);
        typeAt(robot);
        typeKey(robot, KeyCode.F);
        typeKey(robot, KeyCode.Q);
        robot.sleep(300);
        assertGeneration(grid, 3);
        snapshot(robot, "05-gen3-via-macro-record");
        assertEquals(WIDTH + 1, controller.cellSelector.getXCoord(),
            "macro n must leave the cursor one past WIDTH for chaining");

        // Paced playback grows the remaining generations so the CA "runs"
        colon(robot, "macroDelay " + MACRO_DELAY_MS);
        robot.sleep(200);
        snapshot(robot, "06-before-paced");

        // After recording n we sit on the cell past the last paste of gen 3.
        // remaining = GENS - 3 further applications of n.
        int remaining = GENS - 3;
        typeDigits(robot, remaining);
        typeAt(robot);
        typeKey(robot, KeyCode.N);

        waitForReplay(robot, remaining * (WIDTH * 3 + 6) * MACRO_DELAY_MS + 20_000);
        robot.sleep(400);
        snapshot(robot, "07-after-paced");

        // Full grid assertion
        for (int y = 1; y <= GENS; y++) {
            assertGeneration(grid, y);
        }
        snapshot(robot, "08-verified");

        // Optional: show the colored poster (secondary — after live proof)
        if (Files.exists(Path.of("demos/rule110.json"))) {
            colon(robot, "e demos/rule110.json");
            robot.sleep(800);
            goHome(robot);
            snapshot(robot, "09-poster");
        }

        robot.sleep(600);
        snapshot(robot, "10-done");
    }

    /** Types {@code @} via Shift+2 (US layout). */
    private void typeAt(FxRobot robot) {
        robot.press(KeyCode.SHIFT);
        robot.type(KeyCode.DIGIT2);
        robot.release(KeyCode.SHIFT);
        robot.sleep(KEY_MS);
    }

    private void assertGeneration(int[][] grid, int y) {
        for (int x = 1; x <= WIDTH; x++) {
            Cell c = controller.sheet.simplyFindCell(x, y);
            if (grid[y][x] == 0 && (c == null || c.txt() == null || c.txt().isEmpty())) {
                continue; // blank == 0 for seed row edges
            }
            assertNotNull(c, "missing cell (" + x + "," + y + ")");
            String expected = Integer.toString(grid[y][x]);
            assertEquals(expected, c.txt(),
                "Rule 110 mismatch at (" + x + "," + y + ")");
            if (y >= 2) {
                assertNotNull(c.formula(), "gen " + y + " col " + x + " should be a formula");
            }
        }
    }

    private static int rule110(int L, int C, int R) {
        return (C + R + C * R + L * C * R) % 2;
    }

    /** From anywhere on row 1 or below, go to A1 via go-to + harmless left. */
    private void goHome(FxRobot robot) {
        typeKey(robot, KeyCode.G);
        typeKey(robot, KeyCode.A);
        typeKey(robot, KeyCode.DIGIT1);
        typeKey(robot, KeyCode.H);
        assertEquals(1, controller.cellSelector.getXCoord(), "should be on col A");
        assertEquals(1, controller.cellSelector.getYCoord(), "should be on row 1");
    }

    private void colon(FxRobot robot, String command) {
        typeKey(robot, KeyCode.SEMICOLON);
        typeText(robot, command);
        typeKey(robot, KeyCode.ENTER);
        robot.sleep(80);
    }

    private void typeFormula(FxRobot robot, String formula) {
        for (char c : formula.toCharArray()) {
            typeChar(robot, c);
            robot.sleep(18);
        }
    }

    private void typeText(FxRobot robot, String s) {
        for (char c : s.toCharArray()) {
            typeChar(robot, c);
            robot.sleep(25);
        }
    }

    private void typeDigits(FxRobot robot, int n) {
        typeText(robot, Integer.toString(n));
    }

    private void typeChar(FxRobot robot, char c) {
        if (c == ' ') {
            robot.type(KeyCode.SPACE);
        } else if (c == '+') {
            robot.press(KeyCode.SHIFT).type(KeyCode.EQUALS).release(KeyCode.SHIFT);
        } else if (c == '*') {
            robot.press(KeyCode.SHIFT).type(KeyCode.DIGIT8).release(KeyCode.SHIFT);
        } else if (c == '-') {
            robot.type(KeyCode.MINUS);
        } else if (c == '/') {
            robot.type(KeyCode.SLASH);
        } else if (c == '.') {
            robot.type(KeyCode.PERIOD);
        } else if (c == '_') {
            robot.press(KeyCode.SHIFT).type(KeyCode.MINUS).release(KeyCode.SHIFT);
        } else if (c >= '0' && c <= '9') {
            robot.type(KeyCode.valueOf("DIGIT" + c));
        } else if (c >= 'a' && c <= 'z') {
            robot.type(KeyCode.getKeyCode(String.valueOf(Character.toUpperCase(c))));
        } else if (c >= 'A' && c <= 'Z') {
            robot.press(KeyCode.SHIFT)
                .type(KeyCode.getKeyCode(String.valueOf(c)))
                .release(KeyCode.SHIFT);
        } else {
            fail("unsupported character for typing: '" + c + "'");
        }
    }

    private void typeKey(FxRobot robot, KeyCode code) {
        robot.type(code);
        robot.sleep(KEY_MS);
    }

    private void waitForReplay(FxRobot robot, long maxMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + maxMs;
        // Give the Timeline a moment to start
        robot.sleep(200);
        while (controller.keyCommand.isReplaying()
            && System.currentTimeMillis() < deadline) {
            robot.sleep(80);
        }
        assertFalse(controller.keyCommand.isReplaying(),
            "paced macro replay did not finish within " + maxMs + "ms");
    }

    private void snapshot(FxRobot robot, String name) throws Exception {
        robot.sleep(80);
        AtomicReference<WritableImage> img = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                int w = Math.max(1, (int) Math.ceil(scene.getWidth()));
                int h = Math.max(1, (int) Math.ceil(scene.getHeight()));
                img.set(scene.snapshot(new WritableImage(w, h)));
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "snapshot timed out: " + name);
        File out = shotDir.resolve(name + ".png").toFile();
        ImageIO.write(SwingFXUtils.fromFXImage(img.get(), null), "png", out);
        System.out.println("Wrote " + out.getAbsolutePath());
    }
}
