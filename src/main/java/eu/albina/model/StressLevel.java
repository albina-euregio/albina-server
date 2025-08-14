// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.google.common.base.MoreObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@IdClass(StressLevel.StressLevelID.class)
@Table(name = "user_stress_levels")
public class StressLevel {

	@Id
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "USER_ID", nullable = false, columnDefinition = "varchar(255)")
	@JsonIgnore
	private User user;

	@Id
	@Column(name = "DATE", nullable = false)
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = FromStringDeserializer.class)
	private LocalDate date;

	@Column(name = "STRESS_LEVEL")
	private Integer stressLevel;

	@Column(name = "LAST_UPDATED", nullable = false)
	@JsonSerialize(using = InstantSerializer.class)
	@JsonDeserialize(using = InstantDeserializer.class)
	private Instant lastUpdated;

	public static Map<UUID, List<StressLevel>> randomizeUsers(List<StressLevel> stressLevels) {
		Map<User, UUID> randomization = new TreeMap<>(Comparator.comparing(User::getEmail));
		return stressLevels.stream()
			.collect(Collectors.groupingBy(stressLevel -> randomization.computeIfAbsent(stressLevel.getUser(), i -> UUID.randomUUID())));
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Integer getStressLevel() {
		return stressLevel;
	}

	public void setStressLevel(Integer stressLevel) {
		this.stressLevel = stressLevel;
	}

	public Instant getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Instant lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("user", user)
			.add("date", date)
			.add("stressLevel", stressLevel)
			.add("lastUpdated", lastUpdated)
			.toString();
	}

	public static final class StressLevelID {
		private User user;
		private LocalDate date;

		public StressLevelID() {
		}

		public StressLevelID(User user, LocalDate date) {
			this.user = user;
			this.date = date;
		}

		public LocalDate getDate() {
			return date;
		}

		public void setDate(LocalDate date) {
			this.date = date;
		}

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			StressLevelID that = (StressLevelID) o;
			return Objects.equals(user, that.user) && Objects.equals(date, that.date);
		}

		@Override
		public int hashCode() {
			return Objects.hash(user, date);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
				.add("user", user)
				.add("date", date)
				.toString();
		}
	}

}
