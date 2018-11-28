package eu.albina.controller.socialmedia;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.albina.model.rapidmail.mailings.PostMailingsRequest;
import eu.albina.model.rapidmail.mailings.PostMailingsRequestDestination;
import eu.albina.model.rapidmail.mailings.PostMailingsResponse;
import eu.albina.model.rapidmail.recipientlist.RapidMailRecipientListResponse;
import eu.albina.model.rapidmail.recipientlist.RapidMailRecipientListResponseItem;
import eu.albina.model.rapidmail.recipients.post.PostRecipientsRequest;
import eu.albina.model.socialmedia.RapidMailConfig;
import eu.albina.model.socialmedia.Shipment;

public class RapidMailProcessorController extends CommonProcessor {
	private static final int RAPIDMAIL_SOCKET_TIMEOUT = 10000;
	private static final int RAPIDMAIL_CONNECTION_TIMEOUT = 10000;
	private static RapidMailProcessorController instance = null;
	private String baseUrl = "https://apiv3.emailsys.net";
	private ObjectMapper objectMapper = new ObjectMapper();
	private Executor executor;

	public static RapidMailProcessorController getInstance() throws CertificateException, NoSuchAlgorithmException,
			KeyStoreException, IOException, KeyManagementException {
		if (instance == null) {
			instance = new RapidMailProcessorController();
		}
		return instance;
	}

	public RapidMailProcessorController() {
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
		objectMapper.registerModule(new JtsModule());
		executor = Executor.newInstance(sslHttpClient());
	}

	public CloseableHttpClient sslHttpClient() {
		// Trust own CA and all self-signed certs
		SSLContext sslcontext = null;
		// try {
		// File f= new
		// File(this.getClass().getResource("/certificates/emailsys.jks").getFile());
		// sslcontext = SSLContexts.custom()
		// .loadTrustMaterial(f, "".toCharArray(), new TrustSelfSignedStrategy())
		// .build();
		// // Allow TLSv1 protocol only
		// SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
		// sslcontext,
		// new String[] { "TLSv1" },
		// null,
		// SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		return HttpClients.custom()
				// .setSSLSocketFactory(sslsf)
				.build();
		// } catch (NoSuchAlgorithmException | KeyManagementException |
		// KeyStoreException | CertificateException | IOException e) {
		// e.printStackTrace();
		// return null;
		// }
	}

	private String calcBasicAuth(String user, String pass) {
		try {
			return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public HttpResponse getRecipientsList(RapidMailConfig config, String regionId) throws IOException {
		HttpResponse response = executor.execute(Request.Get(baseUrl + "/recipientlists")
				.addHeader("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
				.addHeader("Accept", "application/hal+json").connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT)
				.socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT)).returnResponse();
		if (regionId != null && response.getStatusLine().getStatusCode() == 200) {
			RapidMailRecipientListResponse recipientListResponse = objectMapper.readValue(getResponseContent(response),
					RapidMailRecipientListResponse.class);
			List<RapidMailRecipientListResponseItem> list = recipientListResponse.getEmbedded().getRecipientlists();
			recipientListResponse.getEmbedded().setRecipientlists(
					list.stream().filter(x -> x.getName().startsWith(regionId + "_")).collect(Collectors.toList()));
			BasicHttpResponse newResp = new BasicHttpResponse(response.getStatusLine());
			newResp.setEntity(new StringEntity(toJson(recipientListResponse)));
			newResp.setHeaders(response.getAllHeaders());
			response = newResp;
		}
		return response;
	}

	public HttpResponse getRecipients(RapidMailConfig config, String recipientListId) throws IOException {
		HttpResponse response = executor
				.execute(Request.Get(baseUrl + "/recipients?recipientlist_id=" + recipientListId)
						.addHeader("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
						.addHeader("Accept", "application/json").connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT)
						.socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT))
				.returnResponse();
		return response;
	}

