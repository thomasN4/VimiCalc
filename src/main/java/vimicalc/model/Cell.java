package vimicalc.model;

public record Cell(int xCoord, int yCoord, String txt, double val, String[] formula){};