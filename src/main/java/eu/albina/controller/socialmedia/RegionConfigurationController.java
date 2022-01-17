/*******************************************************************************
 * Copyright (C) 2019 Clesius srl
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
package eu.albina.controller.socialmedia;

import java.util.List;

import javax.persistence.TypedQuery;

import org.hibernate.HibernateException;

import eu.albina.exception.AlbinaException;
import eu.albina.model.socialmedia.Channel;
import eu.albina.model.socialmedia.RapidMailConfig;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.util.HibernateUtil;

public class RegionConfigurationController {

	private static RegionConfigurationController instance = null;

	public static RegionConfigurationController getInstance() {
		if (instance == null) {
			instance = new RegionConfigurationController();
		}
		return instance;
	}

	public RegionConfigurationController() {
	}

	public RegionConfiguration getRegionConfiguration(String regionConfigurationId) throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			TypedQuery<Long> query = entityManager.createQuery(
					"SELECT c.id FROM RegionConfiguration c WHERE c.region.id='" + regionConfigurationId + "'",
					Long.class);
			Long id = query.getSingleResult();
			RegionConfiguration regionConfiguration = entityManager.find(RegionConfiguration.class, id);
			if (regionConfiguration == null) {
				throw new HibernateException("No configuration with ID: " + regionConfigurationId);
			}
			if (regionConfiguration.getRapidMailConfig() == null) {
				regionConfiguration.setRapidMailConfig(new RapidMailConfig());
			}
			return regionConfiguration;
		});
	}

	public Long saveRegionConfiguration(RegionConfiguration regionConfiguration) throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.merge(regionConfiguration.getRapidMailConfig());
			entityManager.merge(regionConfiguration);
			return regionConfiguration.getId();
		});
	}

	public List<Channel> getChannels() throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			TypedQuery<Channel> query = entityManager.createQuery("SELECT c FROM Channel c", Channel.class);
			return query.getResultList();
		});
	}

}
