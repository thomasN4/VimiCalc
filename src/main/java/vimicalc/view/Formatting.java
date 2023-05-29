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

public class Formatting implements Serializable {
    short[] cellColor, txtColor;
    String vPos, alignment, fontWeight, fontPosture;

    public Formatting() {
        cellColor = setColor(DEFAULT_CELL_C);
        txtColor = setColor(DEFAULT_TXT_C);
        vPos = setVPos(DEFAULT_VPOS);
        alignment = setAlignment(DEFAULT_ALIGNMENT);
        fontWeight = setFontWeight(FontWeight.NORMAL);
        fontPosture = setFontPosture(FontPosture.REGULAR);
    }

    public Color getColor(short[] color) {
        return Color.rgb(color[0], color[1], color[2]);
    }
    public short[] setColor(Color color) {
        return new short[]{
            (short) (color.getRed() * 255),
            (short) (color.getGreen() * 255),
            (short) (color.getBlue() * 255)
        };
    }

    public void setCellColor(Color color) {
        this.cellColor = setColor(color);
    }

    public void setTxtColor(Color color) {
        this.txtColor = setColor(color);
    }

    public VPos getVPos(String vPos) {
        return switch (vPos) {
            case "top" -> VPos.TOP;
            case "bottom" -> VPos.BOTTOM;
            case "baseline" -> VPos.BASELINE;
            default -> VPos.CENTER;
        };
    }
    public String setVPos(VPos vPos) {
        return switch (vPos) {
            case TOP -> "top";
            case BOTTOM -> "bottom";
            case BASELINE -> "baseline";
            default -> "center";
        };
    }

    public TextAlignment getAlignment(String alignment) {
        return switch (alignment) {
            case "left" -> TextAlignment.LEFT;
            case "right" -> TextAlignment.RIGHT;
            default -> TextAlignment.CENTER;
        };
    }
    public String setAlignment(TextAlignment alignment) {
        return switch (alignment) {
            case LEFT -> "left";
            case RIGHT -> "right";
            default -> "center";
        };
    }

    public FontWeight getFontWeight(String fontWeight) {
        return switch (fontWeight) {
            case "bold" -> FontWeight.BOLD;
            default -> FontWeight.NORMAL;
        };
    }
    public String setFontWeight(FontWeight fontWeight) {
        return switch (fontWeight) {
            default -> "normal";
        };
    }
    
    public FontPosture getFontPosture(String fontPosture) {
        return switch (fontPosture) {
            case "italic" -> FontPosture.ITALIC;
            default -> FontPosture.REGULAR;
        };
    }
    public String setFontPosture(FontPosture fontPosture) {
        return switch (fontPosture) {
            default -> "regular";
        };
    }

    public boolean isDefault() {
        return (Arrays.equals(cellColor, setColor(DEFAULT_CELL_C)) &&
                Arrays.equals(txtColor, setColor(DEFAULT_TXT_C)) &&
                vPos.equals(setVPos(DEFAULT_VPOS)) &&
                alignment.equals(setAlignment(DEFAULT_ALIGNMENT)) &&
                fontPosture.equals("regular") &&
                fontWeight.equals("normal"));
    }

    public void renderCell(@NotNull GraphicsContext gc, int x, int y, int w, int h, String txt) {
        gc.setFill(getColor(cellColor));
        gc.fillRect(x, y, w, h);
        gc.setFill(getColor(txtColor));
        gc.setTextAlign(getAlignment(alignment));
        gc.setTextBaseline(getVPos(vPos));
        gc.setFont(Font.font(
            gc.getFont().getFamily(),
            getFontWeight(fontWeight),
            getFontPosture(fontPosture),
            gc.getFont().getSize()
        ));
        gc.fillText(txt, x + (float)w/2, y + (float)h/2, w);
    }
}