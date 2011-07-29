package edu.ualberta.med.scannerconfig.dmscanlib;

public class ScanLibResult {

    private int resultCode;

    private String message;

    public ScanLibResult(int resultCode, String message) {
        this.resultCode = resultCode;
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

}
