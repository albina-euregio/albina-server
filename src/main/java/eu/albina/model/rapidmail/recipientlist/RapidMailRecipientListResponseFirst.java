package eu.albina.model.rapidmail.recipientlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class RapidMailRecipientListResponseFirst {

	@JsonProperty("href")
	private String href;

	public void setHref(String href){
		this.href = href;
	}

	public String getHref(){
		return href;
	}

	@Override
 	public String toString(){
		return 
			"First{" + 
			"href = '" + href + '\'' + 
			"}";
		}
}