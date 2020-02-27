package service;


import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import exceptions.ValidationException;
import lombok.extern.apachecommons.CommonsLog;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

@CommonsLog
public class ParseCsvService {


    private final Properties appProps;
    private String invalidNumberMessage;
    private String wrongOrderMessage;
    private String blankRecordMessage;
    private String fileName;
    private String fileExtension;
    private String token;

    public ParseCsvService(Properties appProps, String fileName, String fileExtension, String token) {
        this.appProps = appProps;
        this.invalidNumberMessage = appProps.getProperty("candle-validation.slack.invalid_number_message");
        this.wrongOrderMessage = appProps.getProperty("candle-validation.slack.wrong_order_message");
        this.blankRecordMessage = appProps.getProperty("candle-validation.slack.blank_record_message");
        this.fileName = fileName;
        this.token = token;
        this.fileExtension = fileExtension;
    }

    public List<String[]> getRecords(File file) throws IOException, CsvException {
        FileReader filereader = new FileReader(file);
        CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
        CSVReader csvReader = new CSVReaderBuilder(filereader)
                .withCSVParser(parser)
                .build();
        return csvReader.readAll();
    }

    public boolean checkSize(List<String[]> list) throws ValidationException {
        String numberStr = appProps.getProperty("candle-validation.count_number_to_validate");
        int number = 375;
        if (numberStr != null) {
            number = Integer.parseInt(numberStr);
        }
        log.info("Required records count = " + number);
        if (list.size() != number) {
            throw new ValidationException(String.format(invalidNumberMessage, String.valueOf(list.size()), fileName + fileExtension, String.valueOf(number)));
        } else {
            return true;
        }
    }

    public boolean checkDescending(List<String[]> list) throws Exception {
        if (list == null) {
            throw new Exception("There are no data to check descending");
        }
        for (int i = 0; i < list.size() - 1; i++) {
            int j = i + 1;
            String currStr = list.get(i)[0];
            String nextStr = list.get(j)[0];
            if (currStr == null || currStr.isBlank()) {
                log.info("Record number " + i + " is absent");
                throw new ValidationException(String.format(blankRecordMessage, fileName + fileExtension, token));
            }
            if (nextStr == null || nextStr.isBlank()) {
                log.info("Record number " + j + " is absent");
                throw new ValidationException(String.format(blankRecordMessage, fileName + fileExtension, token));
            }
            long curr = Long.parseLong(currStr);
            long next = Long.parseLong(nextStr);
            if (curr >= next) {
                throw new ValidationException(String.format(wrongOrderMessage, fileName + fileExtension));
            }
        }
        return true;
    }

}
