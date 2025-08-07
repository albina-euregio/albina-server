// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.exception;

import com.github.openjson.JSONObject;

import eu.albina.model.AvalancheInformationObject;

/**
 * Custom exception for the ALBINA project.
 *
 * @author Norbert Lanzanasto
 *
 */
public class AlbinaException extends Exception implements AvalancheInformationObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 3770001034138464030L;

	/**
	 * Constructor with a custom {@code message} text.
	 *
	 * @param message
	 *            the message text for the exception.
	 */
	public AlbinaException(String message) {
		super(message);
	}

	/**
	 * Return a {@code JSONObject} representing the exception (only containing the
	 * message).
	 *
	 * @return a {@code JSONObject} representing the exception (only containing the
	 *         message)
	 */
	@Override
	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		result.put("message", this.getMessage());
		return result;
	}

}
