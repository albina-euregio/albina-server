package eu.albina.controller;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Parent controller for ALBINA. This singleton is
 * responsible for holding object like the session factory for all child
 * controllers.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class AlbinaController {

	/**
	 * The single instance of this class.
	 */
	private static AlbinaController instance = null;

	/**
	 * The session factory to talk to the database.
	 */
	protected SessionFactory sessionFactory;

	/**
	 * Default constructor.
	 */
	protected AlbinaController() {
		sessionFactory = new Configuration().configure().buildSessionFactory();
	}

	/**
	 * Singleton method for the controller.
	 * 
	 * @return The instance of this class.
	 */
	public static AlbinaController getInstance() {
		if (instance == null) {
			instance = new AlbinaController();
		}
		return instance;
	}
}
