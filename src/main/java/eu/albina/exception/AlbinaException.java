// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.exception;

import java.util.Map;

/**
 * Custom exception for the ALBINA project.
 *
 * @author Norbert Lanzanasto
 *
 */
public class AlbinaException extends Exception {

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
	public Map<String, String> toJSON() {
		return Map.of("message", this.getMessage());
	}

}
