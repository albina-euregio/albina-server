// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication;

import java.io.Serializable;
import java.util.Objects;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "push_configurations")
@Serdeable
public class PushConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "VAPID_PUBLIC_KEY", length = 191)
	private String vapidPublicKey;

	@Column(name = "VAPID_PRIVATE_KEY", length = 191)
	private String vapidPrivateKey;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVapidPublicKey() {
		return vapidPublicKey;
	}

	public PushConfiguration vapidPublicKey(String vapidPublicKey) {
		this.vapidPublicKey = vapidPublicKey;
		return this;
	}

	public void setVapidPublicKey(String vapidPublicKey) {
		this.vapidPublicKey = vapidPublicKey;
	}

	public String getVapidPrivateKey() {
		return vapidPrivateKey;
	}

	public PushConfiguration vapidPrivateKey(String vapidPrivateKey) {
		this.vapidPrivateKey = vapidPrivateKey;
		return this;
	}

	public void setVapidPrivateKey(String vapidPrivateKey) {
		this.vapidPrivateKey = vapidPrivateKey;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PushConfiguration pushConfig = (PushConfiguration) o;
		if (pushConfig.getId() == null || getId() == null) {
			return false;
		}
		return Objects.equals(getId(), pushConfig.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return "PushConfig{" + "id=" + getId() + "}";
	}
}
