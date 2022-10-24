package eu.albina.model.enumerations;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class BulletinStatusTest {
	@Test
	public void testCompare() {
		final List<BulletinStatus> expected = Arrays.asList(BulletinStatus.test, BulletinStatus.missing, BulletinStatus.draft, BulletinStatus.updated, BulletinStatus.submitted, BulletinStatus.resubmitted, BulletinStatus.published, BulletinStatus.republished);
		for (int i = 0; i < 10; i++) {
			final List<BulletinStatus> statusList = Arrays.asList(BulletinStatus.values());
			Collections.shuffle(statusList);
			statusList.sort(BulletinStatus::comparePublicationStatus);
			assertEquals(expected, statusList);
		}

	}
}
