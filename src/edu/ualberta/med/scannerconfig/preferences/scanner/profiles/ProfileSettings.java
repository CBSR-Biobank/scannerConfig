package edu.ualberta.med.scannerconfig.preferences.scanner.profiles;

import java.util.BitSet;

/**
 * One bit is used to represent whether the profile will scan a single cell.
 * There are NUM_CELLS on a plate. The first cell is A1 and the last cell is
 * H12.
 * 
 */
public class ProfileSettings extends BitSet {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int NUM_CELLS = 96;

    private static final int NUM_WORDS = NUM_CELLS / 32;

    private String name;

    public ProfileSettings(String name) {
        super(NUM_CELLS);
        this.name = name;
    }

    /*
     * The first word is for cells 0 to 31, the second for cells 32 to 63, and
     * the third for 64 to 95;
     */
    public ProfileSettings(String name, int[] words) throws Exception {
        this(name);
        if (words.length != NUM_WORDS) {
            throw new Exception("invalid int array"); //$NON-NLS-1$
        }
        for (int i = 0; i < words.length; i++) {
            int flag = 1;
            for (int j = 0; j < 32; j++) {
                if ((words[i] & flag) != 0) {
                    set(32 * i + j);
                }
                flag <<= 1;
            }
        }
    }

    public boolean equals() {
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAll() {
        set(0, NUM_CELLS);
    }

    public int[] toWords() {
        int[] words = new int[NUM_WORDS];
        for (int i = 0; i < words.length; i++) {
            words[i] = 0;
            int flag = 1;
            for (int j = 0; j < 32; j++) {
                if (get(32 * i + j)) {
                    words[i] |= flag;
                }
                flag <<= 1;
            }
        }
        return words;
    }

}