package eu.albina.caaml;

/**
 * The version of CAAML (Canadian Avalanche Association Markup Language)
 * @apiNote http://caaml.org/
 */
public enum CaamlVersion {

	V6, V6_JSON;

	public interface Str {
		String V6 = "V6";
		String V6_JSON = "V6_JSON";
	}

	String namespace() {
		return "http://caaml.org/Schemas/V6.0/Profiles/BulletinEAWS";
	}

}
