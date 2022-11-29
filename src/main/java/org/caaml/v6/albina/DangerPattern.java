package org.caaml.v6.albina;

import org.caaml.v6.CustomData;

public class DangerPattern implements CustomData {
	private String type;
	private eu.albina.model.enumerations.DangerPattern id;
	private String name;

	public DangerPattern() {
	}

	public DangerPattern(eu.albina.model.enumerations.DangerPattern id, String name) {
		this.type = "dangerPattern";
		this.id = id;
		this.name = name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	public eu.albina.model.enumerations.DangerPattern getId() {
		return id;
	}

	public void setId(eu.albina.model.enumerations.DangerPattern id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
