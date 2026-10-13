// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.util.Locale;

/**
 * Password strength check following NIST SP 800-63B-4 (2025) and OWASP ASVS 5.0 (V6.2): length is
 * what matters, composition rules (upper/lower/digit/symbol) are explicitly discouraged.
 */
public enum PasswordStrength {
	VERY_WEAK, WEAK, STRONG;

	/** Absolute minimum length (OWASP ASVS 6.2.1). */
	public static final int MIN_LENGTH = 8;

	/** NIST SP 800-63B-4 minimum for passwords used as a single authentication factor. */
	public static final int RECOMMENDED_LENGTH = 15;

	public static PasswordStrength of(String password, String username) {
		int length = password == null ? 0 : password.codePointCount(0, password.length());
		if (length < MIN_LENGTH) {
			return VERY_WEAK;
		}
		if (length < RECOMMENDED_LENGTH) {
			return WEAK;
		}
		if (password.codePoints().distinct().count() <= 2) {
			return WEAK; // e.g. "aaaaaaaaaaaaaaa" or "abababababababab"
		}
		// context-specific words (ASVS 6.2.11): password must not contain the username
		String lowerPassword = password.toLowerCase(Locale.ROOT);
		String localPart = username == null ? "" : username.toLowerCase(Locale.ROOT).replaceFirst("@.*", "");
		if (localPart.length() >= 3 && lowerPassword.contains(localPart)) {
			return WEAK;
		}
		return STRONG;
	}
}
