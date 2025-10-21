// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

public enum Role {
	SUPERADMIN, ADMIN, FORECASTER, FOREMAN, OBSERVER;

	public interface Str {
		String SUPERADMIN = "SUPERADMIN";
		String ADMIN = "ADMIN";
		String FORECASTER = "FORECASTER";
		String FOREMAN = "FOREMAN";
		String OBSERVER = "OBSERVER";
	}
}
