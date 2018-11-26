package eu.albina.model.rapidmail.mailings;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class PostMailingsRequestDestination {

	@JsonProperty("action")
	private String action;

	@JsonProperty("id")
	private Integer id;

	@JsonProperty("type")
	private String type;

	public void setAction(String action){
		this.action = action;
	}

	public String getAction(){
		return action;
	}

	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return id;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	@Override
 	public String toString(){
		return 
			"DestinationsItem{" + 
			"action = '" + action + '\'' + 
			",id = '" + id + '\'' + 
			",type = '" + type + '\'' + 
			"}";
		}


	public PostMailingsRequestDestination action(String action) {
		this.action = action;
		return this;
	}

	public PostMailingsRequestDestination id(Integer id) {
		this.id = id;
		return this;
	}

	public PostMailingsRequestDestination type(String type) {
		this.type = type;
		return this;
	}
}