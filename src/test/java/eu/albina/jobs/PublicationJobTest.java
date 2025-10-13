// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import eu.albina.util.AlbinaUtil;

class PublicationJobTest {

    @Test
    public void testPublicationJob() {
        PublicationJob job = new PublicationJob(null);
        Clock clock = Clock.fixed(Instant.parse("2022-03-26T16:01:02Z"), AlbinaUtil.localZone());
        assertEquals(job.getStartDate(clock), Instant.parse("2022-03-26T16:00:00Z")); // ok
        assertEquals(job.getEndDate(clock), Instant.parse("2022-03-27T15:00:00Z")); // ok (time change)
    }

    @Test
    public void testUpdateJob() {
        UpdateJob job = new UpdateJob(null);
        Clock clock = Clock.fixed(Instant.parse("2022-03-27T06:01:02Z"), AlbinaUtil.localZone());
        assertEquals(job.getStartDate(clock), Instant.parse("2022-03-26T16:00:00Z")); // ok
        assertEquals(job.getEndDate(clock), Instant.parse("2022-03-27T15:00:00Z")); // ok (time change)
    }

}
