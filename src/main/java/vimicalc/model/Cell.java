package vimicalc.model;

import java.util.ArrayList;

public record Cell(int xCoord, int yCoord, String txt, double val, ArrayList<String> formula){};