package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
public abstract class AbstractPersistentObject implements PersistentObject {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;

	public String getId() {
		return id;
	}

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