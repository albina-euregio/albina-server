// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;

import eu.albina.exception.AlbinaException;
import eu.albina.model.ServerInstance;
import eu.albina.util.HibernateUtil;
import jakarta.persistence.EntityManager;

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
		return HibernateUtil.getInstance().run(entityManager -> {
			ServerInstance serverInstance = entityManager.find(ServerInstance.class, serverInstanceId);
			if (serverInstance == null) {
				throw new HibernateException("No server instance with ID: " + serverInstanceId);
			}
			return serverInstance;
		});
    }

	public ServerInstance getLocalServerInstance() {
		return HibernateUtil.getInstance().run(this::getLocalServerInstance);
	}

	public ServerInstance getLocalServerInstance(EntityManager entityManager) {
		return entityManager.createQuery(HibernateUtil.queryGetLocalServerInstance, ServerInstance.class).getSingleResult();
	}

	public List<ServerInstance> getExternalServerInstances() {
		return HibernateUtil.getInstance().run(this::getExternalServerInstances);
	}

	public List<ServerInstance> getExternalServerInstances(EntityManager entityManager) {
		return entityManager.createQuery(HibernateUtil.queryGetExternalServerInstances, ServerInstance.class).getResultList();
	}

	public boolean serverInstanceExists(Long id) {
		return HibernateUtil.getInstance().run(entityManager -> entityManager.find(ServerInstance.class, id) != null);
	}
}
