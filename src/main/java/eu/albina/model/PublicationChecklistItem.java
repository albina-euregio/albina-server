// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "publication_checklist_items", indexes = {
		@Index(name = "publication_checklist_items_CHECKLIST_IDX", columnList = "CHECKLIST_ID"),
})
@Serdeable
public class PublicationChecklistItem extends AbstractPersistentObject {

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CHECKLIST_ID", nullable = false)
	private PublicationChecklist checklist;

	@Column(name = "PUBLICATION_CHANNEL", nullable = false)
	private String publicationChannel;

	@Column(name = "OK_VALUE")
	private Boolean ok;

	@Column(name = "PROBLEM_DESCRIPTION")
	private String problemDescription;

	public PublicationChecklist getChecklist() {
		return checklist;
	}

	public void setChecklist(PublicationChecklist checklist) {
		this.checklist = checklist;
	}

	public String getPublicationChannel() {
		return publicationChannel;
	}

	public void setPublicationChannel(String publicationChannel) {
		this.publicationChannel = publicationChannel;
	}

	public Boolean getOk() {
		return ok;
	}

	public void setOk(Boolean ok) {
		this.ok = ok;
	}

	public String getProblemDescription() {
		return problemDescription;
	}

	public void setProblemDescription(String problemDescription) {
		this.problemDescription = problemDescription;
	}
}