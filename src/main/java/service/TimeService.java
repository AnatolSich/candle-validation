package service;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.log4j.Logger;
import utilities.TimestampFileAppender;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

@CommonsLog
public class TimeService {

    private static final DateTimeFormatter FORMATTER_WITH_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH.mm.ss");
    private static final DateTimeFormatter FORMATTER_ONLY_DAY = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Properties appProps;
    private final ZoneId zoneId;
    private final List<DayOfWeek> weekEndDays;
    private final TimestampFileAppender timestampFileAppender;

    public TimeService(Properties appProps) {
        this.appProps = appProps;
        this.timestampFileAppender = (TimestampFileAppender) Logger.getRootLogger().getAppender("rollingFile");
        this.zoneId = ZoneId.of(getTimeZone());
        this.weekEndDays = getPropsWeekEndDays();
    }

    private String getTimeZone() {
        String timeZone = timestampFileAppender.getTimeZone();
        if (timeZone == null || timeZone.isBlank()) {
            return "UTC+05:30";
        } else return timeZone.trim();
    }

    public String getLocalDateTimeInMillis() throws Exception {
        LocalDateTime fileNameDate = getCheckDay();
        String result = String.valueOf(fileNameDate.atZone(ZoneId.of("UTC+00:00")).toInstant().toEpochMilli());
        log.info("FileNameDate in millis = " + result);
        return result;
    }

    private LocalDateTime getCheckDay() throws Exception {
        String checkDateStr = appProps.getProperty("candle-validation.checkingDate");
        log.info("CheckDate from props = " + checkDateStr);
        if (checkDateStr != null && !checkDateStr.isBlank()) {
            return LocalDate.parse(checkDateStr, FORMATTER_ONLY_DAY).atStartOfDay();
        }
        LocalDate localDate = LocalDate.now(zoneId);
        log.info("CurrentDate = " + localDate.format(FORMATTER_ONLY_DAY));
        LocalDateTime currentDate = LocalDate.now(zoneId).atStartOfDay();
        LocalDateTime fileNameDate = currentDate.minusDays(1L);

        int count = 1;
        while (isWeekEnd(fileNameDate)) {
            fileNameDate = fileNameDate.minusDays(1L);
            count++;
            if (count >= 7) {
                throw new Exception("Can't define date for file name");
            }
        }
        log.info("FileNameDate = " + fileNameDate.format(FORMATTER_WITH_TIME));
        return fileNameDate;
    }

    private boolean isWeekEnd(LocalDateTime date) {
        DayOfWeek checkedDay = date.getDayOfWeek();
        return weekEndDays.contains(checkedDay);
    }

    private List<DayOfWeek> getPropsWeekEndDays() {
        String prop = appProps.getProperty("candle-validation.weekEnd");
        List<DayOfWeek> result = new ArrayList<>();
        log.info("WeekEndDays from props: " + prop);
        if (prop == null || prop.isBlank()) {
            result.add(DayOfWeek.SATURDAY);
            result.add(DayOfWeek.SUNDAY);
            log.info("Result WeekEndDays: " + result);
            return result;
        }
        String[] splitProp = prop.split(",");

        for (DayOfWeek day : DayOfWeek.values()) {
            for (String str : splitProp
            ) {
                str = str.trim();
                if (str.equalsIgnoreCase(day.getDisplayName(TextStyle.SHORT, Locale.US))) {
                    result.add(day);
                }
            }
        }
        Collections.sort(result);
        log.info("Result WeekEndDays: " + result);
        return result;
    }

    public String getLogFileName() {
        return this.timestampFileAppender.getFileName();
    }

}
