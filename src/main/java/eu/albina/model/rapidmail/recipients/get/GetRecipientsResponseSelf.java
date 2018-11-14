package eu.albina.model.rapidmail.recipients.get;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class GetRecipientsResponseSelf {

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
			"Self{" + 
			"href = '" + href + '\'' + 
			"}";
		}
}