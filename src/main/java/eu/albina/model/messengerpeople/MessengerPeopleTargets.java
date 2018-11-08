package eu.albina.model.messengerpeople;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class MessengerPeopleTargets{

	@JsonProperty("code")
	private int code;

	@JsonProperty("list")
	private MessengerPeopleTargetsList list;

	public void setCode(int code){
		this.code = code;
	}

	public int getCode(){
		return code;
	}

	public void setList(MessengerPeopleTargetsList list){
		this.list = list;
	}

	public MessengerPeopleTargetsList getList(){
		return list;
	}

	@Override
 	public String toString(){
		return 
			"MessengerPeopleTargets{" + 
			"code = '" + code + '\'' + 
			",list = '" + list + '\'' + 
			"}";
		}
}
