// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.io.IOException;
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
import com.google.common.base.MoreObjects;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Deserializer;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.Serializer;
import io.micronaut.serde.annotation.Serdeable;
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
@Serdeable
public class StressLevel {

	public static class ToStringSerializer implements Serializer<Object> {
		@Override
		public void serialize(Encoder encoder, EncoderContext context, Argument<?> type, Object value) throws IOException {
			encoder.encodeString(value.toString());
		}
	}

	public static class LocalDateFromStringDeserializer implements Deserializer<LocalDate> {
		@Override
		public @Nullable LocalDate deserialize(Decoder decoder, DecoderContext context, Argument<? super LocalDate> type) throws IOException {
			return LocalDate.parse(decoder.decodeString());
		}
	}

	public static class InstantFromStringDeserializer implements Deserializer<Instant> {
		@Override
		public @Nullable Instant deserialize(Decoder decoder, DecoderContext context, Argument<? super Instant> type) throws IOException {
			return Instant.parse(decoder.decodeString());
		}
	}

	@Id
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "USER_ID", nullable = false, columnDefinition = "varchar(255)")
	@JsonIgnore
	private User user;

	@Id
	@Column(name = "DATE", nullable = false)
	@Serdeable.Serializable(using = ToStringSerializer.class)
	@Serdeable.Deserializable(using = LocalDateFromStringDeserializer.class)
	private LocalDate date;

	@Column(name = "STRESS_LEVEL")
	private Integer stressLevel;

	@Column(name = "LAST_UPDATED", nullable = false)
	@Serdeable.Serializable(using = ToStringSerializer.class)
	@Serdeable.Deserializable(using = InstantFromStringDeserializer.class)
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
