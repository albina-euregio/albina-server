package eu.albina.controller;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.albina.exception.AlbinaException;
import eu.albina.util.HibernateUtil;

public class NewsControllerTest {

	// private static Logger logger =
	// LoggerFactory.getLogger(NewsControllerTest.class);

	@Before
	public void setUp() throws Exception {
		HibernateUtil.createSessionFactory();
	}

	@After
	public void shutDown() {
		// HibernateUtil.closeSessionFactory();
	}

	@Ignore
	@Test
	public void getNewsTest() {
		try {
			NewsController.getInstance().getNews(null, null);

			// TODO implement

		} catch (AlbinaException e) {
			e.printStackTrace();
		}
	}

	@Ignore
	@Test
	public void getNewsStartTest() {
		try {
			DateTime startDate = new DateTime();
			NewsController.getInstance().getNews(startDate, null);

			// TODO implement

		} catch (AlbinaException e) {
			e.printStackTrace();
		}
	}

	@Ignore
	@Test
	public void getNewsEndTest() {
		try {
			DateTime endDate = new DateTime();
			NewsController.getInstance().getNews(null, endDate);

			// TODO implement

		} catch (AlbinaException e) {
			e.printStackTrace();
		}
	}

	@Ignore
	@Test
	public void getNewsStartEndTest() {
		try {
			DateTime startDate = new DateTime();
			DateTime endDate = new DateTime();
			NewsController.getInstance().getNews(startDate, endDate);

			// TODO implement

		} catch (AlbinaException e) {
			e.printStackTrace();
		}
	}
}
