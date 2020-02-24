package service;

import lombok.extern.apachecommons.CommonsLog;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

@CommonsLog
public class TimeService {

    private static final DateTimeFormatter formatterNifty = DateTimeFormatter.ofPattern("dd.MM.yyyy HH.mm.ss");
    private static final DateTimeFormatter formatterCheckDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Properties appProps;
    private final ZoneId zoneId;
    private final List<DayOfWeek> weekEndDays;

    public TimeService(Properties appProps) {
        String timeZone = appProps.getProperty("candle-validation.timeZone");
        this.appProps = appProps;
        this.zoneId = ZoneId.of(timeZone);
        this.weekEndDays = getPropsWeekEndDays();
    }

    public String getLocalDateTimeInMillis() throws Exception {
        LocalDateTime fileNameDate = getCheckDay();
        String trueResult = String.valueOf(fileNameDate.atZone(zoneId).toInstant().toEpochMilli());
        return appProps.getProperty("temp.fileName");
    }

    private LocalDateTime getCheckDay() throws Exception {
        String checkDateStr = appProps.getProperty("candle-validation.checkingDate");
        log.info("checkDateStr = " + checkDateStr);
        if (checkDateStr != null && !checkDateStr.isBlank()) {
            return LocalDate.parse(checkDateStr, formatterCheckDate).atStartOfDay();
        }
        LocalDateTime currentDate = LocalDate.now(zoneId).atStartOfDay();
        log.info("currentDate = " + currentDate.format(formatterNifty));


        LocalDateTime fileNameDate = currentDate.minusDays(1L);

        int count = 1;
        while (isWeekEnd(fileNameDate)) {
            fileNameDate = fileNameDate.minusDays(1L);
            count++;
            if (count >= 7) {
                throw new Exception("Can't define date for file name");
            }
        }
        log.info("fileNameDate = " + fileNameDate.format(formatterNifty));
        return fileNameDate;
    }

    private boolean isWeekEnd(LocalDateTime date) {
        DayOfWeek checkedDay = date.getDayOfWeek();
      //  log.info(checkedDay);
      //  log.info(weekEndDays);
        return weekEndDays.contains(checkedDay);
    }

    private List<DayOfWeek> getPropsWeekEndDays() {
        String prop = appProps.getProperty("candle-validation.weekEnd");
        List<DayOfWeek> result = new ArrayList<>();
        if (prop == null || prop.isBlank()) {
            result.add(DayOfWeek.SATURDAY);
            result.add(DayOfWeek.SUNDAY);
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
        return result;
    }

}
