package eu.albina.model.rapidmail.recipientlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class RapidMailRecipientListResponseLinks {

	@JsonProperty("self")
	private RapidMailRecipientListResponseSelf self;

	public void setSelf(RapidMailRecipientListResponseSelf self){
		this.self = self;
	}

	public RapidMailRecipientListResponseSelf getSelf(){
		return self;
	}

	@Override
 	public String toString(){
		return 
			"Links{" + 
			"self = '" + self + '\'' + 
			"}";
		}
}