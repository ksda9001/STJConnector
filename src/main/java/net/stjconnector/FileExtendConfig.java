package net.stjconnector;

public class FileExtendConfig {
    private String systemType;
    private String logType;

    public FileExtendConfig(String systemType, String logType) {
        this.systemType = systemType;
        this.logType = logType;
    }

    public String getSystemType() {
        return systemType;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType){
        this.logType = logType;
    }
}
