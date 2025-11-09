// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import eu.albina.controller.RegionRepository;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.util.AlbinaUtil;
import jakarta.annotation.Nullable;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public interface PublicationStrategy {

	static PublicationStrategy publishAt5PM() {
		return new PublicationStrategy() {
			@Override
			public boolean isEnabled(ServerInstance serverInstance) {
				return serverInstance.isPublishAt5PM();
			}

			@Override
			public boolean isChange() {
				return false;
			}

			@Override
			public Instant getStartDate(Clock clock) {
				return ZonedDateTime.of(
					LocalDate.now(clock),
					AlbinaUtil.validityStart(),
					clock.getZone()
				).toInstant();
			}
		};
	}

	static PublicationStrategy updateAt8AM() {
		return new PublicationStrategy() {
			@Override
			public boolean isEnabled(ServerInstance serverInstance) {
				return serverInstance.isPublishAt8AM();
			}

			@Override
			public boolean isChange() {
				return false;
			}

			@Override
			public Instant getStartDate(Clock clock) {
				return ZonedDateTime.of(
					LocalDate.now(clock).minusDays(1),
					AlbinaUtil.validityStart(),
					clock.getZone()
				).toInstant();
			}
		};
	}

	static PublicationStrategy change() {
		return new PublicationStrategy() {
			@Override
			public boolean isEnabled(ServerInstance serverInstance) {
				return true;
			}

			@Override
			public boolean isChange() {
				return true;
			}

			@Override
			public Instant getStartDate(Clock clock) {
				return ZonedDateTime.of(
					LocalDate.now(clock),
					AlbinaUtil.validityStart(),
					clock.getZone()
				).toInstant();
			}
		};
	}

	static PublicationStrategy publishAll(Instant startDate, boolean change) {
		Objects.requireNonNull(startDate, "startDate");
		return new PublicationStrategy() {
			@Override
			public boolean isEnabled(ServerInstance serverInstance) {
				return true;
			}

			@Override
			public boolean isChange() {
				return change;
			}

			@Override
			public Instant getStartDate(Clock clock) {
				return startDate;
			}
		};
	}

	static PublicationStrategy publish(Instant startDate, List<Region> regions) {
		Objects.requireNonNull(startDate, "startDate");
		Objects.requireNonNull(regions, "regions");
		return new PublicationStrategy() {
			@Override
			public boolean isEnabled(ServerInstance serverInstance) {
				return true;
			}

			@Override
			public boolean isChange() {
				return false;
			}

			@Override
			public Instant getStartDate(Clock clock) {
				return startDate;
			}

			@Override
			public List<Region> getRegions() {
				return regions;
			}
		};
	}

	static PublicationStrategy change(Instant startDate, List<Region> regions) {
		Objects.requireNonNull(startDate, "startDate");
		Objects.requireNonNull(regions, "regions");
		return new PublicationStrategy() {
			@Override
			public boolean isEnabled(ServerInstance serverInstance) {
				return true;
			}

			@Override
			public boolean isChange() {
				return true;
			}

			@Override
			public Instant getStartDate(Clock clock) {
				return startDate;
			}

			@Override
			public List<Region> getRegions() {
				return regions;
			}
		};
	}

	boolean isEnabled(ServerInstance serverInstance);

	boolean isChange();

	Instant getStartDate(Clock clock);

	default Instant getEndDate(Clock clock) {
		return getStartDate(clock).atZone(clock.getZone()).plusDays(1).toInstant();
	}

	/**
	 * Defaults to {@link RegionRepository#getPublishBulletinRegions()}
	 */
	@Nullable
	default List<Region> getRegions() {
		return null;
	}
}
