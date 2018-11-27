package eu.albina.model.messengerpeople;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@Generated("com.robohorse.robopojogenerator")
public class MessengerPeopleUserStats {

	@JsonProperty("timestamp")
	private String timestamp;

	@JsonProperty("day")
	private String day;


    @JsonProperty("active")
    private Integer active;

    @JsonProperty("started")
    private Integer started;

    @JsonProperty("stopped")
    private Integer stopped;

    @JsonProperty("deleted")
    private Integer deleted;

    @JsonProperty("WA_active")
    private Integer WA_active;

    @JsonProperty("WA_started")
    private Integer WA_started;

    @JsonProperty("WA_stopped")
    private Integer WA_stopped;

    @JsonProperty("TG_active")
    private Integer TG_active;

    @JsonProperty("TG_started")
    private Integer TG_started;

    @JsonProperty("TG_stopped")
    private Integer TG_stopped;

    public void setActive(Integer active) {
        this.active = active;
    }

    public Integer getActive() {
        return active;
    }

    public void setStarted(Integer started) {
        this.started = started;
    }

    public Integer getStarted() {
        return started;
    }

    public void setStopped(Integer stopped) {
        this.stopped = stopped;
    }

    public Integer getStopped() {
        return stopped;
    }

    public void setWA_active(Integer WA_active) {
        this.WA_active = WA_active;
    }

    public Integer getWA_active() {
        return WA_active;
    }

    public void setWA_started(Integer WA_started) {
        this.WA_started = WA_started;
    }

    public Integer getWA_started() {
        return WA_started;
    }

    public void setWA_stopped(Integer WA_stopped) {
        this.WA_stopped = WA_stopped;
    }

    public Integer getWA_stopped() {
        return WA_stopped;
    }

    public void setTG_active(Integer TG_active) {
        this.TG_active = TG_active;
    }

    public Integer getTG_active() {
        return TG_active;
    }

    public void setTG_started(Integer TG_started) {
        this.TG_started = TG_started;
    }

    public Integer getTG_started() {
        return TG_started;
    }

    public void setTG_stopped(Integer TG_stopped) {
        this.TG_stopped = TG_stopped;
    }

    public Integer getTG_stopped() {
        return TG_stopped;
    }

	public void setTimestamp(String timestamp){
		this.timestamp= timestamp;
	}

	public String getTimestamp(){
		return timestamp;
	}

	public void setDay(String day){
		this.day= day;
	}

	public String getDay(){
		return day;
	}


    @Override
    public String toString() {
        return
                "Response{" +
                        "timestamp = '" + timestamp + '\'' +
                        ",day = '" + day + '\'' +
                        ",user ={" +
                        "active = '" + active + '\'' +
                        ",started = '" + started + '\'' +
                        ",stopped = '" + stopped + '\'' +
                        ",deleted = '" + deleted + '\'' +
                        ",WA_active = '" + WA_active + '\'' +
                        ",WA_started = '" + WA_started + '\'' +
                        ",WA_stopped = '" + WA_stopped + '\'' +
                        ",TG_active = '" + TG_active + '\'' +
                        ",TG_started = '" + TG_started + '\'' +
                        ",TG_stopped = '" + TG_stopped + '\'' +
                        "}" +
                        "}";
    }
}

/*
			"timestamp": "1543273200",
			"day": "2018-11-27",
			"user": {
				"active": "8",
				"started": "0",
				"stopped": "0",
				"deleted": "0",
				"WA_active": 6,
				"WA_started": 0,
				"WA_stopped": 0,
				"TG_active": 2,
				"TG_started": 0,
				"TG_stopped": 0
	}
	*/