package utilities;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.log4j.RollingFileAppender;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimestampFileAppender extends RollingFileAppender {

    private static final String TARGET = "\\{timestamp}";

    @Getter
    @Setter
    private String timestampPattern = null;
    @Getter
    @Setter
    private String timeZone = null;
    private String fileName = null;


    public String getFileName() {
        return fileName;
    }

    private String createFileName(String file) {
        System.out.println("timeZone = " + timeZone);
        String fileName = file.replaceAll(TARGET, LocalDateTime.now(ZoneId.of(timeZone)).format(DateTimeFormatter.ofPattern(timestampPattern)));
        this.fileName = fileName;
        return fileName;
    }

    public void setFile(String file) {
        if (timestampPattern != null && timeZone != null) {
            super.setFile(createFileName(file));
        } else {
            super.setFile(file);
        }
    }

    public void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
        if (timestampPattern != null && timeZone != null) {
            super.setFile(createFileName(fileName), append, bufferedIO, bufferSize);
        } else {
            super.setFile(fileName, append, bufferedIO, bufferSize);
        }
    }
}
