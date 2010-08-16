package edu.ualberta.med.scannerconfig.dmscanlib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

public class ScanCell {

	public static int ROW_MAX = 8;

	public static int COL_MAX = 12;

	/**
	 * 1 <= row <=8
	 */
	private int row;

	/**
	 * 1<= column <= 12
	 */
	private int column;

	/**
	 * 10 digits
	 */
	private String value;

	public ScanCell(int row, int column, String value) {
		this.row = row;
		this.column = column;
		this.value = value;
	}

	public static ScanCell[][] getScanLibResults() throws Exception {
		ScanCell[][] paletteScanned = new ScanCell[ROW_MAX][COL_MAX];

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

		for (int row = 0; row < ROW_MAX; ++row) {
			for (int col = 0; col < COL_MAX; ++col) {
				if (paletteScanned[row][col] == null) {
					paletteScanned[row][col] = new ScanCell(row, col, null);
				}
			}
		}
		return paletteScanned;
	}

	public static ScanCell[][] getRandom() {
		ScanCell[][] paletteScanned = new ScanCell[ROW_MAX][COL_MAX];
		Random random = new Random();
		for (int indexRow = 0; indexRow < ROW_MAX; indexRow++) {
			for (int indexCol = 0; indexCol < COL_MAX; indexCol++) {
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

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
