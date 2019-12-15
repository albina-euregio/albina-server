package eu.albina.util;

import com.bedatadriven.jackson.datatype.jts.serialization.GeometryDeserializer;
import com.bedatadriven.jackson.datatype.jts.serialization.GeometrySerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Geometry;

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
