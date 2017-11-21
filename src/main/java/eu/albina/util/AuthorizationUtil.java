package eu.albina.util;

import eu.albina.model.enumerations.Role;

public class AuthorizationUtil {

	public static boolean hasPermissionForRegion(Role role, String region) {
		switch (role) {

		case ADMIN:
			return true;

		case TRENTINO:
			if (region.startsWith(GlobalVariables.codeTrentino))
				return true;
			else
				return false;

		case TYROL:
			if (region.startsWith(GlobalVariables.codeTyrol))
				return true;
			else
				return false;

		case SOUTH_TYROL:
			if (region.startsWith(GlobalVariables.codeSouthTyrol))
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
			return GlobalVariables.codeTrentino;

		case TRENTINO:
			return GlobalVariables.codeTrentino;

		case TYROL:
			return GlobalVariables.codeTyrol;

		case SOUTH_TYROL:
			return GlobalVariables.codeSouthTyrol;

		case EVTZ:
			return null;

		case VIENNA:
			return null;

		default:
			return null;
		}
	}
}
