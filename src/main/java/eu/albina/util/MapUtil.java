package eu.albina.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

public class MapUtil {

	private static final Logger logger = LoggerFactory.getLogger(MapUtil.class);

	/**
	 * Create images of each map needed for the different products of
	 * avalanche.report. This consists of an overview map over the whole EUREGIO,
	 * maps for each province and detailed maps for each aggregated region.
	 * 
	 * @param bulletins
	 *            The bulletins to create the maps from.
	 */
	public static void createDangerRatingMaps(List<AvalancheBulletin> bulletins) {

		// TODO implement local creation of danger rating maps
		// TODO delete copying of maps

		try {
			Document doc = XmlUtil.createCaaml(bulletins, LanguageCode.en);
			triggerMapProductionUnivie(XmlUtil.convertDocToString(doc));
			runCopyScript(AlbinaUtil.getValidityDate(bulletins));
		} catch (AlbinaException e) {
			logger.error("Error producing maps: " + e.getMessage());
			e.printStackTrace();
		} catch (TransformerException e) {
			logger.error("Error producing maps: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void runCopyScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", "/opt/copyMaps.sh", date);
			Process p = pb.start();
			p.waitFor();
			logger.info("Maps copied to local directory.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Maps could not be copied to local directory!");
		}
	}

	public static String triggerMapProductionUnivie(String caaml) throws AlbinaException {
		HttpURLConnection connection = null;

		try {
			// Create connection
			URL url = new URL(GlobalVariables.univieMapProductionUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length", Integer.toString(caaml.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(caaml);
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();

			if (connection.getResponseCode() != 200 && connection.getResponseCode() != 200)
				throw new AlbinaException("Error while triggering map production!");

			return response.toString();
		} catch (Exception e) {
			throw new AlbinaException(e.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}
