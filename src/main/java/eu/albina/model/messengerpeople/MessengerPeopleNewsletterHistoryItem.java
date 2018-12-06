package eu.albina.model.messengerpeople;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class MessengerPeopleNewsletterHistoryItem{

	@JsonProperty("targeting_list_id")
	private String targetingListId;

	@JsonProperty("targeting_list")
	private String targetingList;

	@JsonProperty("category_name")
	private String categoryName;

	@JsonProperty("created")
	private String created;

	@JsonProperty("recipients")
	private Recipients recipients;

	@JsonProperty("text")
	private String text;

	@JsonProperty("to")
	private String to;

	@JsonProperty("sendtime")
	private String sendtime;

	@JsonProperty("created_by")
	private String createdBy;

	@JsonProperty("status")
	private String status;

	@JsonProperty("media")
	private String media;

	public void setTargetingListId(String targetingListId){
		this.targetingListId = targetingListId;
	}

	public String getTargetingListId(){
		return targetingListId;
	}

	public void setTargetingList(String targetingList){
		this.targetingList = targetingList;
	}

	public String getTargetingList(){
		return targetingList;
	}

	public void setCategoryName(String categoryName){
		this.categoryName = categoryName;
	}

	public String getCategoryName(){
		return categoryName;
	}

	public void setCreated(String created){
		this.created = created;
	}

	public String getCreated(){
		return created;
	}

	public void setRecipients(Recipients recipients){
		this.recipients = recipients;
	}

	public Recipients getRecipients(){
		return recipients;
	}

	public void setText(String text){
		this.text = text;
	}

	public String getText(){
		return text;
	}

	public void setTo(String to){
		this.to = to;
	}

	public String getTo(){
		return to;
	}

	public void setSendtime(String sendtime){
		this.sendtime = sendtime;
	}

	public String getSendtime(){
		return sendtime;
	}

	public void setCreatedBy(String createdBy){
		this.createdBy = createdBy;
	}

	public String getCreatedBy(){
		return createdBy;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}

	public void setMedia(String media){
		this.status = media;
	}

	public String getMedia(){
		return media;
	}


	@Override
 	public String toString(){
		return 
			"MessengerPeopleNewsletterHistoryItem{" +
            "targeting_list_id = '" + targetingListId + '\'' +
            ",targeting_list = '" + targetingList + '\'' +
            "category_name = '" + categoryName + '\'' +
			",created = '" + created + '\'' + 
			",recipients = '" + recipients + '\'' + 
			",text = '" + text + '\'' + 
			",to = '" + to + '\'' + 
			",sendtime = '" + sendtime + '\'' + 
			",created_by = '" + createdBy + '\'' + 
			",status = '" + status + '\'' +
			",media = '" + media + '\'' +
			"}";
		}
}