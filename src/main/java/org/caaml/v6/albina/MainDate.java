package org.caaml.v6.albina;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.caaml.v6.CustomData;

import java.time.LocalDate;

public class MainDate implements CustomData {
	private String type;
	@JsonSerialize(using = ToStringSerializer.class)
	private LocalDate mainDate;

	public MainDate() {
	}

	public MainDate(LocalDate mainDate) {
		this.type = "mainDate";
		this.mainDate = mainDate;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	public LocalDate getMainDate() {
		return mainDate;
	}

	public void setMainDate(LocalDate mainDate) {
		this.mainDate = mainDate;
	}
}
