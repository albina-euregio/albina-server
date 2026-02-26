// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.TextPart;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Entity
@Table(name = "avalanche_bulletin_texts")
public class AvalancheBulletinText {
	@EmbeddedId
	private AvalancheBulletinTextId id;

	@Column(name = "TEXT")
	private String text;

	public AvalancheBulletinTextId getId() {
		return id;
	}

	public void setId(AvalancheBulletinTextId id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Embeddable
	public static class AvalancheBulletinTextId {
		@NotNull
		@Column(name = "AVALANCHE_BULLETIN_ID", nullable = false, length = 191)
		private String avalancheBulletinId;

		@NotNull
		@Enumerated(EnumType.STRING)
		@Column(name = "TEXT_TYPE", nullable = false)
		private TextPart textType;

		@NotNull
		@Enumerated(EnumType.STRING)
		@Column(name = "LANGUAGE_CODE", nullable = false, length = 191)
		private LanguageCode languageCode;

		public String getAvalancheBulletinId() {
			return avalancheBulletinId;
		}

		public void setAvalancheBulletinId(String avalancheBulletinId) {
			this.avalancheBulletinId = avalancheBulletinId;
		}

		public TextPart getTextType() {
			return textType;
		}

		public void setTextType(TextPart textType) {
			this.textType = textType;
		}

		public LanguageCode getLanguageCode() {
			return languageCode;
		}

		public void setLanguageCode(LanguageCode languageCode) {
			this.languageCode = languageCode;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AvalancheBulletinTextId entity = (AvalancheBulletinTextId) o;
			return Objects.equals(this.avalancheBulletinId, entity.avalancheBulletinId) &&
				Objects.equals(this.textType, entity.textType) &&
					Objects.equals(this.languageCode, entity.languageCode);
		}

		@Override
		public int hashCode() {
			return Objects.hash(avalancheBulletinId, textType, languageCode);
		}
	}
}
