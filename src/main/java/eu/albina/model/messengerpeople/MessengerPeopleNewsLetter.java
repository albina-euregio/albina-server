package eu.albina.model.messengerpeople;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class MessengerPeopleNewsLetter {

	@JsonProperty("code")
	private int code;

	@JsonProperty("broadcast_id")
	private int broadcastId;

	public void setCode(int code){
		this.code = code;
	}

	public int getCode(){
		return code;
	}

	public void setBroadcastId(int broadcastId){
		this.broadcastId = broadcastId;
	}

	public int getBroadcastId(){
		return broadcastId;
	}

	@Override
 	public String toString(){
		return 
			"NewsletterId{" + 
			"code = '" + code + '\'' + 
			",broadcast_id = '" + broadcastId + '\'' + 
			"}";
		}
}
