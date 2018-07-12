package eu.albina.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;

public class GlobalVariables {

	public static String localImagesPath = "images/";

	// TODO for testing
	public static String pdfDirectory = "D:\\";
	public static String serverImagesUrl = "https://natlefs.snowobserver.com/images/";
	public static String mapsPath = "D:\\norbert\\workspaces\\albina-euregio\\albina-server\\src\\main\\resources\\images\\";

	public static String emailUsername = "norbert.lanzanasto@gmail.com";
	public static String emailPassword = "Go6Zaithee";

	public static String[] daysDe = { "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag" };
	public static String[] daysIt = { "Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica" };
	public static String[] daysEn = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

	public static DateTimeFormatter formatterDateTime = ISODateTimeFormat.dateTimeNoMillis();
	public static DateTimeFormatter formatterDate = ISODateTimeFormat.date();
	public static DateTimeFormatter parserDateTime = ISODateTimeFormat.dateTimeParser();
	public static DateTimeFormatter dateTimeEn = DateTimeFormat.forPattern(" dd.MM.yyyy");
	public static DateTimeFormatter dateTimeDe = DateTimeFormat.forPattern(" dd.MM.yyyy");
	public static DateTimeFormatter dateTimeIt = DateTimeFormat.forPattern(" dd.MM.yyyy");
	public static DateTimeFormatter publicationDateTimeEn = DateTimeFormat.forPattern("dd.MM.yyyy, hh:mm aa");
	public static DateTimeFormatter publicationDateTimeDe = DateTimeFormat.forPattern("dd.MM.yyyy, HH:mm");
	public static DateTimeFormatter publicationDateTimeIt = DateTimeFormat.forPattern("dd.MM.yyyy, HH:mm");

	public static String codeTrentino = "IT-32-TN";
	public static String codeSouthTyrol = "IT-32-BZ";
	public static String codeTyrol = "AT-07";
	public static String codeStyria = "AT-06";

