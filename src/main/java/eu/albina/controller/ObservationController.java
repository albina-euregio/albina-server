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
package eu.albina.controller;

import eu.albina.model.GenericObservation;
import eu.albina.util.HibernateUtil;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.HibernateException;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public interface ObservationController {

	static GenericObservation create(GenericObservation observation) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.persist(observation);
			return observation;
		});
	}

	static GenericObservation update(GenericObservation observation) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.merge(observation);
			return observation;
		});
	}

	static GenericObservation get(long id) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager ->
			entityManager.find(GenericObservation.class, id));
	}

	static List<GenericObservation> get(LocalDateTime startDate, LocalDateTime endDate) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<GenericObservation> select = criteriaBuilder.createQuery(GenericObservation.class);
			Root<GenericObservation> root = select.from(GenericObservation.class);
			select.where(criteriaBuilder.between(root.get("eventDate"), startDate, endDate));
			return entityManager.createQuery(select).getResultList();
		});
	}

	static void delete(long id) throws HibernateException {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			GenericObservation observation = entityManager.find(GenericObservation.class, id);
			entityManager.remove(observation);
			return null;
		});
	}

	static String getCsv(LocalDateTime start, LocalDateTime end) {
		List<GenericObservation> observations = get(start, end);

		// sort observations by event date
		observations.sort(Comparator.comparing(GenericObservation::getEventDate));

		StringBuilder sb = new StringBuilder();

		// add header
		sb.append("EventDate");
		sb.append(StatisticsController.csvDeliminator);
		sb.append("EventTime");
		sb.append(StatisticsController.csvDeliminator);
		sb.append("EventType");
		sb.append(StatisticsController.csvDeliminator);
		sb.append("ReportDate");
		sb.append(StatisticsController.csvDeliminator);
		sb.append("AuthorName");
		sb.append(StatisticsController.csvDeliminator);
		sb.append("LocationName");
		sb.append(StatisticsController.csvDeliminator);
		sb.append("Latitude");
		sb.append(StatisticsController.csvDeliminator);
		sb.append("Longitude");
		sb.append(StatisticsController.csvDeliminator);
		sb.append("Elevation");
		sb.append(StatisticsController.csvDeliminator);
		sb.append("Aspect");
		sb.append(StatisticsController.csvDeliminator);
		sb.append("Region");
		sb.append(StatisticsController.csvDeliminator);
		sb.append("Content");
		sb.append(StatisticsController.csvLineBreak);

		for (GenericObservation observation : observations) {
			addCsvLines(sb, observation);
		}

		return sb.toString();
	}

	static void addCsvLines(StringBuilder sb, GenericObservation observation) {
		if (observation.getEventDate() != null) {
			sb.append(observation.getEventDate().toLocalDate());
			sb.append(StatisticsController.csvDeliminator);
			sb.append(observation.getEventDate().toLocalTime());
		} else {
			sb.append(StatisticsController.notAvailableString);
			sb.append(StatisticsController.csvDeliminator);
			sb.append(StatisticsController.notAvailableString);
		}
		sb.append(StatisticsController.csvDeliminator);
		sb.append(StatisticsController.notAvailableString);
		sb.append(StatisticsController.csvDeliminator);
		if (observation.getReportDate() != null)
			sb.append(observation.getReportDate().toLocalDate());
		else
			sb.append(StatisticsController.notAvailableString);
		sb.append(StatisticsController.csvDeliminator);
		if (observation.getAuthorName() != null)
			sb.append(StringEscapeUtils.escapeCsv(observation.getAuthorName()));
		else
			sb.append(StatisticsController.notAvailableString);
		sb.append(StatisticsController.csvDeliminator);
		if (observation.getLocationName() != null)
			sb.append(StringEscapeUtils.escapeCsv(observation.getLocationName()));
		else
			sb.append(StatisticsController.notAvailableString);
		sb.append(StatisticsController.csvDeliminator);
		if (observation.getLatitude() != null)
			sb.append(observation.getLatitude());
		else
			sb.append(StatisticsController.notAvailableString);
		sb.append(StatisticsController.csvDeliminator);
		if (observation.getLongitude() != null)
			sb.append(observation.getLongitude());
		else
			sb.append(StatisticsController.notAvailableString);
		sb.append(StatisticsController.csvDeliminator);
		if (observation.getElevation() != null)
			sb.append(observation.getElevation());
		else
			sb.append(StatisticsController.notAvailableString);
		sb.append(StatisticsController.csvDeliminator);
		if (observation.getAspects() != null)
			sb.append(observation.getAspects());
		else
			sb.append(StatisticsController.notAvailableString);
		sb.append(StatisticsController.csvDeliminator);
		if (observation.getRegionId() != null)
			sb.append(observation.getRegionId());
		else
			sb.append(StatisticsController.notAvailableString);
		sb.append(StatisticsController.csvDeliminator);
		if (observation.getObsContent() != null) {
			sb.append(StringEscapeUtils.escapeCsv(observation.getObsContent()));
		} else
			sb.append(StatisticsController.notAvailableString);
		sb.append(StatisticsController.csvLineBreak);
	}
}
