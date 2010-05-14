package edu.ualberta.med.scannerconfig.calibration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.ScannerRegion;
import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
import edu.ualberta.med.scannerconfig.scanlib.ScanCell;
import edu.ualberta.med.scannerconfig.scanlib.ScanLib;

public class FitnessFunct extends FitnessFunction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public final static double CELLDIST_LIST[] = { 0.32, 0.325, 0.33, 0.335,
            0.34, 0.345, 0.35, 0.355 };

    public final static double GAPS_LIST[] = { 0.06, 0.065, 0.07, 0.075, 0.08,
            0.085 };

    public final static int THRESHOLD_LIST[] = { 5, 10, 15, 20, 25, 30, 35, 40,
            45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100 };

    public final static int SQUAREDEV_LIST[] = { 1, 3, 4, 5, 6, 7, 8, 9, 10,
            11, 12, 13, 14, 15 };

    public final static int CORRECTION_LIST[] = { 0, 1, 5, 10, 15, 20 };

    public final static int BRIGHTNESS_LIST[] = { -500,-250,-100,-50,-20,-10,0,10,20,50,100,250,500};

    public final static int CONTRAST_LIST[] = { -500,-250,-100,-50,-20,-10,0,10,20,50,100,250,500};


    public static int getSquareDev(IChromosome chroma) {
        return SQUAREDEV_LIST[(Integer) chroma.getGene(0).getAllele()];
    }

    public static int getThreshold(IChromosome chroma) {
        return THRESHOLD_LIST[(Integer) chroma.getGene(1).getAllele()];
    }

    public static double getGap(IChromosome chroma) {
        return GAPS_LIST[(Integer) chroma.getGene(2).getAllele()];
    }

    public static int getCorrections(IChromosome chroma) {
        return CORRECTION_LIST[(Integer) chroma.getGene(3).getAllele()];
    }

    public static double getCellDist(IChromosome chroma) {
        return CELLDIST_LIST[(Integer) chroma.getGene(4).getAllele()];
    }

    public static int getBrightness(IChromosome chroma) {
        return BRIGHTNESS_LIST[(Integer) chroma.getGene(5).getAllele()];
    }

    public static int getContrast(IChromosome chroma) {
        return CONTRAST_LIST[(Integer) chroma.getGene(6).getAllele()];
    }
    
    public static double getAccuracy(IChromosome chroma){
    	return (chroma.getFitnessValue()/96.00)*100.0;
        //double tubesScanned = Math.ceil(Math.log(chroma.getFitnessValue())/Math.log(1.1));
        //return (tubesScanned / (12.0*8.0))*100.0; //12x8 + CSV line
    }

    
    @Override
    protected double evaluate(IChromosome chroma) {
    	return scanlibCount(chroma);
        //return Math.pow(1.1, scanlibCount(chroma));
    }
    
    
    // Returns the amount of tubes successfully scanned
    private static int scanlibCount(IChromosome chroma) {
  
        // Delete scanlib.txt if it exists
        File scanlibFile = new File("scanlib.txt");
        if (scanlibFile.exists()) {
            scanlibFile.delete();
        }

        double gap = getGap(chroma);
        double celldist = getCellDist(chroma);
        
        int squareDev = getSquareDev(chroma);
        int threshold = getThreshold(chroma);
        int corrections = getCorrections(chroma);
        
        if(chroma.getGenes().length == 7){ //TWAIN
            
            int brightness = getBrightness(chroma);
            int contrast = getContrast(chroma);
            
            //TWAIN, dpi = 600, plate = 1
			int tubesscanned = ScannerConfigPlugin.getTestTubesScanned(
            		1, 600, brightness, contrast,
            		0, threshold, gap, squareDev, 
            		corrections,celldist);
			
			System.out.println("TWAIN Tubes Scanned: " + tubesscanned);
			
			return tubesscanned;
        }
        else{ //WIA
        	int retcode = ScanLib.getInstance().slDecodeImage(
        			0, 1, "calibration.bmp", gap, squareDev, threshold, corrections, celldist);
        	
        	if(retcode != ScanLib.SC_SUCCESS){
        	    return 0;
        	}
        	
        	int tubesscanned =  countTubesScanned();
        	System.out.println("WIA Tubes Scanned: " + tubesscanned + " Ret: " + retcode);
			return tubesscanned;
        }
        
    }
    
    
    private static int countTubesScanned(){
        File scanlibFile = new File("scanlib.txt");
        Scanner fileInput = null;
        int tubesScanned = 0;
        
        if (scanlibFile.exists()) {
            try {
                fileInput = new Scanner(
                            new BufferedReader(
                                new FileReader("scanlib.txt")));
            } catch (IOException e) {
            }
            while (fileInput.hasNextLine()) {
                tubesScanned++;
                fileInput.nextLine();
            }
        }
        if(tubesScanned > 0) //CSV line
        	--tubesScanned;
        
        return tubesScanned;
    }

}