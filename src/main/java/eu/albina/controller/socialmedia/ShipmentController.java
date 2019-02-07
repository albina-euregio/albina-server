package eu.albina.controller.socialmedia;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.hibernate.HibernateException;

import eu.albina.exception.AlbinaException;
import eu.albina.model.socialmedia.Shipment;
import eu.albina.util.HibernateUtil;

public class ShipmentController extends CommonProcessor {
	private static ShipmentController instance = null;

	public static ShipmentController getInstance() {
		if (instance == null) {
			instance = new ShipmentController();
		}
		return instance;
	}

	public Long saveShipment(Shipment shipment) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			entityManager.merge(shipment);
			transaction.commit();
			return shipment.getId();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public List<Shipment> shipmentsList() throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			TypedQuery<Shipment> query = entityManager.createQuery("SELECT s " + "FROM Shipment s order by DATE DESC",
					Shipment.class);
			List<Shipment> shipmentsList = query.getResultList();
			transaction.commit();
			return shipmentsList;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}
}
