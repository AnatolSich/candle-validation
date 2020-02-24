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

    public void sendMessageToSlack(String message) {
        //  StringBuilder messageBuider = new StringBuilder();
        //  messageBuider.append("*My message*: " + message + NEW_LINE);
        //  messageBuider.append("*My message*: " + message + NEW_LINE);
        //  messageBuider.append("*Item example:* " + exampleMessage() + NEW_LINE);

        process(message);
    }

    private void process(String message) {
        Payload payload = Payload.builder()
                .text(message)
                .build();
        try {
            WebhookResponse webhookResponse = Slack.getInstance().send(urlSlackWebHook, payload);
            log.info("code -> " + webhookResponse.getCode());
            log.info("body -> " + webhookResponse.getBody());
        } catch (IOException e) {
            log.error("Unexpected Error! WebHook:" + urlSlackWebHook);
        }
    }

    private String exampleMessage() {
        return "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                + "Aliquam eu odio est. Donec viverra hendrerit lacus et tempor.";
    }
}

