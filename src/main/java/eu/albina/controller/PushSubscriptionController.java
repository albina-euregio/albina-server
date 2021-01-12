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

import org.hibernate.HibernateException;

import eu.albina.model.PushSubscription;
import eu.albina.util.HibernateUtil;

public interface PushSubscriptionController {

	static Long create(PushSubscription subscription) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.persist(subscription);
			return subscription.getId();
		});
	}
}
