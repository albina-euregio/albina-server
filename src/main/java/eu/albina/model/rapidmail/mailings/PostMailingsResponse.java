package eu.albina.model.rapidmail.mailings;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class PostMailingsResponse {

	@JsonProperty("paused")
	private String paused;

	@JsonProperty("from_email")
	private String fromEmail;

	@JsonProperty("attachments")
	private String attachments;

	@JsonProperty("subject")
	private String subject;

	@JsonProperty("created")
	private String created;

	@JsonProperty("destinations")
	private List<PostMailingsRequestDestination> destinations;

	@JsonProperty("send_at")
	private String sendAt;

	@JsonProperty("feature_mailingsplit")
	private String featureMailingsplit;

	@JsonProperty("from_name")
	private String fromName;

	@JsonProperty("title")
	private String title;

	@JsonProperty("sent")
	private String sent;

	@JsonProperty("tracking_domain")
	private String trackingDomain;

	@JsonProperty("canceled")
	private String canceled;

	@JsonProperty("check_robinson")
	private String checkRobinson;

	@JsonProperty("tracking_param")
	private String trackingParam;

	@JsonProperty("deleted")
	private String deleted;

	@JsonProperty("size")
	private String size;

	@JsonProperty("check_ecg")
	private String checkEcg;

	@JsonProperty("recipients")
	private String recipients;

	@JsonProperty("host")
	private String host;

	@JsonProperty("id")
	private String id;

	@JsonProperty("updated")
	private String updated;

	@JsonProperty("status")
	private String status;

	@JsonProperty("_links")
	private RapidMailMailingsResponseLinks links;



	public void setPaused(String paused){
		this.paused = paused;
	}

	public String getPaused(){
		return paused;
	}

	public void setFromEmail(String fromEmail){
		this.fromEmail = fromEmail;
	}

	public String getFromEmail(){
		return fromEmail;
	}

	public void setAttachments(String attachments){
		this.attachments = attachments;
	}

	public String getAttachments(){
		return attachments;
	}

	public void setSubject(String subject){
		this.subject = subject;
	}

	public String getSubject(){
		return subject;
	}

	public void setCreated(String created){
		this.created = created;
	}

	public String getCreated(){
		return created;
	}

	public void setDestinations(List<PostMailingsRequestDestination> destinations){
		this.destinations = destinations;
	}

	public List<PostMailingsRequestDestination> getDestinations(){
		return destinations;
	}

	public void setSendAt(String sendAt){
		this.sendAt = sendAt;
	}

	public String getSendAt(){
		return sendAt;
	}

	public void setFeatureMailingsplit(String featureMailingsplit){
		this.featureMailingsplit = featureMailingsplit;
	}

	public String getFeatureMailingsplit(){
		return featureMailingsplit;
	}

	public void setFromName(String fromName){
		this.fromName = fromName;
	}

	public String getFromName(){
		return fromName;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}

	public void setSent(String sent){
		this.sent = sent;
	}

	public String getSent(){
		return sent;
	}

	public void setTrackingDomain(String trackingDomain){
		this.trackingDomain = trackingDomain;
	}

	public String getTrackingDomain(){
		return trackingDomain;
	}

	public void setCanceled(String canceled){
		this.canceled = canceled;
	}

	public String getCanceled(){
		return canceled;
	}

	public void setCheckRobinson(String checkRobinson){
		this.checkRobinson = checkRobinson;
	}

	public String getCheckRobinson(){
		return checkRobinson;
	}

	public void setTrackingParam(String trackingParam){
		this.trackingParam = trackingParam;
	}

	public String getTrackingParam(){
		return trackingParam;
	}

	public void setDeleted(String deleted){
		this.deleted = deleted;
	}

	public String getDeleted(){
		return deleted;
	}

	public void setSize(String size){
		this.size = size;
	}

	public String getSize(){
		return size;
	}

	public void setCheckEcg(String checkEcg){
		this.checkEcg = checkEcg;
	}

	public String getCheckEcg(){
		return checkEcg;
	}

	public void setRecipients(String recipients){
		this.recipients = recipients;
	}

	public String getRecipients(){
		return recipients;
	}

	public void setHost(String host){
		this.host = host;
	}

	public String getHost(){
		return host;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setUpdated(String updated){
		this.updated = updated;
	}

	public String getUpdated(){
		return updated;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}
	public RapidMailMailingsResponseLinks getLinks() {
		return links;
	}

	public void setLinks(RapidMailMailingsResponseLinks links) {
		this.links = links;
	}
	@Override
 	public String toString(){
		return 
			"MailingPostResponse{" + 
			"paused = '" + paused + '\'' + 
			",from_email = '" + fromEmail + '\'' + 
			",attachments = '" + attachments + '\'' +
			",subject = '" + subject + '\'' + 
			",created = '" + created + '\'' + 
			",destinations = '" + destinations + '\'' + 
			",send_at = '" + sendAt + '\'' + 
			",feature_mailingsplit = '" + featureMailingsplit + '\'' +
			",from_name = '" + fromName + '\'' + 
			",title = '" + title + '\'' + 
			",sent = '" + sent + '\'' + 
			",tracking_domain = '" + trackingDomain + '\'' + 
			",canceled = '" + canceled + '\'' + 
			",check_robinson = '" + checkRobinson + '\'' + 
			",tracking_param = '" + trackingParam + '\'' + 
			",deleted = '" + deleted + '\'' + 
			",size = '" + size + '\'' + 
			",check_ecg = '" + checkEcg + '\'' + 
			",recipients = '" + recipients + '\'' + 
			",host = '" + host + '\'' + 
			",id = '" + id + '\'' + 
			",updated = '" + updated + '\'' + 
			",status = '" + status + '\'' +
			",_links= '" + links + '\'' +
			"}";
		}
}