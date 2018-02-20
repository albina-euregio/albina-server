package eu.albina.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;

public class MapUtil {

	private static final Logger logger = LoggerFactory.getLogger(MapUtil.class);

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

	public static String triggerMapProduction(String caaml) throws AlbinaException {
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
			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
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
