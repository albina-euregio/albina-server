package eu.albina.model;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.google.common.io.Resources;

import eu.albina.util.GlobalVariables;

/**
 * A list of {@link Region}s
 */
public class Regions extends ArrayList<Region> implements AvalancheInformationObject {

	private static final long serialVersionUID = 1L;

	public Regions() {
	}

	public Regions(Collection<? extends Region> c) {
		super(c);
	}

	public Stream<Region> getRegionsForBulletin(AvalancheBulletin bulletin, boolean preview) {
		if (preview) {
			return stream()
					.flatMap(region -> Stream.concat(Stream.of(region), region.getSubregions().stream()))
					.filter(region -> (bulletin.getPublishedAndSavedRegions().contains(region.getId())));
		} else {
			return stream()
					.flatMap(region -> Stream.concat(Stream.of(region), region.getSubregions().stream()))
					.filter(region -> bulletin.getPublishedRegions().contains(region.getId()));
		}
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("type", "FeatureCollection");
		JSONObject crs = new JSONObject();
		crs.put("type", "name");
		JSONObject properties = new JSONObject();
		properties.put("name", GlobalVariables.referenceSystemUrn);
		crs.put("properties", properties);
		json.put("crs", crs);

		JSONArray features = new JSONArray();
		for (Region entry : this) {
			features.put(entry.toJSON());
		}
		json.put("features", features);
		return json;
	}

	public static Regions readRegions(final URL resource) throws IOException {
		final String string = Resources.toString(resource, StandardCharsets.UTF_8);
		final JSONObject object = new JSONObject(string);
		if (!"FeatureCollection".equals(object.getString("type"))) {
			throw new IllegalArgumentException("Expecting type=FeatureCollection");
		}
		final JSONArray features = object.getJSONArray("features");
		final Regions regions = new Regions();
		IntStream.range(0, features.length()).mapToObj(features::getJSONObject).map(Region::new).forEach(regions::add);
		return regions;
	}
}
