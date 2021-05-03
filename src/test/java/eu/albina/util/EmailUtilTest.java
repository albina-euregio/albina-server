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
package eu.albina.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmailUtilTest {

	private List<AvalancheBulletin> bulletins;
	private List<AvalancheBulletin> bulletinsAmPm;

	@Before
	public void setUp() throws IOException {
		// HibernateUtil.getInstance().setUp();
		// Load valid avalanche bulletin JSON from resources
		bulletins = new ArrayList<AvalancheBulletin>();
		bulletinsAmPm = new ArrayList<AvalancheBulletin>();
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_1.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_2.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_3.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_4.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_5.json")));
		bulletinsAmPm.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_6.json")));
		bulletinsAmPm.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_7.json")));
	}

	@After
	public void shutDown() {
		// HibernateUtil.getInstance().shutDown();
	}

	@Ignore
	@Test
	public void createFreemarker() throws IOException, URISyntaxException {
		String html = EmailUtil.getInstance().createBulletinEmailHtml(bulletins, LanguageCode.de,
				GlobalVariables.codeTyrol, false, false);
		System.out.println(html);
	}

	@Ignore
	@Test
	public void sendEmail() throws MessagingException, IOException, URISyntaxException {
		ArrayList<String> regions = new ArrayList<String>();
		regions.add(GlobalVariables.codeTyrol);
		EmailUtil.getInstance().sendBulletinEmails(bulletins, regions, false);
	}
}
