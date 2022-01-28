/*******************************************************************************
 * Copyright (C) 2022 Norbert Lanzanasto
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

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;

import eu.albina.exception.AlbinaException;
import eu.albina.model.ServerInstance;
import eu.albina.util.HibernateUtil;

/**
 * Controller for regions.
 *
 * @author Norbert Lanzanasto
 *
 */
public class ServerInstanceController {

	// private static Logger logger = LoggerFactory.getLogger(ServerInstanceController.class);

	private static ServerInstanceController instance = null;

	/**
	 * Private constructor.
	 */
	private ServerInstanceController() {
	}

	/**
	 * Returns the {@code ServerInstanceController} object associated with the current Java
	 * application.
	 *
	 * @return the {@code ServerInstanceController} object associated with the current Java
	 *         application.
	 */
	public static ServerInstanceController getInstance() {
		if (instance == null) {
			instance = new ServerInstanceController();
		}
		return instance;
	}

	/**
	 * Save a {@code serverInstance} to the database.
	 *
	 * @param serverInstance
	 *            the server instance to be saved
	 * @return the id of the saved server instance
	 */
	public Serializable createServerInstance(ServerInstance serverInstance) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.persist(serverInstance);
			return serverInstance.getId();
		});
	}

	public ServerInstance updateServerInstance(ServerInstance serverInstance) throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.merge(serverInstance);
			return serverInstance;
		});
	}

    public ServerInstance getServerInstance(String serverInstanceId) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			ServerInstance serverInstance = entityManager.find(ServerInstance.class, serverInstanceId);
			if (serverInstance == null) {
				throw new HibernateException("No server instance with ID: " + serverInstanceId);
			}
			return serverInstance;
		});
    }

	public ServerInstance getLocalServerInstance() {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			return (ServerInstance) entityManager.createQuery(HibernateUtil.queryGetLocalServerInstance).getSingleResult();
		});
	}

	@SuppressWarnings("unchecked")
	public List<ServerInstance> getExternalServerInstances() {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			return entityManager.createQuery(HibernateUtil.queryGetExternalServerInstances).getResultList();
		});
	}
}
