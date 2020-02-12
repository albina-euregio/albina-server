package eu.albina.model.enumerations;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class BulletinStatusTest {
	@Test
	public void testCompare() {
		final List<BulletinStatus> expected = Arrays.asList(BulletinStatus.missing, BulletinStatus.draft,
				BulletinStatus.submitted, BulletinStatus.published, BulletinStatus.updated, BulletinStatus.resubmitted,
				BulletinStatus.republished);
		for (int i = 0; i < 10; i++) {
			final List<BulletinStatus> statusList = Arrays.asList(BulletinStatus.values());
			Collections.shuffle(statusList);
			statusList.sort(BulletinStatus::comparePublicationStatus);
			assertEquals(expected, statusList);
		}

	}
}
