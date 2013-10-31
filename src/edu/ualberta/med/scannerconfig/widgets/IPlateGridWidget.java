package edu.ualberta.med.scannerconfig.widgets;

import org.apache.commons.lang3.tuple.Pair;

import edu.ualberta.med.scannerconfig.PlateOrientation;

public interface IPlateGridWidget extends IScanRegionWidget {

    public PlateOrientation getOrientation();

    public Pair<Integer, Integer> getPlateDimensions();

}
