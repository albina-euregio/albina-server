package eu.albina.util;

import org.joda.time.DateTime;
import org.json.JSONObject;

import eu.albina.model.enumerations.BulletinStatus;

public class JsonUtil {

	// private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

	public static JSONObject createRegionHeaderJson() {
		JSONObject json = new JSONObject();
		json.put("type", "FeatureCollection");
		JSONObject crs = new JSONObject();
		crs.put("type", "name");
		JSONObject properties = new JSONObject();
		properties.put("name", GlobalVariables.referenceSystemUrn);
		crs.put("properties", properties);
		json.put("crs", crs);
		return json;
	}

	public static JSONObject createBulletinStatusUpdateJson(String region, DateTime date, BulletinStatus status) {
		JSONObject json = new JSONObject();

		if (region != null && region != "")
			json.put("region", region);
		if (date != null)
			json.put("date", date.toString(GlobalVariables.formatterDateTime));
		if (status != null)
			json.put("status", status);

		return json;
	}

}
