// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PasswordStrengthTest {

	private static final String USERNAME = "jane@example.com";

	@Test
	void veryWeak() {
		assertEquals(PasswordStrength.VERY_WEAK, PasswordStrength.of(null, USERNAME));
		assertEquals(PasswordStrength.VERY_WEAK, PasswordStrength.of("", USERNAME));
		assertEquals(PasswordStrength.VERY_WEAK, PasswordStrength.of("S3cret!", USERNAME));
	}

	@Test
	void weak() {
		assertEquals(PasswordStrength.WEAK, PasswordStrength.of("Tr0ub4dor&3!", USERNAME));
		assertEquals(PasswordStrength.WEAK, PasswordStrength.of("aaaaaaaaaaaaaaaaaaaa", USERNAME));
		assertEquals(PasswordStrength.WEAK, PasswordStrength.of("abababababababababab", USERNAME));
		assertEquals(PasswordStrength.WEAK, PasswordStrength.of("my name is JANE and I ski", USERNAME));
	}

	@Test
	void strong() {
		assertEquals(PasswordStrength.STRONG, PasswordStrength.of("correct horse battery staple", USERNAME));
		assertEquals(PasswordStrength.STRONG, PasswordStrength.of("lawinenwarndienst tirol", null));
	}
}
