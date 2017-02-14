package org.avalanches.albina.exception;

import org.avalanches.albina.model.AvalancheInformationObject;
import org.json.JSONObject;

public class AvalancheInformationSystemException extends Exception implements AvalancheInformationObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3770001034138464030L;

	public AvalancheInformationSystemException(String message) {
		super(message);
	}

	@Override
	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		result.put("message", this.getMessage());
		return result;
	}

}
