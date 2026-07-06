package vimicalc.view;

import javafx.scene.text.FontWeight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static vimicalc.view.Defaults.DEFAULT_FONT_SIZE;

class FormattingTest {

    private Formatting formatting;

    @BeforeEach
    void setUp() {
        formatting = new Formatting();
    }

    // ── Defaults ──

    @Nested
    class DefaultsTests {
        @Test
        void freshFormattingIsDefault() {
            assertTrue(formatting.isDefault());
        }

        @Test
        void fontSizeDefaultsToConstant() {
            assertEquals(DEFAULT_FONT_SIZE, formatting.getFontSize());
        }

        @Test
        void sixArgConstructorGetsDefaultFontSize() {
            Formatting f = new Formatting(
                new short[]{255, 0, 0}, new short[]{0, 0, 0},
                "center", "center", "normal", "regular"
            );
            assertEquals(DEFAULT_FONT_SIZE, f.getFontSize());
        }
    }

    // ── Font size ──

    @Nested
    class FontSizeTests {
        @Test
        void nonDefaultSizeIsNotDefault() {
            formatting.setFontSize(30);
            assertEquals(30, formatting.getFontSize());
            assertFalse(formatting.isDefault());
        }

        @Test
        void resettingSizeRestoresDefault() {
            formatting.setFontSize(30);
            formatting.setFontSize(DEFAULT_FONT_SIZE);
            assertTrue(formatting.isDefault());
        }
    }

    // ── Colors ──

    @Nested
    class ColorTests {
        @Test
        void builtInNameKeepsExactRGB() {
            formatting.setCellColor("lGray");
            assertArrayEquals(new short[]{211, 211, 211}, formatting.getCellColor());
        }

        @Test
        void cssNameIsAccepted() {
            formatting.setCellColor("teal");
            assertArrayEquals(new short[]{0, 128, 128}, formatting.getCellColor());
        }

        @Test
        void hexValueIsAccepted() {
            formatting.setTxtColor("#ff8800");
            assertArrayEquals(new short[]{255, 136, 0}, formatting.getTxtColor());
        }

        @Test
        void unknownNameResetsToDefault() {
            formatting.setCellColor("teal");
            formatting.setCellColor("notacolor");
            assertTrue(formatting.isDefault());
        }

        @Test
        void emptyNameResetsToDefault() {
            formatting.setTxtColor("crimson");
            formatting.setTxtColor("");
            assertTrue(formatting.isDefault());
        }
    }

    // ── Font weight ──

    @Nested
    class FontWeightTests {
        @Test
        void boldMapsToBold() {
            assertEquals(FontWeight.BOLD, formatting.setFXFontWeight("bold"));
        }

        @Test
        void normalMapsToNormal() {
            assertEquals(FontWeight.NORMAL, formatting.setFXFontWeight("normal"));
        }

        @Test
        void numericWeightsMap() {
            assertEquals(FontWeight.BOLD, formatting.setFXFontWeight("700"));
            assertEquals(FontWeight.LIGHT, formatting.setFXFontWeight("300"));
            assertEquals(FontWeight.BLACK, formatting.setFXFontWeight("900"));
        }

        @Test
        void unknownWeightFallsBackToNormal() {
            assertEquals(FontWeight.NORMAL, formatting.setFXFontWeight("banana"));
        }
    }
}
