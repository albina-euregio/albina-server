package eu.albina.model.rapidmail.recipientlist;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class RapidMailRecipientListResponseEmbedded {

	@JsonProperty("recipientlists")
	private List<RapidMailRecipientListResponseItem> recipientlists;

	public void setRecipientlists(List<RapidMailRecipientListResponseItem> recipientlists){
		this.recipientlists = recipientlists;
	}

	public List<RapidMailRecipientListResponseItem> getRecipientlists(){
		return recipientlists;
	}

	@Override
 	public String toString(){
		return 
			"Embedded{" + 
			"recipientlists = '" + recipientlists + '\'' + 
			"}";
		}
}