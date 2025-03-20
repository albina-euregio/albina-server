/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.DangerSourceVariant;
import eu.albina.model.DangerSourceVariantText;
import eu.albina.model.enumerations.AvalancheType;
import eu.albina.util.HibernateUtil;


/**
 * Controller for danger sources variant texts.
 *
 * @author Norbert Lanzanasto
 *
 */
public class DangerSourceVariantTextController {

	private static Logger logger = LoggerFactory.getLogger(DangerSourceVariantTextController.class);

	private static DangerSourceVariantTextController instance = null;

	/**
	 * Private constructor.
	 */
	private DangerSourceVariantTextController() {
	}

	/**
	 * Returns the {@code DangerSourceVariantTextController} object associated with the
	 * current Java application.
	 *
	 * @return the {@code DangerSourceVariantTextController} object associated with the
	 *         current Java application.
	 */
	public static DangerSourceVariantTextController getInstance() {
		if (instance == null) {
			instance = new DangerSourceVariantTextController();
		}
		return instance;
	}

	/**
	 * Returns all danger source variant texts for the given {@code avalanche type}.
	 *
	 * @param avalancheType
	 *            the type of avalanche
	 * @return all danger source variant texts for the given avalanche type
	 */
	private List<DangerSourceVariantText> getDangerSourceVariantTexts(AvalancheType avalancheType) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			return entityManager.createQuery(HibernateUtil.queryGetDangerSourceVariantTexts, DangerSourceVariantText.class)
				.setParameter("avalancheType", avalancheType).getResultList();
		});
	}

	private DangerSourceVariantText getDangerSourceVariantText(DangerSourceVariant dangerSourceVariant) {
		List<DangerSourceVariantText> result = getDangerSourceVariantTexts(dangerSourceVariant.getAvalancheType());
		switch (dangerSourceVariant.getAvalancheType()) {
			case glide:
				return result.stream()
					.filter(text -> dangerSourceVariant.getGlidingSnowActivity() == null || text.getGlidingSnowActivity() == dangerSourceVariant.getGlidingSnowActivity())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize() == null || text.getAvalancheSize() == dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getFrequency() == null || text.getFrequency() == dangerSourceVariant.getEawsMatrixInformation().getFrequency())
					.findFirst().orElse(null);
			case loose:
				return result.stream()
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize() == null || text.getAvalancheSize() == dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getSnowpackStability() == null || text.getSnowpackStability() == dangerSourceVariant.getEawsMatrixInformation().getSnowpackStability())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getFrequency() == null || text.getFrequency() == dangerSourceVariant.getEawsMatrixInformation().getFrequency())
					.findFirst().orElse(null);
			case slab:
				return result.stream()
					.filter(text -> text.getAvalancheProblem() == dangerSourceVariant.deriveAvalancheProblem())
					.filter(text -> dangerSourceVariant.getHasDaytimeDependency() == null || text.getHasDaytimeDependency() == dangerSourceVariant.getHasDaytimeDependency())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize() == null || text.getAvalancheSize() == dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getSnowpackStability() == null || text.getSnowpackStability() == dangerSourceVariant.getEawsMatrixInformation().getSnowpackStability())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getFrequency() == null || text.getFrequency() == dangerSourceVariant.getEawsMatrixInformation().getFrequency())
					.findFirst().orElse(null);
			default:
				return null;
		}
	}

	public String getTextForDangerSourceVariant(DangerSourceVariant dangerSourceVariant) {
		DangerSourceVariantText dangerSourceVariantText = getDangerSourceVariantText(dangerSourceVariant);
		if (dangerSourceVariantText == null) {
			logger.warn("No danger source variant text found for: {}", dangerSourceVariant);
			return "";
		}

		// TODO replace placeholders with actual values

		return dangerSourceVariantText.getTextcat();
	}
}
