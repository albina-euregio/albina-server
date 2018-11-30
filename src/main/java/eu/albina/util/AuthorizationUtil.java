package eu.albina.util;

import eu.albina.model.User;

public class AuthorizationUtil {

	public static boolean hasPermissionForRegion(User user, String region) {
		if (user.getRegions().contains(region))
			return true;
		else
			return false;
	}
}
