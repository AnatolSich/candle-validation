package service;

import org.apache.log4j.Logger;
import utilities.TimestampFileAppender;

public class LogService {

    public String getLogFileName() {
        TimestampFileAppender timestampFileAppender = (TimestampFileAppender) Logger.getRootLogger().getAppender("rollingFile");
        return timestampFileAppender.getFileName();
    }
}
