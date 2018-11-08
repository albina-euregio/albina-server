package eu.albina.model.messengerpeople;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@Generated("com.robohorse.robopojogenerator")
public class MessengerPeopleNewsletterHistory{

	private Map<String, MessengerPeopleNewsletterHistoryItem> items = new HashMap<>();

	@JsonAnyGetter
	public Map<String, MessengerPeopleNewsletterHistoryItem> any() {
		return items;
	}
	@JsonAnySetter
	public void any(String name, MessengerPeopleNewsletterHistoryItem value) {
		this.items.put(name, value);
	}

	@Override
 	public String toString(){
		return 
			"MessengerPeopleNewsletterHistory{" + 
			"items = '" + items + '\'' +
			"}";
		}
}