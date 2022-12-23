package eu.albina.model.enumerations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BulletinStatusTest {
	@Test
	public void testCompare() {
		final List<BulletinStatus> expected = Arrays.asList(BulletinStatus.test, BulletinStatus.missing, BulletinStatus.draft, BulletinStatus.updated, BulletinStatus.submitted, BulletinStatus.resubmitted, BulletinStatus.published, BulletinStatus.republished);
		for (int i = 0; i < 10; i++) {
			final List<BulletinStatus> statusList = Arrays.asList(BulletinStatus.values());
			Collections.shuffle(statusList);
			statusList.sort(BulletinStatus::comparePublicationStatus);
			Assertions.assertEquals(expected, statusList);
		}

	}
}
