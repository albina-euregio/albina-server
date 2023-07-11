package eu.albina.util;

import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.User;
import eu.albina.model.enumerations.Position;
import eu.albina.model.enumerations.Role;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.*;

public class DBMigration {

	private static final Logger LOGGER = LoggerFactory.getLogger(DBMigration.class);

	public static void executeMigration() {
		if (!"true".equals(System.getenv("ALBINA_DB_RUN_MIGRATION"))) {
			LOGGER.info("database migration skipped");
			return;
		}

		LOGGER.info("start database migration");

		try (Session session = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager().unwrap(Session.class)) {

			session.doWork(connection -> {
				try {
					Map<String, Object> scopeValues = new HashMap<>();
					scopeValues.put("liquibase.hub.mode", "off");
					scopeValues.put(Scope.Attr.resourceAccessor.name(), new ClassLoaderResourceAccessor());

					Scope.child(scopeValues, () -> {
						JdbcConnection jdbcConnection = new JdbcConnection(connection);
						Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);

						CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
						updateCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database);
						updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "/db/changelog-main.xml");
						updateCommand.execute();

					});
				} catch (Exception e) {
					throw new HibernateException("database migration failed", e);
				}
			});
		}

		LOGGER.info("database migration finished");
	}

	public static void createAutoConfiguration() {
		if (!"true".equals(System.getenv("ALBINA_DB_AUTO_CONFIG"))) {
			LOGGER.info("auto configuration skipped");
			return;
		}

		LOGGER.info("start auto configuration");

		HibernateUtil.getInstance().runTransaction(em -> {
			List<ServerInstance> localInstances = em.createQuery(HibernateUtil.queryGetLocalServerInstance, ServerInstance.class).getResultList();
			if(!localInstances.isEmpty()) {
				LOGGER.info("local server instance already exists, skip auto configuration");
				return 1;
			}

			// local server instance
			ServerInstance si = createLocalServerInstance(em);
			// admin and forecaster user
			User adminForecaster = createAdminForecaster(em);
			// euregio region
			Region euregio = createEuregioRegion(em, si);
			// euregio subregion at07
			Region at07 = createEuregioSubRegionAt07(em, si, euregio);
			// euregio subregion it32bz
			Region it32bz = createEuregioSubRegionIt32Bz(em, si, euregio);
			// euregio subregion it32tn
			Region it32tn = createEuregioSubRegionIt32Tn(em, si, euregio);
			// ch region
			Region ch = createChRegion(em, si);
			// connect regions to admin
			adminForecaster.setRegions(Set.of(at07, it32bz, it32tn, ch));
			return 0;
		});

		LOGGER.info("auto configuration finished");
	}

	private static Region createChRegion(EntityManager em, ServerInstance si) {
		Region ch = new Region();
		ch.setId("CH");
		ch.setCreateCaamlV5(true);
		ch.setCreateCaamlV6(true);
		ch.setCreateJson(true);
		ch.setCreateMaps(true);
		ch.setCreatePdf(true);
		ch.setCreateSimpleHtml(true);
		ch.setEmailColor("1AABFF");
		ch.setEnableMediaFile(false);
		ch.setGeoDataDirectory("geodata.CH/");
		ch.setImageColorbarBwPath("logo/grey/colorbar.gif");
		ch.setImageColorbarColorPath("logo/color/colorbar.gif");
		ch.setMapCenterLat(46.8);
		ch.setMapCenterLng(8.5);
		ch.setMapLogoBwPath("");
		ch.setMapLogoColorPath("");
		ch.setMapLogoPosition(Position.topleft);
		ch.setMapXmax(1176800);
		ch.setMapXmin(646800);
		ch.setMapYmax(6053000);
		ch.setMapYmin(5710000);
		ch.setMicroRegions(36);
		ch.setPdfColor("00ACFB");
		ch.setPdfFooterLogo(true);
		ch.setPdfFooterLogoBwPath("logo/grey/slf.png");
		ch.setPdfFooterLogoColorPath("logo/color/slf.png");
		ch.setPdfMapHeight(270);
		ch.setPdfMapWidthAmPm(270);
		ch.setPdfMapWidthFd(420);
		ch.setPdfMapYAmPm(130);
		ch.setPdfMapYFd(250);
		ch.setPublishBlogs(false);
		ch.setPublishBulletins(true);
		ch.setSendEmails(false);
		ch.setSendPushNotifications(false);
		ch.setSendTelegramMessages(false);
		ch.setSimpleHtmlTemplateName("simple-bulletin.min.html");
		ch.setServerInstance(si);
		ch.setShowMatrix(false);
		em.persist(ch);
		return ch;
	}

	private static Region createEuregioSubRegionIt32Tn(EntityManager em, ServerInstance si, Region euregio) {
		Region it32tn = new Region();
		it32tn.setId("IT-32-TN");
		it32tn.setCreateCaamlV5(true);
		it32tn.setCreateCaamlV6(true);
		it32tn.setCreateJson(true);
		it32tn.setCreateMaps(true);
		it32tn.setCreatePdf(true);
		it32tn.setCreateSimpleHtml(true);
		it32tn.setEmailColor("1AABFF");
		it32tn.setEnableMediaFile(false);
		it32tn.setGeoDataDirectory("geodata.Euregio/IT-32-TN/");
		it32tn.setImageColorbarBwPath("logo/grey/colorbar.gif");
		it32tn.setImageColorbarColorPath("logo/color/colorbar.gif");
		it32tn.setMapCenterLat(46.05);
		it32tn.setMapCenterLng(11.07);
		it32tn.setMapLogoBwPath("");
		it32tn.setMapLogoColorPath("");
		it32tn.setMapLogoPosition(Position.bottomright);
		it32tn.setMapXmax(1358000);
		it32tn.setMapXmin(1133000);
		it32tn.setMapYmax(5842000);
		it32tn.setMapYmin(5692000);
		it32tn.setMicroRegions(21);
		it32tn.setPdfColor("00ACFB");
		it32tn.setPdfFooterLogo(true);
		it32tn.setPdfFooterLogoBwPath("logo/grey/euregio.png");
		it32tn.setPdfFooterLogoColorPath("logo/color/euregio.png");
		it32tn.setPdfMapHeight(267);
		it32tn.setPdfMapWidthAmPm(400);
		it32tn.setPdfMapWidthFd(500);
		it32tn.setPdfMapYAmPm(130);
		it32tn.setPdfMapYFd(290);
		it32tn.setPublishBlogs(false);
		it32tn.setPublishBulletins(true);
		it32tn.setSendEmails(false);
		it32tn.setSendPushNotifications(false);
		it32tn.setSendTelegramMessages(false);
		it32tn.setSimpleHtmlTemplateName("simple-bulletin.min.html");
		it32tn.setServerInstance(si);
		it32tn.setShowMatrix(false);
		it32tn.setSuperRegions(Set.of(euregio));
		em.persist(it32tn);
		return it32tn;
	}

	private static Region createEuregioSubRegionIt32Bz(EntityManager em, ServerInstance si, Region euregio) {
		Region it32bz = new Region();
		it32bz.setId("IT-32-BZ");
		it32bz.setCreateCaamlV5(true);
		it32bz.setCreateCaamlV6(true);
		it32bz.setCreateJson(true);
		it32bz.setCreateMaps(true);
		it32bz.setCreatePdf(true);
		it32bz.setCreateSimpleHtml(true);
		it32bz.setEmailColor("1AABFF");
		it32bz.setEnableMediaFile(false);
		it32bz.setGeoDataDirectory("geodata.Euregio/IT-32-BZ/");
		it32bz.setImageColorbarBwPath("logo/grey/colorbar.gif");
		it32bz.setImageColorbarColorPath("logo/color/colorbar.gif");
		it32bz.setMapCenterLat(46.65);
		it32bz.setMapCenterLng(11.4);
		it32bz.setMapLogoBwPath("");
		it32bz.setMapLogoColorPath("");
		it32bz.setMapLogoPosition(Position.bottomright);
		it32bz.setMapXmax(1400000);
		it32bz.setMapXmin(1145000);
		it32bz.setMapYmax(5939000);
		it32bz.setMapYmin(5769000);
		it32bz.setMicroRegions(31);
		it32bz.setPdfColor("00ACFB");
		it32bz.setPdfFooterLogo(true);
		it32bz.setPdfFooterLogoBwPath("logo/grey/euregio.png");
		it32bz.setPdfFooterLogoColorPath("logo/color/euregio.png");
		it32bz.setPdfMapHeight(267);
		it32bz.setPdfMapWidthAmPm(400);
		it32bz.setPdfMapWidthFd(500);
		it32bz.setPdfMapYAmPm(130);
		it32bz.setPdfMapYFd(290);
		it32bz.setPublishBlogs(false);
		it32bz.setPublishBulletins(true);
		it32bz.setSendEmails(false);
		it32bz.setSendPushNotifications(false);
		it32bz.setSendTelegramMessages(false);
		it32bz.setSimpleHtmlTemplateName("simple-bulletin.min.html");
		it32bz.setServerInstance(si);
		it32bz.setShowMatrix(false);
		it32bz.setSuperRegions(Set.of(euregio));
		em.persist(it32bz);
		return it32bz;
	}

	private static Region createEuregioSubRegionAt07(EntityManager em, ServerInstance si, Region euregio) {
		Region at07 = new Region();
		at07.setId("AT-07");
		at07.setCreateCaamlV5(true);
		at07.setCreateCaamlV6(true);
		at07.setCreateJson(true);
		at07.setCreateMaps(true);
		at07.setCreatePdf(true);
		at07.setCreateSimpleHtml(true);
		at07.setEmailColor("1AABFF");
		at07.setEnableMediaFile(false);
		at07.setGeoDataDirectory("geodata.Euregio/AT-07/");
		at07.setImageColorbarBwPath("logo/grey/colorbar.gif");
		at07.setImageColorbarColorPath("logo/color/colorbar.gif");
		at07.setMapCenterLat(47.1);
		at07.setMapCenterLng(11.44);
		at07.setMapLogoBwPath("");
		at07.setMapLogoColorPath("");
		at07.setMapLogoPosition(Position.bottomright);
		at07.setMapXmax(1452000);
		at07.setMapXmin(1116000);
		at07.setMapYmax(6053000);
		at07.setMapYmin(5829000);
		at07.setMicroRegions(36);
		at07.setPdfColor("00ACFB");
		at07.setPdfFooterLogo(true);
		at07.setPdfFooterLogoBwPath("logo/grey/euregio.png");
		at07.setPdfFooterLogoColorPath("logo/color/euregio.png");
		at07.setPdfMapHeight(267);
		at07.setPdfMapWidthAmPm(400);
		at07.setPdfMapWidthFd(500);
		at07.setPdfMapYAmPm(130);
		at07.setPdfMapYFd(290);
		at07.setPublishBlogs(false);
		at07.setPublishBulletins(true);
		at07.setSendEmails(false);
		at07.setSendPushNotifications(false);
		at07.setSendTelegramMessages(false);
		at07.setSimpleHtmlTemplateName("simple-bulletin.min.html");
		at07.setServerInstance(si);
		at07.setShowMatrix(false);
		at07.setSuperRegions(Set.of(euregio));
		em.persist(at07);
		return at07;
	}

	private static Region createEuregioRegion(EntityManager em, ServerInstance si) {
		Region euregio = new Region();
		euregio.setId("EUREGIO");
		euregio.setCreateCaamlV5(true);
		euregio.setCreateCaamlV6(true);
		euregio.setCreateJson(true);
		euregio.setCreateMaps(true);
		euregio.setCreatePdf(true);
		euregio.setCreateSimpleHtml(true);
		euregio.setEmailColor("1AABFF");
		euregio.setEnableMediaFile(false);
		euregio.setGeoDataDirectory("geodata.Euregio/");
		euregio.setImageColorbarBwPath("logo/grey/colorbar.gif");
		euregio.setImageColorbarColorPath("logo/color/colorbar.gif");
		euregio.setMapCenterLat(0.0);
		euregio.setMapCenterLng(0.0);
		euregio.setMapLogoBwPath("logo/color/colorbar.gif");
		euregio.setMapLogoColorPath("images/logo/color/euregio.png");
		euregio.setMapLogoPosition(Position.bottomright);
		euregio.setMapXmax(1464000);
		euregio.setMapXmin(1104000);
		euregio.setMapYmax(6047000);
		euregio.setMapYmin(5687000);
		euregio.setMicroRegions(0);
		euregio.setPdfColor("00ACFB");
		euregio.setPdfFooterLogo(true);
		euregio.setPdfFooterLogoBwPath("logo/grey/euregio.png");
		euregio.setPdfFooterLogoColorPath("logo/color/euregio.png");
		euregio.setPdfMapHeight(270);
		euregio.setPdfMapWidthAmPm(270);
		euregio.setPdfMapWidthFd(420);
		euregio.setPdfMapYAmPm(130);
		euregio.setPdfMapYFd(250);
		euregio.setPublishBlogs(false);
		euregio.setPublishBulletins(true);
		euregio.setSendEmails(false);
		euregio.setSendPushNotifications(false);
		euregio.setSendTelegramMessages(false);
		euregio.setSimpleHtmlTemplateName("simple-bulletin.min.html");
		euregio.setServerInstance(si);
		euregio.setShowMatrix(false);
		em.persist(euregio);
		return euregio;
	}

	private static User createAdminForecaster(EntityManager em) {
		String password = UUID.randomUUID().toString();
		LOGGER.info("------------------------------------------------------------");
		LOGGER.info("info@albina.local - {}", password);
		LOGGER.info("------------------------------------------------------------");
		User admin = new User();
		admin.setEmail("info@albina.local");
		admin.setName("info@albina.local");
		admin.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
		admin.setRoles(List.of(Role.ADMIN, Role.FORECASTER));
		em.persist(admin);
		return admin;
	}

	private static ServerInstance createLocalServerInstance(EntityManager em) {
		ServerInstance si = new ServerInstance();
		si.setExternalServer(false);
		si.setHtmlDirectory("/app/static/simple");
		si.setMapProductionUrl("/app/avalanche-warning-maps");
		si.setMapsPath("/app/static/bulletins");
		si.setMediaPath("/app/static/media_files");
		si.setName("Lokal");
		si.setPdfDirectory("/app/static/bulletins");
		si.setPublishAt5PM(true);
		si.setPublishAt8AM(true);
		si.setServerImagesUrl("https://admin.avalanche.report/images/");
		si.setUserName("info@albina.local");
		em.persist(si);
		return si;
	}

}
