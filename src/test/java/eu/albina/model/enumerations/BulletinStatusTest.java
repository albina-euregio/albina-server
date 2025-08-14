// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BulletinStatusTest {
	@Test
	public void testCompare() {
		final List<BulletinStatus> expected = Arrays.asList(BulletinStatus.missing, BulletinStatus.draft, BulletinStatus.updated, BulletinStatus.submitted, BulletinStatus.resubmitted, BulletinStatus.published, BulletinStatus.republished);
		for (int i = 0; i < 10; i++) {
			final List<BulletinStatus> statusList = Arrays.asList(BulletinStatus.values());
			Collections.shuffle(statusList);
			statusList.sort(BulletinStatus::comparePublicationStatus);
			Assertions.assertEquals(expected, statusList);
		}

	}
}
