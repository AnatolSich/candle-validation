package service;


import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

@RequiredArgsConstructor
public class ApacheHttpClient {

    private static final Logger log = Logger.getLogger(ApacheHttpClient.class);


    public String sendGet(String url) throws IOException {
        log.info("Sending GET request");
        log.info(url);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);

        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9");
        request.addHeader(HttpHeaders.CONNECTION, "keep-alive");
        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/78.0.3904.70 Chrome/78.0.3904.70 Safari/537.36");

        CloseableHttpResponse response = httpClient.execute(request);

        log.info("Response:");
        log.info(response.getProtocolVersion().toString());
        log.info(String.valueOf(response.getStatusLine().getStatusCode()));
        log.info(response.getStatusLine().getReasonPhrase());
        log.info(response.getStatusLine().toString());
        String result = null;
        if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        }
        response.close();
        httpClient.close();
        return result;
    }
}