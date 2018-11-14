package eu.albina.controller.socialmedia;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.albina.exception.AlbinaException;
import eu.albina.model.socialmedia.Shipment;
import eu.albina.util.HibernateUtil;
import org.hibernate.HibernateException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.util.List;

public class ShipmentController extends CommonProcessor{
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
            TypedQuery<Shipment> query =
                    entityManager.createQuery("SELECT s " +
                            "FROM Shipment s ", Shipment.class);
            List<Shipment> shipmentsList=query.getResultList();
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
