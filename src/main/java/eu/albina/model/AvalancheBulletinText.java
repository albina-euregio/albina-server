// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.TextPart;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "avalanche_bulletin_texts")
@IdClass(AvalancheBulletinText.AvalancheBulletinTextId.class)
public class AvalancheBulletinText {
	@Id
	@ManyToOne
	private AvalancheBulletin avalancheBulletin;

	@Id
	@Enumerated(EnumType.STRING)
	private TextPart textType;

	@Id
	@Enumerated(EnumType.STRING)
	private LanguageCode languageCode;

	@Column(name = "TEXT")
	private String text;

	public AvalancheBulletinText() {
	}

	public AvalancheBulletinText(AvalancheBulletin avalancheBulletin, TextPart textType, LanguageCode languageCode, String text) {
		this.avalancheBulletin = avalancheBulletin;
		this.textType = textType;
		this.languageCode = languageCode;
		this.text = text;
	}

	public AvalancheBulletin getAvalancheBulletin() {
		return avalancheBulletin;
	}

	public void setAvalancheBulletin(AvalancheBulletin avalancheBulletin) {
		this.avalancheBulletin = avalancheBulletin;
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public static class AvalancheBulletinTextId implements Serializable {
		@ManyToOne
		@JoinColumn(name = "AVALANCHE_BULLETIN_ID")
		private AvalancheBulletin avalancheBulletin;

		@Enumerated(EnumType.STRING)
		@Column(name = "TEXT_TYPE")
		private TextPart textType;

		@Enumerated(EnumType.STRING)
		@Column(name = "LANGUAGE_CODE", length = 191)
		private LanguageCode languageCode;

		public AvalancheBulletin getAvalancheBulletin() {
			return avalancheBulletin;
		}

		public void setAvalancheBulletin(AvalancheBulletin avalancheBulletin) {
			this.avalancheBulletin = avalancheBulletin;
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
			if (!(o instanceof AvalancheBulletinTextId that)) return false;
			return Objects.equals(avalancheBulletin, that.avalancheBulletin) && textType == that.textType && languageCode == that.languageCode;
		}

		@Override
		public int hashCode() {
			return Objects.hash(avalancheBulletin, textType, languageCode);
		}
	}
}
