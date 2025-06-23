/*******************************************************************************
 * Copyright (C) 2025 albina
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

import eu.albina.model.enumerations.LanguageCode;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "region_language_configuration")
public class RegionLanguageConfiguration {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "REGION_ID")
	private Region region;

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE", length = 191)
	public LanguageCode lang;

	@Column(name = "WEBSITE_NAME", length = 191)
	public String websiteName;

	@Column(name = "WARNING_SERVICE_NAME", length = 191)
	public String warningServiceName;

	@Column(name = "WARNING_SERVICE_EMAIL", length = 191)
	public String warningServiceEmail;

	@Column(name = "URL", length = 191)
	public String url;

	@Column(name = "URL_WITH_DATE", length = 191)
	public String urlWithDate;

	@Column(name = "STATIC_URL", length = 191)
	private String staticUrl;
}
