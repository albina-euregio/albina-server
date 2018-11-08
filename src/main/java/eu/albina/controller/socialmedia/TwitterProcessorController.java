package eu.albina.controller.socialmedia;

import com.fasterxml.jackson.databind.ObjectMapper;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

public class TwitterProcessorController {
    private ObjectMapper jacksonObjectMapper;

    Twitter twitter;

    //TODO: load a configuration from db here!!!
    @PostConstruct
    public void init() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("your consumer key")
                .setOAuthConsumerSecret("your consumer secret")
                .setOAuthAccessToken("your access token")
                .setOAuthAccessTokenSecret("your access token secret");
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
    }

    public String createTweet(String tweet) throws TwitterException {
        Status status = twitter.updateStatus(tweet);
        return status.getText();
    }

    public List<String> getTimeLine() throws TwitterException {
        return twitter.getHomeTimeline().stream()
                .map(Status::getText)
                .collect(Collectors.toList());
    }

    public String sendDirectMessage(String recipientName, String msg)
            throws TwitterException {
        DirectMessage message = twitter.sendDirectMessage(recipientName, msg);
        return message.getText();
    }

    public List<String> searchtweets(String queryText) throws TwitterException {
        Query query = new Query("source:" + queryText);
        QueryResult result = twitter.search(query);
        return result.getTweets().stream()
                .map(item -> item.getText())
                .collect(Collectors.toList());
    }
}
