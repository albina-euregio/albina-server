package eu.albina.model.messengerpeople;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

@Generated("com.robohorse.robopojogenerator")
public class MessengerPeopleTargetsList {

	private Map<String, String> items = new HashMap<>();

	@JsonAnyGetter
	public Map<String, String> any() {
		return items;
	}

	@JsonAnySetter
	public void any(String name, String value) {
		this.items.put(name, value);
	}

	@Override
	public String toString() {
		return "List{" + "items = '" + items + '\'' + "}";
	}
}
