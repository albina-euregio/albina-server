package eu.albina.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.joda.time.DateTime;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;

public class MapUtil {

	// private static final Logger logger = LoggerFactory.getLogger(MapUtil.class);

	public static String createMapUrlOverview(DateTime date, int version, String daytime, int resolution,
			String fileExtension) {
		StringBuilder result = new StringBuilder();
		result.append(GlobalVariables.univieBaseUrl);
		result.append(date.toString(GlobalVariables.formatterDate));
		result.append(GlobalVariables.urlSeperator);
		result.append(version);
		result.append(GlobalVariables.urlSeperator);
		result.append(daytime);
		result.append(GlobalVariables.urlSeperator);
		result.append(resolution);
		result.append(".");
		result.append(fileExtension);

		return result.toString();
	}

	public static String createMapUrlAggregatedRegion(DateTime date, int version, String id, String fileExtension) {
		StringBuilder result = new StringBuilder();
		result.append(GlobalVariables.univieBaseUrl);
		result.append(date.toString(GlobalVariables.formatterDate));
		result.append(GlobalVariables.urlSeperator);
		result.append(version);
		result.append(GlobalVariables.urlSeperator);
		result.append(id);
		result.append(".");
		result.append(fileExtension);

		return result.toString();
	}

	/**
	 * Create images of each map needed for the different products of
	 * avalanche.report. This consists of an overview map over the whole EUREGIO,
	 * maps for each province and detailed maps for each aggregated region.
	 * 
	 * @param bulletins
	 *            The bulletins to create the maps from.
	 */
	public static void createDangerRatingMaps(List<AvalancheBulletin> bulletins) {

		// TODO implement creation of danger rating maps

		// overview map big (email, pdf)
		// overview map small (homepage archive)
		// map for each aggregated region (email, pdf)
		// map for TN, BZ and TI (for later, email, pdf)
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
