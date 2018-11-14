package eu.albina.model.rapidmail.mailings;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class PostMailingsRequestPostFile {

	@JsonProperty("type")
	private String type;

	@JsonProperty("content")
	private String content;

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return content;
	}

	@Override
 	public String toString(){
		return 
			"File{" + 
			"type = '" + type + '\'' + 
			",content = '" + content + '\'' + 
			"}";
		}


	public PostMailingsRequestPostFile type(String type) {
		this.type = type;
		return this;
	}

	public PostMailingsRequestPostFile content(String content) {
		this.content = content;
		return this;
	}
}