package edu.ualberta.med.scannerconfig.dmscanlib;

import java.io.BufferedReader;
import java.io.FileReader;
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

    public static ScanCell[][] getScanLibResults() throws Exception {
        ScanCell[][] paletteScanned = new ScanCell[ScanCellPos.ROW_MAX][ScanCellPos.COL_MAX];

        BufferedReader in = new BufferedReader(new FileReader("dmscanlib.txt"));

        String str;
        while ((str = in.readLine()) != null) {
            if (str.charAt(0) == '#')
                continue;
            String[] fields = str.split(",");
            if (fields.length != 4)
                throw new RuntimeException();
            int row = fields[1].charAt(0) - 'A';
            int col = Integer.parseInt(fields[2]) - 1;

            paletteScanned[row][col] = new ScanCell(row, col, fields[3]);
        }
        in.close();

        for (int row = 0; row < ScanCellPos.ROW_MAX; ++row) {
            for (int col = 0; col < ScanCellPos.COL_MAX; ++col) {
                if (paletteScanned[row][col] == null) {
                    paletteScanned[row][col] = new ScanCell(row, col, null);
                }
            }
        }
        return paletteScanned;
    }

    public static ScanCell[][] getRandom() {
        ScanCell[][] paletteScanned = new ScanCell[ScanCellPos.ROW_MAX][ScanCellPos.COL_MAX];
        Random random = new Random();
        for (int indexRow = 0; indexRow < ScanCellPos.ROW_MAX; indexRow++) {
            for (int indexCol = 0; indexCol < ScanCellPos.COL_MAX; indexCol++) {
                StringBuffer digits = new StringBuffer();
                if (random.nextBoolean()) {
                    for (int i = 0; i < 10; i++) {
                        digits.append(random.nextInt(10));
                    }
                    paletteScanned[indexRow][indexCol] = new ScanCell(indexRow,
                        indexCol, digits.toString());
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
