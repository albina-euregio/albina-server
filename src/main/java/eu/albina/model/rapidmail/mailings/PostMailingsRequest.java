package eu.albina.model.rapidmail.mailings;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class PostMailingsRequest {

	@JsonProperty("check_robinson")
	private String checkRobinson;

	@JsonProperty("from_email")
	private String fromEmail;

	@JsonProperty("attachments")
	private String attachments;

	@JsonProperty("file")
	private PostMailingsRequestPostFile file;

	@JsonProperty("check_ecg")
	private String checkEcg;

	@JsonProperty("subject")
	private String subject;

	@JsonProperty("destinations")
	private List<PostMailingsRequestDestination> destinations;

	@JsonProperty("host")
	private String host;

	@JsonProperty("send_at")
	private String sendAt;

	@JsonProperty("feature_mailingsplit")
	private String featureMailingsplit;

	@JsonProperty("from_name")
	private String fromName;

	@JsonProperty("status")
	private String status;

	public void setCheckRobinson(String checkRobinson){
		this.checkRobinson = checkRobinson;
	}

	public String getCheckRobinson(){
		return checkRobinson;
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

	public void setFile(PostMailingsRequestPostFile file){
		this.file = file;
	}

	public PostMailingsRequestPostFile getFile(){
		return file;
	}

	public void setCheckEcg(String checkEcg){
		this.checkEcg = checkEcg;
	}

	public String getCheckEcg(){
		return checkEcg;
	}

	public void setSubject(String subject){
		this.subject = subject;
	}

	public String getSubject(){
		return subject;
	}

	public void setDestinations(List<PostMailingsRequestDestination> destinations){
		this.destinations = destinations;
	}

	public List<PostMailingsRequestDestination> getDestinations(){
		return destinations;
	}

	public void setHost(String host){
		this.host = host;
	}

	public String getHost(){
		return host;
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

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}

	@Override
 	public String toString(){
		return 
			"SendMail{" + 
			"check_robinson = '" + checkRobinson + '\'' + 
			",from_email = '" + fromEmail + '\'' + 
			",attachments = '" + attachments + '\'' + 
			",file = '" + file + '\'' + 
			",check_ecg = '" + checkEcg + '\'' + 
			",subject = '" + subject + '\'' + 
			",destinations = '" + destinations + '\'' + 
			",host = '" + host + '\'' + 
			",send_at = '" + sendAt + '\'' + 
			",feature_mailingsplit = '" + featureMailingsplit + '\'' + 
			",from_name = '" + fromName + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}


	public PostMailingsRequest checkRobinson(String checkRobinson) {
		this.checkRobinson = checkRobinson;
		return this;
	}

	public PostMailingsRequest fromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
		return this;
	}

	public PostMailingsRequest attachments(String attachments) {
		this.attachments = attachments;
		return this;
	}

	public PostMailingsRequest file(PostMailingsRequestPostFile file) {
		this.file = file;
		return this;
	}

	public PostMailingsRequest checkEcg(String checkEcg) {
		this.checkEcg = checkEcg;
		return this;
	}

	public PostMailingsRequest subject(String subject) {
		this.subject = subject;
		return this;
	}

	public PostMailingsRequest destinations(List<PostMailingsRequestDestination> destinations) {
		this.destinations = destinations;
		return this;
	}

	public PostMailingsRequest host(String host) {
		this.host = host;
		return this;
	}

	public PostMailingsRequest sendAt(String sendAt) {
		this.sendAt = sendAt;
		return this;
	}

	public PostMailingsRequest featureMailingsplit(String featureMailingsplit) {
		this.featureMailingsplit = featureMailingsplit;
		return this;
	}

	public PostMailingsRequest fromName(String fromName) {
		this.fromName = fromName;
		return this;
	}

	public PostMailingsRequest status(String status) {
		this.status = status;
		return this;
	}
}