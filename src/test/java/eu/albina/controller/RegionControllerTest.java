/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
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

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;

public class RegionControllerTest {

	// private static Logger logger =
	// LoggerFactory.getLogger(RegionControllerTest.class);

	@Before
	public void setUp() throws Exception {
		HibernateUtil.getInstance().setUp();
	}

	@After
	public void shutDown() {
		HibernateUtil.getInstance().shutDown();
	}

	@Ignore
	@Test
	public void getTopLevelRegionsTest() {
		try {
			List<Region> regions = RegionController.getInstance().getRegions("");
			Assert.assertEquals(6, regions.size());
		} catch (AlbinaException e) {
			e.printStackTrace();
		}
	}

	@Ignore
	@Test
	public void getSubregionsTest() {
		try {
			List<Region> regions = RegionController.getInstance().getRegions(GlobalVariables.codeTrentino);
			Assert.assertEquals(21, regions.size());
		} catch (AlbinaException e) {
			e.printStackTrace();
		}
	}
}
