/*******************************************************************************
 * Copyright (C) 2022 Norbert Lanzanasto
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
package eu.albina.model.publication;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "push_configurations")
public class PushConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "PROVIDER_ID")
	private PublicationProvider provider;

	@Column(name = "VAPID_PUBLIC_KEY")
	private String vapidPublicKey;

	@Column(name = "VAPID_PRIVATE_KEY")
	private String vapidPrivateKey;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PublicationProvider getProvider() {
		return provider;
	}

	public PushConfiguration provider(PublicationProvider provider) {
		this.provider = provider;
		return this;
	}

	public void setProvider(PublicationProvider provider) {
		this.provider = provider;
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