	public HttpResponse createRecipient(RapidMailConfig config, PostRecipientsRequest recipient,
			String sendActivationmail, String language) throws Exception {
		if (recipient.getRecipientlistId() == null) {
			Integer recipientListId = resolveRecipientListIdByName(config, language);
			recipient.setRecipientlistId(recipientListId);
		}
		String url = baseUrl + "/recipients";
		if (sendActivationmail == null)
			sendActivationmail = "yes";
		url += "?send_activationmail=" + sendActivationmail;
		HttpResponse response = executor
				.execute(Request.Post(url)
						.addHeader("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
						.addHeader("Accept", "application/hal+json").addHeader("Content-Type", "application/json")
						.bodyString(toJson(recipient), ContentType.APPLICATION_JSON)
						.connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT).socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT))
				.returnResponse();
		return response;
	}

	public HttpResponse deleteRecipient(RapidMailConfig config, Integer recipientId) throws IOException {
		HttpResponse response = executor.execute(Request.Delete(baseUrl + "/recipients/" + recipientId)
				.addHeader("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
				.addHeader("Accept", "application/json").connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT)
				.socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT)).returnResponse();
		return response;
	}

	public HttpResponse sendMessage(RapidMailConfig config, String language, PostMailingsRequest mailingsPost)
			throws Exception {
		// Set destination to right get i.e. IT-32-TN_IT. Resolve id by name via api
		// If destinations already specified be transparent (do nothing)
		if (mailingsPost.getDestinations() == null) {
			int recipientListId = resolveRecipientListIdByName(config, language);
			mailingsPost.setDestinations(Collections.singletonList(
					new PostMailingsRequestDestination().id(recipientListId).type("recipientlist").action("include")));
		}

		if (mailingsPost.getSendAt() == null) {
			mailingsPost.setSendAt(OffsetDateTime.now().toString());
		}
		if (mailingsPost.getStatus() == null) {
			mailingsPost.setStatus("scheduled");
		}

		HttpResponse response = executor
				.execute(Request.Post(baseUrl + "/mailings")
						.addHeader("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
						.addHeader("Content-Type", "application/json").addHeader("Accept", "application/hal+json")
						.bodyString(toJson(mailingsPost), ContentType.APPLICATION_JSON)
						.connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT).socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT))
				.returnResponse();
		// Go ahead only if success
		if (response.getStatusLine().getStatusCode() != 201) {
			String body = response.getEntity()!=null?IOUtils.toString(response.getEntity().getContent(), "UTF-8"):null;
			ShipmentController.getInstance().saveShipment(createActivityRow(config, language, toJson(mailingsPost), body, null));
			return response;
		}
		String body = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		response.getEntity().getContent().reset();
		PostMailingsResponse bodyObject = objectMapper.readValue(body, PostMailingsResponse.class);
		ShipmentController.getInstance().saveShipment(createActivityRow(config, language, toJson(mailingsPost), body, "" + bodyObject.getId()));
		return response;
	}

	private int resolveRecipientListIdByName(RapidMailConfig config, String language) throws Exception {
		// Set destination to right get i.e. IT-32-TN_IT. Resolve id by name via api
		String recipientName = config.getRegionConfiguration().getRegion().getId() + "_" + language.toUpperCase();
		HttpResponse resp = getRecipientsList(config, null);
		RapidMailRecipientListResponse recipientListResponse = objectMapper.readValue(getResponseContent(resp),
				RapidMailRecipientListResponse.class);
		Integer recipientId = recipientListResponse.getEmbedded().getRecipientlists().stream()
				.filter(x -> StringUtils.equalsIgnoreCase(x.getName(), recipientName))
				.map(RapidMailRecipientListResponseItem::getId).findFirst().orElseThrow(() -> new Exception(
						"Invalid recipientList name '" + recipientName + "'. Please check configuration"));
		return recipientId;
	}

	private Shipment createActivityRow(RapidMailConfig config, String language, String request, String response,
			String idRm) {
		return new Shipment().date(ZonedDateTime.now()).name(config.getRegionConfiguration().getRegion().getNameEn())
				.language(language).idMp(null).idRm(idRm).idTw(null).request(request).response(response)
				.region(config.getRegionConfiguration()).provider(config.getProvider());
	}

	private String getResponseContent(HttpResponse response) throws IOException {
		return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
	}

}
