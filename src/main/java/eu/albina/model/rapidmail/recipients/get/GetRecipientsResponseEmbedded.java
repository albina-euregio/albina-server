package eu.albina.model.rapidmail.recipients.get;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class GetRecipientsResponseEmbedded {

	@JsonProperty("recipients")
	private List<GetRecipientsResponseItem> recipients;

	public void setRecipients(List<GetRecipientsResponseItem> recipients){
		this.recipients = recipients;
	}

	public List<GetRecipientsResponseItem> getRecipients(){
		return recipients;
	}

	@Override
 	public String toString(){
		return 
			"Embedded{" + 
			"recipients = '" + recipients + '\'' + 
			"}";
		}
}