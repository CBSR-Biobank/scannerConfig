package edu.ualberta.med.scannerconfig.dmscanlib;

public class DecodedWell {

    final String label;
    final String message;

    public DecodedWell(String label, String message) {
        this.label = label;
        this.message = message;
    }

    public String getLabel() {
        return label;
    }

    public String getMessage() {
        return message;
    }
}
