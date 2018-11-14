package eu.albina.controller.socialmedia;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.albina.exception.AlbinaException;
import eu.albina.model.rapidmail.mailings.PostMailingsRequest;
import eu.albina.model.rapidmail.mailings.PostMailingsRequestDestination;
import eu.albina.model.rapidmail.mailings.PostMailingsResponse;
import eu.albina.model.rapidmail.recipientlist.RapidMailRecipientListResponse;
import eu.albina.model.rapidmail.recipientlist.RapidMailRecipientListResponseItem;
import eu.albina.model.rapidmail.recipients.post.PostRecipientsRequest;
import eu.albina.model.rapidmail.recipients.get.GetRecipientsResponse;
import eu.albina.model.rapidmail.recipients.post.PostRecipientsResponse;
import eu.albina.model.socialmedia.RapidMailConfig;
import eu.albina.model.socialmedia.Shipment;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Collections;

public class RapidMailProcessorController extends CommonProcessor {
    private static final int RAPIDMAIL_SOCKET_TIMEOUT = 1000;
    private static final int RAPIDMAIL_CONNECTION_TIMEOUT = 1000;
    private static RapidMailProcessorController instance = null;
    private String baseUrl="https://apiv3.emailsys.net";
    private ObjectMapper objectMapper=new ObjectMapper();

    public static RapidMailProcessorController getInstance() {
        if (instance == null) {
            instance = new RapidMailProcessorController();
        }
        return instance;
    }

    public RapidMailProcessorController(){
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        objectMapper.registerModule(new JtsModule());
    }

    private String calcBasicAuth(String user, String pass) {
        try {
            return "Basic "+Base64.getEncoder().encodeToString((user+":"+pass).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public RapidMailRecipientListResponse getRecipientsList(RapidMailConfig config) throws IOException {
        String json=Request.Get(baseUrl+"/recipientlists")
                .addHeader("Authorization", calcBasicAuth(config.getUsername(),config.getPassword()))
                .connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT)
                .socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT)
                .execute()
                .returnContent().asString();
        RapidMailRecipientListResponse targets = objectMapper.readValue(json, RapidMailRecipientListResponse.class);
        return targets;
    }

    public GetRecipientsResponse getRecipients(RapidMailConfig config, String recipientListId) throws IOException {
        String json=Request.Get(baseUrl+"/recipients?recipientlist_id="+recipientListId)
                .addHeader("Authorization", calcBasicAuth(config.getUsername(),config.getPassword()))
                .connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT)
                .socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT)
                .execute().returnContent().asString();
        GetRecipientsResponse targets = objectMapper.readValue(json, GetRecipientsResponse.class);
        return targets;
    }

    public PostRecipientsResponse createRecipient(RapidMailConfig config, PostRecipientsRequest recipient) throws IOException {
        String json=Request.Post(baseUrl+"/recipients")
                .addHeader("Authorization", calcBasicAuth(config.getUsername(),config.getPassword()))
                .bodyString(toJson(recipient), ContentType.APPLICATION_JSON)
                .connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT)
                .socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT)
                .execute().returnContent().asString();
        PostRecipientsResponse targets = objectMapper.readValue(json, PostRecipientsResponse.class);
        return targets;
    }

    public int deleteRecipient(RapidMailConfig config, Integer recipientId) throws IOException {
        int code=Request.Delete(baseUrl+"/recipients/recipientId")
                .addHeader("Authorization", calcBasicAuth(config.getUsername(),config.getPassword()))
                .connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT)
                .socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT)
                .execute().returnResponse().getStatusLine().getStatusCode();
        return code;
    }
    //TODO: Add post put delete ????

    public PostMailingsResponse sendMessage(RapidMailConfig config, String language, PostMailingsRequest mailingsPost) throws IOException, AlbinaException {
        //Set destination to right get i.e. IT-32-TN_IT. Resolve id by name via api
        String recipientName=config.getRegionConfiguration().getRegion().getId()+"_"+language.toUpperCase();
        RapidMailRecipientListResponse recipientListResponse=getRecipientsList(config);
        String recipientId=recipientListResponse.getEmbedded().getRecipientlists()
                .stream()
                .filter(x-> StringUtils.equalsIgnoreCase(x.getName(),recipientName))
                .map(RapidMailRecipientListResponseItem::getId)
                .findFirst()
                .orElse(null);
        mailingsPost.setDestinations(Collections.singletonList(
                new PostMailingsRequestDestination()
                        .id(recipientId)
        ));
        String json= Request.Post(baseUrl+"/apiusers")
                .addHeader("Authorization", calcBasicAuth(config.getUsername(),config.getPassword()))
                .bodyString(toJson(mailingsPost), ContentType.APPLICATION_JSON)
                .connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT)
                .socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT)
                .execute().returnContent().asString();
        PostMailingsResponse response = objectMapper.readValue(json, PostMailingsResponse.class);
        ShipmentController.getInstance().saveShipment(createActivityRow(config,language,toJson(mailingsPost),toJson(response),""+response.getId()));
        return response;
    }

    private Shipment createActivityRow(RapidMailConfig config, String language, String request, String response, String idRm){
        return new Shipment()
                .date(ZonedDateTime.now())
                .name("name???")
                .language(language)
                .idMp(null)
                .idRm(idRm)
                .idTw(null)
                .request(request)
                .response(response)
                .region(config.getRegionConfiguration())
                .provider(config.getProvider());
    }

}
