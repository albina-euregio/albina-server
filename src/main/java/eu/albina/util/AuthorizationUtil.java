package eu.albina.util;

import eu.albina.model.enumerations.Role;

public class AuthorizationUtil {

	public static boolean hasPermissionForRegion(Role role, String region) {
		switch (role) {

		case ADMIN:
			return true;

		case TRENTINO:
			if (region.startsWith("IT-32-TN"))
				return true;
			else
				return false;

		case TYROL:
			if (region.startsWith("AT-07"))
				return true;
			else
				return false;

		case SOUTH_TYROL:
			if (region.startsWith("IT-32-BZ"))
				return true;
			else
				return false;

		case EVTZ:
			return false;

		case VIENNA:
			return false;

		default:
			return false;
		}
	}

	public static String getRegion(Role role) {
		switch (role) {

		case ADMIN:
			return "IT-32-TN";

		case TRENTINO:
			return "IT-32-TN";

		case TYROL:
			return "AT-07";

		case SOUTH_TYROL:
			return "IT-32-BZ";

		case EVTZ:
			return null;

		case VIENNA:
			return null;

		default:
			return null;
		}
	}
}
