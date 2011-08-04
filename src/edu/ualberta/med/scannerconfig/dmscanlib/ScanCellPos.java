package edu.ualberta.med.scannerconfig.dmscanlib;

public class ScanCellPos {

    public static int ROW_MAX = 8;

    public static int COL_MAX = 12;

    private int row;

    private int col;

    public ScanCellPos(int row, int col) {
        this.setRow(row);
        this.setCol(col);
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

}
