package eu.albina.model;

import org.json.JSONObject;

public interface AvalancheInformationObject {

	/**
	 * This method serializes the object to JSON.
	 * 
	 * @return Returns a string in JSON format representing the object.
	 */
	public abstract JSONObject toJSON();

}