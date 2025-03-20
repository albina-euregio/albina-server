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
		return getDangerSourceVariantTexts(dangerSourceVariant.getAvalancheType()).stream()
			.filter(text -> text.getAvalancheProblem() == dangerSourceVariant.getAvalancheProblem())
			.filter(text -> text.getHasDaytimeDependency() == dangerSourceVariant.getHasDaytimeDependency())
			.filter(text -> text.getGlidingSnowActivity() == dangerSourceVariant.getGlidingSnowActivity())
			.filter(text -> text.getAvalancheSize() == dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize())
			.filter(text -> text.getSnowpackStability() == dangerSourceVariant.getEawsMatrixInformation().getSnowpackStability())
			.filter(text -> text.getFrequency() == dangerSourceVariant.getEawsMatrixInformation().getFrequency())
			.findFirst().orElse(null);
	}

	public String getTextForDangerSourceVariant(DangerSourceVariant dangerSourceVariant) {
		DangerSourceVariantText dangerSourceVariantText = getDangerSourceVariantText(dangerSourceVariant);
		if (dangerSourceVariantText == null) {
			logger.warn("No danger source variant text found for: {}", dangerSourceVariant);
			return null;
		}

		// TODO replace placeholders with actual values

		return dangerSourceVariantText.getTextcat();
	}
}
