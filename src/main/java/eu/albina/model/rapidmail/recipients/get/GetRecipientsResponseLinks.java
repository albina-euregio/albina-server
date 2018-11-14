package eu.albina.model.rapidmail.recipients.get;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class GetRecipientsResponseLinks {

	@JsonProperty("self")
	private GetRecipientsResponseSelf self;

	public void setSelf(GetRecipientsResponseSelf self){
		this.self = self;
	}

	public GetRecipientsResponseSelf getSelf(){
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