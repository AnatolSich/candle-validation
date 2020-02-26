package service;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;

import java.io.IOException;
import java.util.Properties;

@CommonsLog
@RequiredArgsConstructor
public class WebhookClient {
    private static final String NEW_LINE = "\n";

    private String urlSlackWebHook;


    public WebhookClient(Properties appProps) {
        this.urlSlackWebHook = appProps.getProperty("candle-validation.slack.webhook");
    }

    public void sendMessageToSlack(String message) throws Exception {
        process(message);
    }

    private void process(String message) throws Exception {
        Payload payload = Payload.builder()
                .text(message)
                .build();
        try {
            Slack slack = Slack.getInstance();
            WebhookResponse webhookResponse = slack.send(urlSlackWebHook, payload);
            log.info("Message: \n\"" + message + "\"\nSent to slack." + "\nResponse is : " + webhookResponse.getBody());
            slack.close();
        } catch (IOException e) {
            log.error("Unexpected Error! WebHook:" + urlSlackWebHook);
        }
    }

}

