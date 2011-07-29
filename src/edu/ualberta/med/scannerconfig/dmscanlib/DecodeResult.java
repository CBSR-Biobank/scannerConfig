package edu.ualberta.med.scannerconfig.dmscanlib;

public class DecodeResult extends ScanLibResult {

    private ScanCell[][] cells = null;

    public DecodeResult(int resultCode, int value, String message) {
        super(resultCode, value, message);
    }

    public ScanCell getCell(int row, int col) {
        if ((row < 0) || (row >= ScanCell.ROW_MAX)) {
            throw new IndexOutOfBoundsException("invalid row: " + row);
        }

        if ((col < 0) || (col >= ScanCell.COL_MAX)) {
            throw new IndexOutOfBoundsException("invalid column: " + col);
        }

        if (cells == null) {
            throw new NullPointerException("cells were not initialized");
        }

        return cells[row][col];
    }

    public void setCell(int row, int col, String message) {
        if ((row < 0) || (row >= ScanCell.ROW_MAX)) {
            throw new IndexOutOfBoundsException("invalid row: " + row);
        }

        if ((col < 0) || (col >= ScanCell.COL_MAX)) {
            throw new IndexOutOfBoundsException("invalid column: " + col);
        }

        if (cells == null) {
            cells = new ScanCell[ScanCell.ROW_MAX][ScanCell.COL_MAX];
        }

        cells[row][col] = new ScanCell(row, col, message);
    }
}
