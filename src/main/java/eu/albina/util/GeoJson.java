package eu.albina.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.locationtech.jts.geom.Geometry;
import org.n52.jackson.datatype.jts.GeometryDeserializer;
import org.n52.jackson.datatype.jts.GeometrySerializer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeoJson {

	private GeoJson() {
	}

	public static class FeatureCollection {
		public String type = "FeatureCollection";

		public Map<String, Object> properties = new LinkedHashMap<>();

		public List<Feature> features = new ArrayList<>();
	}

	public static class Feature {
		public String type = "Feature";

		public Map<String, Object> properties = new LinkedHashMap<>();

		@JsonSerialize(using = GeometrySerializer.class)
		@JsonDeserialize(contentUsing = GeometryDeserializer.class)
		public Geometry geometry;

	}
}
