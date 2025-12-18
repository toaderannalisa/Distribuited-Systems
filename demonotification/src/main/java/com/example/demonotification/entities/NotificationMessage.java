package com.example.demonotification;

public class NotificationMessage {
    private String deviceId;
    private String message;
    private String personId;

    public NotificationMessage() {}
    public NotificationMessage(String deviceId, String message, String personId) {
        this.deviceId = deviceId;
        this.message = message;
        this.personId = personId;
    }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }
}
