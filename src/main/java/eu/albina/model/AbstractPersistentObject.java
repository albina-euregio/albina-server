// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractPersistentObject implements PersistentObject {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "ID", length = 191)
	protected String id;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || !(o instanceof PersistentObject)) {

			return false;
		}

		PersistentObject other = (PersistentObject) o;

		// if the id is missing, return false
		if (id == null)
			return false;

		// equivalence by id
		return id.equals(other.getId());
	}

	public int hashCode() {
		if (id != null) {
			return id.hashCode();
		} else {
			return super.hashCode();
		}
	}

	public String toString() {
		return this.getClass().getName() + "[id=" + id + "]";
	}
}
