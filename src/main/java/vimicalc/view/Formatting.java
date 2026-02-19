package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;

import static vimicalc.controller.Controller.*;

/**
 * Stores the visual formatting attributes for a single cell.
 *
 * <p>Properties include background color, text color, vertical position,
 * horizontal alignment, font weight, and font posture (italic). All fields
 * are stored as serializable primitives/strings so they can be persisted
 * in {@code .wss} files alongside cell data.</p>
 *
 * <p>Conversion methods bridge between these serializable representations
 * and JavaFX types (e.g. {@link javafx.scene.paint.Color},
 * {@link javafx.geometry.VPos}).</p>
 */
public class Formatting implements Serializable {
    /** The cell background color as RGB. */
    short[] cellColor;
    /** The text color as RGB. */
    short[] txtColor;
    /** The vertical text position. */
    String vPos;
    /** The horizontal text alignment. */
    String alignment;
    /** The font weight (e.g. "bold" or "normal"). */
    String fontWeight;
    /** The font posture (e.g. "italic" or "regular"). */
    String fontPosture;

    /** Creates a formatting instance with all default values. */
    public Formatting() {
        cellColor = setColor(DEFAULT_CELL_C);
        txtColor = setColor(DEFAULT_TXT_C);
        vPos = setVPos(DEFAULT_VPOS);
        alignment = setAlignment(DEFAULT_ALIGNMENT);
        fontWeight = setFontWeight(FontWeight.NORMAL);
        fontPosture = setFontPosture(FontPosture.REGULAR);
    }

    /**
     * Returns the cell background color as an RGB short array.
     *
     * @return the cell color
     */
    public short[] getCellColor() {
        return cellColor;
    }

    /**
     * Returns the text color as an RGB short array.
     *
     * @return the text color
     */
    public short[] getTxtColor() {
        return txtColor;
    }

    /**
     * Returns the vertical text position as a string.
     *
     * @return the vertical position
     */
    public String getvPos() {
        return vPos;
    }

    /**
     * Returns the horizontal text alignment as a string.
     *
     * @return the alignment
     */
    public String getAlignment() {
        return alignment;
    }

    /**
     * Returns the font weight as a string (e.g. "bold" or "normal").
     *
     * @return the font weight
     */
    public String getFontWeight() {
        return fontWeight;
    }

    /**
     * Returns the font posture as a string (e.g. "italic" or "regular").
     *
     * @return the font posture
     */
    public String getFontPosture() {
        return fontPosture;
    }

    /**
     * Converts an RGB short array to a JavaFX {@link Color}.
     *
     * @param color the RGB short array
     * @return the JavaFX color
     */
    public Color setFXColor(short[] color) {
        return Color.rgb(color[0], color[1], color[2]);
    }
    /**
     * Converts a JavaFX {@link Color} to an RGB short array.
     *
     * @param color the JavaFX color
     * @return the RGB short array
     */
    public short[] setColor(Color color) {
        return new short[]{
            (short) (color.getRed() * 255),
            (short) (color.getGreen() * 255),
            (short) (color.getBlue() * 255)
        };
    }

    /**
     * Sets the cell background color.
     *
     * @param color the new background color
     */
    public void setCellColor(Color color) {
        this.cellColor = setColor(color);
    }

    /**
     * Sets the text color.
     *
     * @param color the new text color
     */
    public void setTxtColor(Color color) {
        this.txtColor = setColor(color);
    }

    /**
     * Converts a vertical position string to a JavaFX {@link VPos}.
     *
     * @param vPos the position string
     * @return the JavaFX VPos
     */
    public VPos setFXVPos(String vPos) {
        return switch (vPos) {
            case "top" -> VPos.TOP;
            case "bottom" -> VPos.BOTTOM;
            case "baseline" -> VPos.BASELINE;
            default -> VPos.CENTER;
        };
    }
    /**
     * Converts a JavaFX {@link VPos} to a serializable string.
     *
     * @param vPos the JavaFX VPos
     * @return the string representation
     */
    public String setVPos(VPos vPos) {
        return switch (vPos) {
            case TOP -> "top";
            case BOTTOM -> "bottom";
            case BASELINE -> "baseline";
            default -> "center";
        };
    }

