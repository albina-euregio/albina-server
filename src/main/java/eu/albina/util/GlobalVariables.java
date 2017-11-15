package eu.albina.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Scanner;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class GlobalVariables {

	public static String imagesDir = "/images";
	public static DateTimeFormatter formatterDateTime = ISODateTimeFormat.dateTimeNoMillis();
	public static DateTimeFormatter formatterDate = ISODateTimeFormat.date();
	public static DateTimeFormatter parserDateTime = ISODateTimeFormat.dateTimeParser();
	public static int paginationCount = 50;
	public static String urlSeperator = "_";
	public static String urlStringForenoon = "am";
	public static String urlStringAfternoon = "pm";
	public static String urlStringFullday = "fullday";
	public static String fileExtensionJpg = "jpg";

	// TODO create secret
	public static String tokenEncodingSecret = "secret";
	public static String tokenEncodingIssuer = "albina";
	public static long accessTokenExpirationDuration = 1000 * 60 * 60 * 24;
	public static long refreshTokenExpirationDuration = 1000 * 60 * 60 * 24 * 7;

	public static String referenceSystemUrn = "urn:ogc:def:crs:OGC:1.3:CRS84";
	// public static String referenceSystemUrn = "EPSG:32632";
	public static String bulletinCaamlSchemaFileString = "http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS/CAAMLv5_BulletinEAWS.xsd";

	public static String univieBaseUrl = "https://data1.geo.univie.ac.at/exchange/albina/trentino/awm/";
	public static String univieMapProductionUrl = "http://data1.geo.univie.ac.at/projects/albina/tools/create_trentino_maps/create_trentino_maps_test.php";

	public static String getJsonSchemaFileString(String fileName) throws FileNotFoundException {
		StringBuilder result = new StringBuilder("");

		URL resource = GlobalVariables.class.getResource("/" + fileName + ".json");
		File file = new File(resource.getFile());

		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			result.append(line).append("\n");
		}
		scanner.close();

		return result.toString();
	}
}
