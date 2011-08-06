package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScanCell {

    private ScanCellPos pos;

    /**
     * 10 digits
     */
    private String value;

    public ScanCell(int row, int col, String value) {
        pos = new ScanCellPos(row, col);
        this.value = value;
    }

    public static List<ScanCell> getRandom() {
        List<ScanCell> paletteScanned = new ArrayList<ScanCell>();
        Random random = new Random();
        for (int indexRow = 0; indexRow < ScanCellPos.ROW_MAX; indexRow++) {
            for (int indexCol = 0; indexCol < ScanCellPos.COL_MAX; indexCol++) {
                StringBuffer digits = new StringBuffer();
                if (random.nextBoolean()) {
                    for (int i = 0; i < 10; i++) {
                        digits.append(random.nextInt(10));
                    }
                    paletteScanned.add(new ScanCell(indexRow, indexCol, digits
                        .toString()));
                }
            }
        }
        return paletteScanned;
    }

    public ScanCellPos getPosition() {
        return pos;
    }

    public int getRow() {
        return pos.getRow();
    }

    public int getColumn() {
        return pos.getCol();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