    /**
     * Converts an alignment string to a JavaFX {@link TextAlignment}.
     *
     * @param alignment the alignment string
     * @return the JavaFX TextAlignment
     */
    public TextAlignment setFXAlignment(String alignment) {
        return switch (alignment) {
            case "left" -> TextAlignment.LEFT;
            case "right" -> TextAlignment.RIGHT;
            default -> TextAlignment.CENTER;
        };
    }
    /**
     * Converts a JavaFX {@link TextAlignment} to a serializable string.
     *
     * @param alignment the JavaFX TextAlignment
     * @return the string representation
     */
    public String setAlignment(TextAlignment alignment) {
        return switch (alignment) {
            case LEFT -> "left";
            case RIGHT -> "right";
            default -> "center";
        };
    }

    /**
     * Converts a font weight string to a JavaFX {@link FontWeight}.
     *
     * @param fontWeight the font weight string
     * @return the JavaFX FontWeight
     */
    public FontWeight setFXFontWeight(String fontWeight) {
        if (fontWeight.equals("bold")) return FontWeight.BOLD;
        else return FontWeight.NORMAL;
    }
    /**
     * Converts a JavaFX {@link FontWeight} to a serializable string.
     *
     * @param fontWeight the JavaFX FontWeight
     * @return the string representation
     */
    public String setFontWeight(FontWeight fontWeight) {
        if (fontWeight == FontWeight.BOLD) return "bold";
        else return "normal";
    }

    /**
     * Converts a font posture string to a JavaFX {@link FontPosture}.
     *
     * @param fontPosture the font posture string
     * @return the JavaFX FontPosture
     */
    public FontPosture setFXFontPosture(String fontPosture) {
        if (fontPosture.equals("italic")) return FontPosture.ITALIC;
        else return FontPosture.REGULAR;
    }
    /**
     * Converts a JavaFX {@link FontPosture} to a serializable string.
     *
     * @param fontPosture the JavaFX FontPosture
     * @return the string representation
     */
    public String setFontPosture(FontPosture fontPosture) {
        if (fontPosture == FontPosture.ITALIC) return "italic";
        else return "regular";
    }

    /**
     * Returns {@code true} if all formatting properties match their defaults,
     * meaning this cell needs no special formatting entry.
     *
     * @return whether this formatting is identical to the default
     */
    public boolean isDefault() {
        return (Arrays.equals(cellColor, setColor(DEFAULT_CELL_C)) &&
                Arrays.equals(txtColor, setColor(DEFAULT_TXT_C)) &&
                vPos.equals(setVPos(DEFAULT_VPOS)) &&
                alignment.equals(setAlignment(DEFAULT_ALIGNMENT)) &&
                fontPosture.equals("regular") &&
                fontWeight.equals("normal"));
    }

    /**
     * Renders a cell with this formatting's colors, alignment, and font style.
     *
     * @param gc  the graphics context
     * @param x   the cell's x-coordinate (pixels)
     * @param y   the cell's y-coordinate (pixels)
     * @param w   the cell's width (pixels)
     * @param h   the cell's height (pixels)
     * @param txt the text content to display
     */
    public void renderCell(@NotNull GraphicsContext gc, int x, int y, int w, int h, String txt) {
        gc.setFill(setFXColor(cellColor));
        gc.fillRect(x, y, w, h);
        gc.setFill(setFXColor(txtColor));
        gc.setTextAlign(setFXAlignment(alignment));
        gc.setTextBaseline(setFXVPos(vPos));
        gc.setFont(Font.font(
            gc.getFont().getFamily(),
            setFXFontWeight(fontWeight),
            setFXFontPosture(fontPosture),
            gc.getFont().getSize()
        ));
        gc.fillText(txt, x + (float)w/2, y + (float)h/2, w);
    }
}