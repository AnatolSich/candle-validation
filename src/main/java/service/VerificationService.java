package service;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class VerificationService {

    private final CloudStorageClient cloudStorageClient;


    public VerificationService(CloudStorageClient cloudStorageClient) {
        this.cloudStorageClient = cloudStorageClient;
    }
/*
    public void verify() throws IOException {
        if (bodCollection == null) {
            return;
        }
        List<String> files = cloudStorageClient.getVerificationObjects();
        for (String fileName : files) {
            verifyOneFile(fileName);
        }
    }

    private void verifyOneFile(String fileName) throws IOException {
        boolean flag = true;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(cloudStorageClient.downloadObjectData(fileName)))) {
            long count = 0L;
            for (String line; (line = reader.readLine()) != null; ) {
                count++;
                if (line.isBlank()) {
                    continue;
                }
                Candle candle;
                try {
                    candle = logService.parseLineToCandle(line);
                } catch (Exception e) {
                    log.error("File: " + fileName + " CORRUPTED, line: " + count);
                    flag = false;
                    break;
                }
                Optional<Document> documentOpt = findBodByExchangeAndToken(candle.getExchange(), candle.getToken());
                if (documentOpt.isEmpty()) {
                    log.error("File: " + fileName + " CORRUPTED, line: " + count);
                    flag = false;
                    break;
                }
                if (!verifyOneCandle(candle, documentOpt.get())) {
                    log.error("File: " + fileName + " CORRUPTED, line: " + count);
                    corruptedCase(candle, documentOpt.get());
                    flag = false;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            log.error("File: " + fileName + " IS NOT DOWNLOADABLE");
            flag = false;
        }
        if (flag) {
            log.info("File: " + fileName + " DOWNLOADABLE AND CORRECT");
        }
    }

    private Optional<Document> findBodByExchangeAndToken(String exchange, int token) {
        return bodCollection.parallelStream()
                .filter(document ->
                        exchange.equalsIgnoreCase(document.getString("e")) && token == document.getInteger("token")
                ).findFirst();
    }

    private boolean verifyOneCandle(Candle candle, Document document) {
        double lower_circuit = document.getDouble("lower_circuit");
        double upper_circuit = document.getDouble("upper_circuit");
        double open = candle.getOpen();
        double low = candle.getLow();
        double high = candle.getHigh();
        double close = candle.getClose();

        boolean openAboveLower = (open >= lower_circuit);
        boolean openUnderUpper = (open <= upper_circuit);
        boolean lowAboveLower = (low >= lower_circuit);
        boolean lowUnderUpper = (low <= upper_circuit);
        boolean highAboveLower = (high >= lower_circuit);
        boolean highUnderUpper = (high <= upper_circuit);
        boolean closeAboveLower = (close >= lower_circuit);
        boolean closeUnderUpper = (close <= upper_circuit);

        return ((openAboveLower && openUnderUpper) &&
                (lowAboveLower && lowUnderUpper) &&
                (highAboveLower && highUnderUpper) &&
                (closeAboveLower && closeUnderUpper)
        );

    }

    private void corruptedCase(Candle candle, Document document) {
        double lower_circuit = document.getDouble("lower_circuit");
        double upper_circuit = document.getDouble("upper_circuit");
        double open = candle.getOpen();
        double low = candle.getLow();
        double high = candle.getHigh();
        double close = candle.getClose();
        log.error("(Additional info) token: " + candle.getToken());
        log.error("(Additional info) exchange: " + candle.getExchange());
        log.error("(Additional info) lower_circuit: " + lower_circuit);
        log.error("(Additional info) upper_circuit: " + upper_circuit);
        log.error("(Additional info) open: " + open);
        log.error("(Additional info) low: " + low);
        log.error("(Additional info) high: " + high);
        log.error("(Additional info) close: " + close);
    }
*/
}