	public static List<String> regions = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add("AT-07");
			add("IT-32-BZ");
			add("IT-32-TN");
			add("AT-06");
		}
	};

	public static List<LanguageCode> languages = new ArrayList<LanguageCode>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add(LanguageCode.de);
			add(LanguageCode.it);
			add(LanguageCode.en);
		}
	};

	public static String avalancheReportUsername = "info@avalanche.report";

	// TODO create secret
	public static String tokenEncodingSecret = "secret";
	public static String tokenEncodingIssuer = "albina";
	public static long accessTokenExpirationDuration = 1000 * 60 * 60 * 24;
	public static long refreshTokenExpirationDuration = 1000 * 60 * 60 * 24 * 7;
	public static long confirmationTokenExpirationDuration = 1000 * 60 * 60 * 24 * 3;

	public static String referenceSystemUrn = "urn:ogc:def:crs:OGC:1.3:CRS84";
	// public static String referenceSystemUrn = "EPSG:32632";
	public static String bulletinCaamlSchemaFileString = "http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS/CAAMLv5_BulletinEAWS.xsd";

	public static String univieMapProductionUrl = "http://data1.geo.univie.ac.at/projects/albina/tools/create_trentino_maps/create_trentino_maps.php";

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

	public static String getDayName(int day, LanguageCode lang) {
		if (day < 8) {
			switch (lang) {
			case de:
				return daysDe[day - 1];
			case it:
				return daysIt[day - 1];
			case en:
				return daysEn[day - 1];
			default:
				return daysEn[day - 1];
			}
		} else
			return "";
	}

	public static String getTreelineString(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Waldgrenze";
		case it:
			return "Linea del bosco";
		case en:
			return "Treeline";
		default:
			return "Treeline";
		}
	}

	public static String getPublishedText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Publiziert ";
		case it:
			return "Pubblicato ";
		case en:
			return "Published ";
		default:
			return "Published ";
		}
	}

	public static String getTendencyHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Tendenz";
		case it:
			return "Tendenza";
		case en:
			return "Tendency";
		default:
			return "Tendency";
		}
	}

	public static String getTendencyText(Tendency tendency, LanguageCode lang) {
		if (tendency != null) {
			switch (tendency) {
			case increasing:
				switch (lang) {
				case de:
					return "Tendenz: Lawinengefahr steigt";
				case it:
					return "Tendenza: Pericolo valanghe in aumento";
				case en:
					return "Tendency: Avalanche danger increasing";
				default:
					return "Tendency: Avalanche danger increasing";
				}
			case steady:
				switch (lang) {
				case de:
					return "Tendenz: Lawinengefahr bleibt gleich";
				case it:
					return "Tendenza: Pericolo valanghe stabile";
				case en:
					return "Tendency: Avalanche danger stays the same";
				default:
					return "Tendency: Avalanche danger stays the same";
				}
			case decreasing:
				switch (lang) {
				case de:
					return "Tendenz: Lawinengefahr nimmt ab";
				case it:
					return "Tendenza: Pericolo valanghe in diminuazione";
				case en:
					return "Tendency: Avalanche danger decreasing";
				default:
					return "Tendency: Avalanche danger decreasing";
				}
			default:
				return "";
			}
		} else
			return "";
	}

	public static String getDangerPatternsHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Gefahrenmuster";
		case it:
			return "Situazioni tipo";
		case en:
			return "Danger patterns";
		default:
			return "Danger patterns";
		}
	}

	public static String getSnowpackHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Schneedecke";
		case it:
			return "Descrizione struttura manto nevoso";
		case en:
			return "Snowpack";
		default:
			return "Snowpack";
		}
	}

	public static String getTitle(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinen.report";
		case it:
			return "Valanghe.report";
		case en:
			return "Avalanche.report";
		default:
			return "Avalanche.report";
		}
	}

	public static String getHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinenvorhersage";
		case it:
			return "Previsione Valanghe";
		case en:
			return "Avalanche Forecast";
		default:
			return "Avalanche Forecast";
		}
	}

	public static String getFollowUs(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Folge uns";
		case it:
			return "Seguici";
		case en:
			return "Follow us";
		default:
			return "Follow us";
		}
	}

	public static String getUnsubscribe(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Abmelden";
		case it:
			return "Annullare l'iscrizione";
		case en:
			return "Unsubscribe";
		default:
			return "Unsubscribe";
		}
	}

	public static String getDangerRatingText(DangerRating dangerRating, LanguageCode lang) {
		switch (dangerRating) {
		case low:
			switch (lang) {
			case de:
				return "gering";
			case it:
				return "debole";
			case en:
				return "low";
			default:
				return "low";
			}
		case moderate:
			switch (lang) {
			case de:
				return "mäßig";
			case it:
				return "moderato";
			case en:
				return "moderate";
			default:
				return "moderate";
			}
		case considerable:
			switch (lang) {
			case de:
				return "erheblich";
			case it:
				return "marcato";
			case en:
				return "considerable";
			default:
				return "considerable";
			}
		case high:
			switch (lang) {
			case de:
				return "groß";
			case it:
				return "forte";
			case en:
				return "high";
			default:
				return "high";
			}
		case very_high:
			switch (lang) {
			case de:
				return "sehr groß";
			case it:
				return "molto forte";
			case en:
				return "very high";
			default:
				return "very high";
			}
		case missing:
			switch (lang) {
			case de:
				return "fehlt";
			case it:
				return "mancha";
			case en:
				return "missing";
			default:
				return "missing";
			}
		case no_rating:
			switch (lang) {
			case de:
				return "keine Beurteilung";
			case it:
				return "senza valutazione";
			case en:
				return "no rating";
			default:
				return "no rating";
			}
		default:
			switch (lang) {
			case de:
				return "fehlt";
			case it:
				return "mancha";
			case en:
				return "missing";
			default:
				return "missing";
			}
		}
	}
}
