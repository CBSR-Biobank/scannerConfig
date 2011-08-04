package edu.ualberta.med.scannerconfig.dmscanlib;

public class ScanLibResult {

    private int resultCode;

    private int value; // used by API call to return its value (if any)

    private String message;

    public ScanLibResult(int resultCode, int value, String message) {
        this.resultCode = resultCode;
        this.setValue(value);
        this.message = message;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
