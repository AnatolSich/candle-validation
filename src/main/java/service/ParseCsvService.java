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
    private String fileName;

    public ParseCsvService(Properties appProps, String fileName) {
        this.appProps = appProps;
        this.invalidNumberMessage = appProps.getProperty("candle-validation.slack.invalid_number_message") + " "
                + appProps.getProperty("aws.s3.loaded.bucket.name")
                + appProps.getProperty("aws.s3.loaded.folder.name")
                + "%s"
                + appProps.getProperty("candle-validation.fileExtension");
        this.wrongOrderMessage = appProps.getProperty("candle-validation.slack.wrong_order_message") + " "
                + appProps.getProperty("aws.s3.loaded.bucket.name")
                + appProps.getProperty("aws.s3.loaded.folder.name")
                + "%s"
                + appProps.getProperty("candle-validation.fileExtension");
        this.fileName = fileName;
    }

    public List<String[]> getRecords(File file) throws IOException, CsvException {
        FileReader filereader = new FileReader(file);
        CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
        CSVReader csvReader = new CSVReaderBuilder(filereader)
                .withCSVParser(parser)
                .build();
        List<String[]> allData = csvReader.readAll();

        log.info("size = " + allData.size());
//        for (String[] row : allData) {
//            for (String cell : row) {
//                System.out.print(cell + "\t");
//            }
//            System.out.println();
//        }
        return allData;
    }

    public boolean checkSize(List<String[]> list) throws ValidationException {
        String numberStr = appProps.getProperty("candle-validation.count_number_to_validate");
        int number = 375;
        if (numberStr != null) {
            number = Integer.parseInt(numberStr);
        }
        if (list.size() != number) {
            throw new ValidationException(String.format(invalidNumberMessage, fileName));
        } else {
            return true;
        }
    }

    public boolean checkDescending(List<String[]> list) throws Exception {
        for (int i = 0; i < list.size() - 1; i++) {
            int j = i + 1;
            String currStr = list.get(i)[0];
            String nextStr = list.get(j)[0];
            if (currStr == null || currStr.isBlank() || nextStr == null || nextStr.isBlank()) {
                throw new ValidationException("Corrupted data in file");
            }
            long curr = Long.parseLong(currStr);
            long next = Long.parseLong(nextStr);
            if (curr < next) {
                throw new ValidationException(String.format(wrongOrderMessage, fileName));
            }
        }
        return true;
    }

}
