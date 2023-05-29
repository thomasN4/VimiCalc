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

    public short[] getCellColor() {
        return cellColor;
    }

    public short[] getTxtColor() {
        return txtColor;
    }

    public String getvPos() {
        return vPos;
    }

    public String getAlignment() {
        return alignment;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public String getFontPosture() {
        return fontPosture;
    }

    public Color setFXColor(short[] color) {
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

    public VPos setFXVPos(String vPos) {
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

    public TextAlignment setFXAlignment(String alignment) {
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

    public FontWeight setFXFontWeight(String fontWeight) {
        if (fontWeight.equals("bold")) return FontWeight.BOLD;
        else return FontWeight.NORMAL;
    }
    public String setFontWeight(FontWeight fontWeight) {
        if (fontWeight == FontWeight.BOLD) return "bold";
        else return "normal";
    }
    
    public FontPosture setFXFontPosture(String fontPosture) {
        if (fontPosture.equals("italic")) return FontPosture.ITALIC;
        else return FontPosture.REGULAR;
    }
    public String setFontPosture(FontPosture fontPosture) {
        if (fontPosture == FontPosture.ITALIC) return "italic";
        else return "regular";
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