package eu.albina.util;

import java.util.List;

import eu.albina.model.enumerations.Role;

public class AuthorizationUtil {

	public static boolean hasPermissionForRegion(List<Role> roles, String region) {
		for (Role role : roles) {
			switch (role) {
			case TRENTINO:
				if (region.startsWith(GlobalVariables.codeTrentino))
					return true;
				else
					break;
			case TYROL:
				if (region.startsWith(GlobalVariables.codeTyrol))
					return true;
				else
					break;
			case SOUTH_TYROL:
				if (region.startsWith(GlobalVariables.codeSouthTyrol))
					return true;
				else
					break;
			default:
				break;
			}
		}
		return false;
	}

	public static String getRegion(List<Role> roles) {
		for (Role role : roles) {
			switch (role) {
			case TRENTINO:
				return GlobalVariables.codeTrentino;
			case TYROL:
				return GlobalVariables.codeTyrol;
			case SOUTH_TYROL:
				return GlobalVariables.codeSouthTyrol;
			default:
				break;
			}
		}
		return null;
	}
}
