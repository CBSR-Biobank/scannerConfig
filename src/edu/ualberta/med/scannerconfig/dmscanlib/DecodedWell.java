package edu.ualberta.med.scannerconfig.dmscanlib;

public class DecodedWell extends Well {

    String message;

    public DecodedWell(String label, double left, double top, double right,
        double bottom) {
        super(label, left, top, right, bottom);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
