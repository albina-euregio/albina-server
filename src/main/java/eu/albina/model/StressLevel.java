/*******************************************************************************
 * Copyright (C) 2021 albina
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.model;

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

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

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
