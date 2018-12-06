package eu.albina.controller.socialmedia;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.albina.exception.AlbinaException;
import eu.albina.model.socialmedia.Shipment;
import eu.albina.model.socialmedia.TwitterConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import twitter4j.*;
import twitter4j.Query;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TwitterProcessorController extends CommonProcessor {
    private static TwitterProcessorController instance = null;
    public static TwitterProcessorController getInstance() {
        if (instance == null) {
            instance = new TwitterProcessorController();
        }
        return instance;
    }

    private Twitter getTwitterContext(TwitterConfig config) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(config.getConsumerKey())
                .setOAuthConsumerSecret(config.getConsumerSecret())
                .setOAuthAccessToken(config.getAccessKey())
                .setOAuthAccessTokenSecret(config.getAccessSecret());
        TwitterFactory tf = new TwitterFactory(cb.build());

//        https://apps.twitter.com/app/8335738/show
//        https://twitter.com/DevClesius
        return tf.getInstance();
    }

    public BasicHttpResponse createTweet(TwitterConfig config, String language, String tweet, Long previousId) throws AlbinaException, IOException, IllegalAccessException {
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        Twitter twitter= getTwitterContext(config);
        try {
            if (previousId!=null){
                twitter.destroyStatus(previousId);
            }
            Status response = twitter.updateStatus(tweet);
            StatusLine statusLine = new BasicStatusLine(protocolVersion, 200, "OK");
            BasicHttpResponse respHttp=new BasicHttpResponse(statusLine);
            String respJson=toJson(response);
            respHttp.setEntity(new StringEntity(respJson));
            respHttp.setHeaders(new BasicHeader[]{
                    new BasicHeader("","")}
            );
            ShipmentController.getInstance().saveShipment(createActivityRow(config,language,tweet,respJson,""+response.getId()));
            return respHttp;
        }
        catch (TwitterException e) {
            twitter4j.HttpResponse respTwHttp= (HttpResponse) FieldUtils.readField(e, "response", true);
            StatusLine statusLine = new BasicStatusLine(protocolVersion, respTwHttp.getStatusCode(), "OK");
            String respJson = (String) FieldUtils.readField(respTwHttp, "responseAsString", true);
            BasicHttpResponse respHttp=new BasicHttpResponse(statusLine);
            respHttp.setEntity(new StringEntity(respJson));
            respHttp.setHeaders(new BasicHeader[]{
                    new BasicHeader("","")}
            );
            ShipmentController.getInstance().saveShipment(createActivityRow(config,language,tweet,respJson,null));
            return respHttp;
        }
    }

    public List<String> getTimeLine(TwitterConfig config) throws TwitterException {
        Twitter twitter= getTwitterContext(config);
        return twitter.getHomeTimeline().stream()
                .map(Status::getText)
                .collect(Collectors.toList());
    }

    public String sendDirectMessage(TwitterConfig config, String recipientName, String msg)
            throws TwitterException {
        Twitter twitter= getTwitterContext(config);
        DirectMessage message = twitter.sendDirectMessage(recipientName, msg);
        return message.getText();
    }

    public List<String> searchtweets(TwitterConfig config, String queryText) throws TwitterException {
        Twitter twitter= getTwitterContext(config);
        Query query = new Query("source:" + queryText);
        QueryResult result = twitter.search(query);
        return result.getTweets().stream()
                .map(item -> item.getText())
                .collect(Collectors.toList());
    }

    private Shipment createActivityRow(TwitterConfig config, String language, String request, String response, String idTw){
        Shipment shipment=new Shipment()
        .date(ZonedDateTime.now())
        .name(config.getRegionConfiguration().getRegion().getNameEn())
        .language(language)
        .idMp(null)
        .idRm(null)
        .idTw(idTw)
        .request(request)
        .response(response)
        .region(config.getRegionConfiguration())
        .provider(config.getProvider());
        return shipment;
    }
}