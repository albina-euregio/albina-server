package eu.albina.model.messengerpeople;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

@Generated("com.robohorse.robopojogenerator")
public class MessengerPeopleNewsletterHistory {

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
	public String toString() {
		return "MessengerPeopleNewsletterHistory{" + "items = '" + items + '\'' + "}";
	}
}