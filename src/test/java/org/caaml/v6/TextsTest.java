// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TextsTest {

	@Test
	public void lines() {
		Assertions.assertEquals(List.of("A", "B", "C", "D"), Texts.lines("A<br/>B <br> \nC&lt;br/&gt;D"));
		Assertions.assertEquals(List.of("A"), Texts.lines("A"));
		Assertions.assertEquals(List.of(), Texts.lines("<br/>"));
		Assertions.assertEquals(List.of(), Texts.lines(null));
	}
}
