package eu.albina.controller;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.util.HibernateUtil;

public class RegionControllerTest {

	// private static Logger logger =
	// LoggerFactory.getLogger(RegionControllerTest.class);

	@Before
	public void setUp() throws Exception {
		HibernateUtil.createSessionFactory();
	}

	@After
	public void shutDown() {
		// HibernateUtil.closeSessionFactory();
	}

	@Test
	public void getTopLevelRegionsTest() {
		try {
			List<Region> regions = RegionController.getInstance().getRegions("");
			Assert.assertEquals(6, regions.size());
		} catch (AlbinaException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getSubregionsTest() {
		try {
			List<Region> regions = RegionController.getInstance().getRegions("IT-32-TN");
			Assert.assertEquals(21, regions.size());
		} catch (AlbinaException e) {
			e.printStackTrace();
		}
	}
}
