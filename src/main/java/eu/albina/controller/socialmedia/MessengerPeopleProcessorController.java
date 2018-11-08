package eu.albina.controller.socialmedia;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.albina.model.messengerpeople.*;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessengerPeopleProcessorController {
    private static String apikey="a1e6d5387c979b039040447af4a4d20a_11513_9fc5a49fc674b5b2750ad90a7";
    private final String baseUrl="https://rest.messengerpeople.com/api/v1";
    ObjectMapper objectMapper = new ObjectMapper();
    int MESSENGER_PEOPLE_CONNECTION_TIMEOUT=1000;
    int MESSENGER_PEOPLE_SOCKET_TIMEOUT=1000;

    private static MessengerPeopleProcessorController instance = null;

    public static MessengerPeopleProcessorController getInstance() {
        if (instance == null) {
            instance = new MessengerPeopleProcessorController();
        }
        return instance;
    }

    public MessengerPeopleProcessorController(){
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        objectMapper.registerModule(new JtsModule());
    }

    /**
     *
     * @param limit
     * @param offset
     * @return
     */
    public List<MessengerPeopleUser> getUsers(Integer limit, Integer offset) throws IOException {
        String json=Request.Get(baseUrl+"/user"+
                String.format("?apikey=%s&limit=%s&offset=%s",apikey,limit,offset))
                .connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT)
                .socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
                .execute().returnContent().asString();
        List<MessengerPeopleUser> users = objectMapper.readValue(json, new TypeReference<List<MessengerPeopleUser>>(){});
        return users;
    }

    public void setUserDetails(String id, MessengerPeopleUserData messengerPeopleUserData) throws IOException {
        String json=URLEncoder.encode(objectMapper.writeValueAsString(messengerPeopleUserData), StandardCharsets.UTF_8.toString());
        Request.Put(baseUrl+"/user"+
                String.format("?apikey=%s&fields=%s",apikey,json))
                .connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT)
                .socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
                .execute();
    }

    /**
     * Do not use this cause doesn't return array. Or use a map from jackson
     * @return
     */
    public MessengerPeopleTargets getTargets() throws IOException {
        String json=Request.Get(baseUrl+"/newsletter/targeting"+
                String.format("?apikey=%s",apikey))
                .connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT)
                .socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
                .execute().returnContent().asString();
        MessengerPeopleTargets targets = objectMapper.readValue(json, MessengerPeopleTargets.class);
        return targets;
    }

    public MessengerPeopleNewsLetter sendNewsLetter(String targetId, String message, String attachmentUrl) throws IOException {
        String json=Request.Post(baseUrl+"/newsletter"+
                String.format("?apikey=%s&message=%s&attachment=%s&targeting_id=%s",apikey,message,attachmentUrl,targetId))
                .connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT)
                .socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
                .execute().returnContent().asString();
        MessengerPeopleNewsLetter newsletterResult = objectMapper.readValue(json, MessengerPeopleNewsLetter.class);
        return newsletterResult;
    }

    public MessengerPeopleNewsletterHistory getNewsLetterHistory(Integer limit) throws IOException {
        String json=Request.Get(baseUrl+"/newsletter"+
                String.format("?apikey=%s&limit=%d",apikey,limit))
                .connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT)
                .socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
                .execute().returnContent().asString();
        MessengerPeopleNewsletterHistory newsletterHistory = objectMapper.readValue(json, MessengerPeopleNewsletterHistory.class);
        return newsletterHistory;
    }

    public String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json,clazz);
    }

}
