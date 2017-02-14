package org.avalanches.ais.controller;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Parent controller for the avalanche information system. This singleton is
 * responsible for holding object like the session factory for all child
 * controllers.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class AvalancheInformationSystemController {

	/**
	 * The single instance of this class.
	 */
	private static AvalancheInformationSystemController instance = null;

	/**
	 * The session factory to talk to the database.
	 */
	protected SessionFactory sessionFactory;

	/**
	 * Default constructor.
	 */
	protected AvalancheInformationSystemController() {
		sessionFactory = new Configuration().configure().buildSessionFactory();
	}

	/**
	 * Singleton method for the controller.
	 * 
	 * @return The instance of this class.
	 */
	public static AvalancheInformationSystemController getInstance() {
		if (instance == null) {
			instance = new AvalancheInformationSystemController();
		}
		return instance;
	}
}
