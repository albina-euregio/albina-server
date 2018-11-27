package eu.albina.controller.socialmedia;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.albina.exception.AlbinaException;
import eu.albina.model.messengerpeople.*;
import eu.albina.model.socialmedia.MessengerPeopleConfig;
import eu.albina.model.socialmedia.Shipment;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MessengerPeopleProcessorController extends CommonProcessor {
    private static MessengerPeopleProcessorController instance = null;

    public static MessengerPeopleProcessorController getInstance() {
        if (instance == null) {
            instance = new MessengerPeopleProcessorController();
        }
        return instance;
    }

//    private static String apikey="a1e6d5387c979b039040447af4a4d20a_11513_9fc5a49fc674b5b2750ad90a7";
    private final String baseUrl="https://rest.messengerpeople.com/api/v1";
    ObjectMapper objectMapper = new ObjectMapper();
    int MESSENGER_PEOPLE_CONNECTION_TIMEOUT=10000;
    int MESSENGER_PEOPLE_SOCKET_TIMEOUT=10000;



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
    public List<MessengerPeopleUser> getUsers(MessengerPeopleConfig config, Integer limit, Integer offset) throws IOException {
        String json=Request.Get(baseUrl+"/user"+
                String.format("?apikey=%s&limit=%s&offset=%s",config.getApiKey(),limit,offset))
                .connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT)
                .socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
                .execute().returnContent().asString();
        List<MessengerPeopleUser> users = objectMapper.readValue(json, new TypeReference<List<MessengerPeopleUser>>(){});
        return users;
    }

    public void setUserDetails(MessengerPeopleConfig config, String id, MessengerPeopleUserData messengerPeopleUserData) throws IOException {
        String json=URLEncoder.encode(objectMapper.writeValueAsString(messengerPeopleUserData), StandardCharsets.UTF_8.toString());
        Request.Put(baseUrl+"/user"+
                String.format("?apikey=%s&fields=%s",config.getApiKey(),json))
                .connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT)
                .socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
                .execute();
    }

    /**
     * Do not use this cause doesn't return array. Or use a map from jackson
     * @return
     */
    public MessengerPeopleTargets getTargets(MessengerPeopleConfig config) throws IOException {
        String json=Request.Get(baseUrl+"/newsletter/targeting"+
                String.format("?apikey=%s",config.getApiKey()))
                .connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT)
                .socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
                .execute().returnContent().asString();
        MessengerPeopleTargets targets = objectMapper.readValue(json, MessengerPeopleTargets.class);
        return targets;
    }

    public MessengerPeopleNewsLetter sendNewsLetter(MessengerPeopleConfig config, String language, String message, String attachmentUrl) throws IOException, AlbinaException {
        Integer categoryId=null;
        if (StringUtils.equalsIgnoreCase(language,"EN")){
            categoryId=1;
        }
        else if (StringUtils.equalsIgnoreCase(language,"DE")){
            categoryId=2;
        }
        else if (StringUtils.equalsIgnoreCase(language,"IT")){
            categoryId=3;
        }
        String params=String.format("apikey=%s&message=%s&category=%s",config.getApiKey(),message=URLEncoder.encode(message, "UTF-8"),categoryId);
        if (attachmentUrl!=null){
            params+="&attachment="+URLEncoder.encode(attachmentUrl, "UTF-8");
        }
        String json=Request.Post(baseUrl+"/newsletter?"+params)
                .connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT)
                .socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
                .execute()
                .returnContent().asString();
        MessengerPeopleNewsLetter response = objectMapper.readValue(json, MessengerPeopleNewsLetter.class);
        ShipmentController.getInstance().saveShipment(createActivityRow(config,language,"message="+message+", attachmentUrl="+attachmentUrl,toJson(response),""+response.getBroadcastId()));
        return response;
    }

    public MessengerPeopleNewsletterHistory getNewsLetterHistory(MessengerPeopleConfig config, Integer limit) throws IOException {
        String json=Request.Get(baseUrl+"/newsletter"+
                String.format("?apikey=%s&limit=%d",config.getApiKey(),limit))
                .connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT)
                .socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
                .execute().returnContent().asString();
        MessengerPeopleNewsletterHistory newsletterHistory = objectMapper.readValue(json, MessengerPeopleNewsletterHistory.class);
        return newsletterHistory;
    }

    public HttpResponse getUsersStats(MessengerPeopleConfig config) throws IOException {
        HttpResponse response=Request.Get(baseUrl+"/stats/user"+
                String.format("?apikey=%s",config.getApiKey()) + "&days=1&hours=0&start=" + Instant.now().minus(1,ChronoUnit.DAYS).getEpochSecond() + "&end=")
                .connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT)
                .socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
                .execute().returnResponse();
        return response;
    }


    private Shipment createActivityRow(MessengerPeopleConfig config, String language, String request, String response, String idMp){
        Shipment shipment=new Shipment()
                .date(ZonedDateTime.now())
                .name(config.getRegionConfiguration().getRegion().getNameEn())
                .language(language)
                .idMp(idMp)
                .idRm(null)
                .idTw(null)
                .request(request)
                .response(response)
                .region(config.getRegionConfiguration())
                .provider(config.getProvider());
        return shipment;
    }
}
