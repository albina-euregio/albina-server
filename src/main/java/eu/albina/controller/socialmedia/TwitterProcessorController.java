package eu.albina.controller.socialmedia;

import eu.albina.exception.AlbinaException;
import eu.albina.model.socialmedia.Shipment;
import eu.albina.model.socialmedia.TwitterConfig;
import twitter4j.*;
import twitter4j.Query;
import twitter4j.conf.ConfigurationBuilder;

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

    //TODO: load a configuration from db here!!!
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

    public Status createTweet(TwitterConfig config, String language, String tweet, Long previousId) throws TwitterException, AlbinaException {

        Twitter twitter= getTwitterContext(config);
        try {
            if (previousId!=null){
                twitter.destroyStatus(previousId);
            }
        }
        catch (TwitterException e) {
            String aa="";
        }
        Status response = twitter.updateStatus(tweet);
        ShipmentController.getInstance().saveShipment(createActivityRow(config,language,tweet,response.getSource(),""+response.getId()));
        return response;
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
        .name("name???")
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
