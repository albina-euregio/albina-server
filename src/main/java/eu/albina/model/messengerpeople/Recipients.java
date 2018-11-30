package eu.albina.model.messengerpeople;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@Generated("com.robohorse.robopojogenerator")
public class Recipients {

	private Map<String, Integer> items = new HashMap<>();

	@JsonAnyGetter
	public Map<String, Integer> any() {
		return items;
	}

	@JsonAnySetter
	public void any(String name, Integer value) {
		this.items.put(name, value);
	}

	@Override
 	public String toString(){
		return 
			"Recipients{" +
			"item = '" + items + '\'' +
			"}";
	}
}