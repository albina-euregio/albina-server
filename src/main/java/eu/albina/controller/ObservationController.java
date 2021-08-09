/*******************************************************************************
 * Copyright (C) 2021 albina-euregio
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
package eu.albina.controller;

import eu.albina.model.Observation;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.HibernateException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public interface ObservationController {

	static Observation create(Observation observation) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.persist(observation);
			return observation;
		});
	}

	static Observation update(Observation observation) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.merge(observation);
			return observation;
		});
	}

	static Observation get(long id) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager ->
			entityManager.find(Observation.class, id));
	}

	static List<Observation> get(LocalDateTime startDate, LocalDateTime endDate) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Observation> select = criteriaBuilder.createQuery(Observation.class);
			Root<Observation> root = select.from(Observation.class);
			select.where(criteriaBuilder.between(root.get("eventDate"), startDate, endDate));
			return entityManager.createQuery(select).getResultList();
		});
	}

	static void delete(long id) throws HibernateException {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			Observation observation = entityManager.find(Observation.class, id);
			entityManager.remove(observation);
			return null;
		});
	}

	static String getCsv(LocalDateTime start, LocalDateTime end) {
		List<Observation> observations = get(start, end);

		// sort observations by event date
		observations.sort(Comparator.comparing(Observation::getEventDate));

		StringBuilder sb = new StringBuilder();

		// add header
		sb.append("EventDate");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("EventTime");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("EventType");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("ReportDate");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AuthorName");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("LocationName");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Latitude");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Longitude");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Elevation");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Aspect");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Region");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Content");
		sb.append(GlobalVariables.csvLineBreak);

		for (Observation observation : observations) {
			addCsvLines(sb, observation);
		}

		return sb.toString();
	}

	static void addCsvLines(StringBuilder sb, Observation observation) {
		if (observation.getEventDate() != null) {
			sb.append(observation.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(observation.getEventDate().format(DateTimeFormatter.ofPattern("HH:mm")));
		} else {
			sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(GlobalVariables.notAvailableString);
		}
		sb.append(GlobalVariables.csvDeliminator);
		if (observation.getEventType() != null)
			sb.append(observation.getEventType());
		else
			sb.append(GlobalVariables.notAvailableString);
		sb.append(GlobalVariables.csvDeliminator);
		if (observation.getReportDate() != null)
			sb.append(observation.getReportDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		else
			sb.append(GlobalVariables.notAvailableString);
		sb.append(GlobalVariables.csvDeliminator);
		if (observation.getAuthorName() != null)
			sb.append(StringEscapeUtils.escapeCsv(observation.getAuthorName()));
		else
			sb.append(GlobalVariables.notAvailableString);
		sb.append(GlobalVariables.csvDeliminator);
		if (observation.getLocationName() != null)
			sb.append(StringEscapeUtils.escapeCsv(observation.getLocationName()));
		else
			sb.append(GlobalVariables.notAvailableString);
		sb.append(GlobalVariables.csvDeliminator);
		if (observation.getLatitude() != null)
			sb.append(observation.getLatitude());
		else
			sb.append(GlobalVariables.notAvailableString);
		sb.append(GlobalVariables.csvDeliminator);
		if (observation.getLongitude() != null)
			sb.append(observation.getLongitude());
		else
			sb.append(GlobalVariables.notAvailableString);
		sb.append(GlobalVariables.csvDeliminator);
		if (observation.getElevation() != null)
			sb.append(observation.getElevation());
		else
			sb.append(GlobalVariables.notAvailableString);
		sb.append(GlobalVariables.csvDeliminator);
		if (observation.getAspect() != null)
			sb.append(observation.getAspect());
		else
			sb.append(GlobalVariables.notAvailableString);
		sb.append(GlobalVariables.csvDeliminator);
		if (observation.getRegion() != null)
			sb.append(observation.getRegion());
		else
			sb.append(GlobalVariables.notAvailableString);
		sb.append(GlobalVariables.csvDeliminator);
		if (observation.getContent() != null) {
			sb.append(StringEscapeUtils.escapeCsv(observation.getContent()));
		} else
			sb.append(GlobalVariables.notAvailableString);
		sb.append(GlobalVariables.csvLineBreak);
	}
}
