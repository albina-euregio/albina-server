package eu.albina.map;

import com.google.common.collect.Streams;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiLineString;
import org.geojson.MultiPoint;
import org.geojson.MultiPolygon;
import org.geojson.Point;
import org.geojson.Polygon;
import org.mapyrus.Argument;
import org.mapyrus.MapyrusException;
import org.mapyrus.Row;
import org.mapyrus.dataset.GeographicDataset;
import tools.jackson.databind.ObjectMapper;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class GeoJsonDataset implements GeographicDataset {

	private static final String GEOMETRY = "GEOMETRY";
	private final Iterator<Feature> featureIterator;
	private final String[] fieldNames;

	public GeoJsonDataset(FeatureCollection featureCollection) {
		this.featureIterator = featureCollection.getFeatures().iterator();
		this.fieldNames = Stream.concat(
			featureCollection.getFeatures().get(0).getProperties().keySet().stream(),
			Stream.of(GEOMETRY)
		).toArray(String[]::new);
	}

	public static GeoJsonDataset of(Path path) throws IOException {
		try (InputStream src = Files.newInputStream(path)) {
			System.out.println("Reading " + path);
			FeatureCollection featureCollection = new ObjectMapper().readValue(src, FeatureCollection.class);
			return new GeoJsonDataset(featureCollection);
		}
	}

	@Override
	public String getProjection() {
		return "";
	}

	@Override
	public Hashtable<String, String> getMetadata() {
		return new Hashtable<>();
	}

	@Override
	public String[] getFieldNames() {
		return fieldNames;
	}

	@Override
	public Rectangle2D.Double getWorlds() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * @see org.mapyrus.Argument#createOGCWKT
	 */
	public Row fetch() {
		if (!featureIterator.hasNext()) {
			return null;
		}
		final Feature feature = featureIterator.next();
		final Row row = new Row();
		for (String fieldName : fieldNames) {
			if (GEOMETRY.equals(fieldName)) {
				System.out.println(feature.getProperties());
				row.add(new Argument(Argument.GEOMETRY_POLYGON, switch (feature.getGeometry()) {
					case MultiPolygon multiPolygon -> coordinates(multiPolygon);
					case Polygon polygon -> coordinates(polygon);
					case MultiLineString multiLineString -> coordinates(multiLineString);
					case LineString lineString -> coordinates(lineString);
					case MultiPoint multiPoint -> coordinates(multiPoint);
					case Point point -> coordinates(point);
					default -> throw new UnsupportedOperationException(feature.getGeometry().getClass().toString());
				}));
			} else {
				row.add(new Argument(Argument.STRING, String.valueOf(feature.getProperties().get(fieldName))));
			}
		}
		return row;
	}

	private double[] coordinates(Point geometry) {
		List<LngLatAlt> coordinates = List.of(geometry.getCoordinates());
		return DoubleStream.concat(
			DoubleStream.of(Argument.GEOMETRY_POINT, sizeL(coordinates)),
			coordinatesL(coordinates)
		).toArray();
	}

	private double[] coordinates(MultiPoint geometry) {
		List<LngLatAlt> coordinates = geometry.getCoordinates();
		return DoubleStream.concat(
			DoubleStream.of(Argument.GEOMETRY_MULTIPOINT, sizeL(coordinates)),
			coordinates.stream().flatMapToDouble(c -> Arrays.stream(coordinates(new Point(c))))
		).toArray();
	}

	private double[] coordinates(LineString geometry) {
		List<LngLatAlt> coordinates = geometry.getCoordinates();
		return DoubleStream.concat(
			DoubleStream.of(Argument.GEOMETRY_LINESTRING, sizeL(coordinates)),
			coordinatesL(coordinates)
		).toArray();
	}

	private double[] coordinates(MultiLineString geometry) {
		List<List<LngLatAlt>> coordinates = geometry.getCoordinates();
		return DoubleStream.concat(
			DoubleStream.of(Argument.GEOMETRY_MULTILINESTRING, sizeLL(coordinates)),
			coordinatesLL(coordinates)
		).toArray();
	}

	private static double[] coordinates(MultiPolygon geometry) {
		List<List<List<LngLatAlt>>> coordinates = geometry.getCoordinates();
		return DoubleStream.concat(
			DoubleStream.of(Argument.GEOMETRY_POLYGON, sizeLLL(coordinates)),
			DoubleStream.concat(coordinatesLLL(coordinates), DoubleStream.generate(() -> 0.0).limit(2L * coordinates.size()))
		).toArray();
	}

	private static double[] coordinates(Polygon geometry) {
		List<List<LngLatAlt>> coordinates = geometry.getCoordinates();
		return DoubleStream.concat(
			DoubleStream.of(Argument.GEOMETRY_POLYGON, sizeLL(coordinates)),
			DoubleStream.concat(coordinatesLL(coordinates), DoubleStream.generate(() -> 0.0).limit(2L * coordinates.size()))
		).toArray();
	}

	private static DoubleStream coordinatesLLL(List<List<List<LngLatAlt>>> coordinates) {
		return coordinates.stream().flatMapToDouble(GeoJsonDataset::coordinatesLL);
	}

	private static DoubleStream coordinatesLL(List<List<LngLatAlt>> coordinates) {
		return coordinates.stream().flatMapToDouble(GeoJsonDataset::coordinatesL);
	}

	private static DoubleStream coordinatesL(List<LngLatAlt> lngLatAlts) {
		return Streams.mapWithIndex(lngLatAlts.stream(), GeoJsonDataset::coordinates).flatMapToDouble(x -> x);
	}

	private static DoubleStream coordinates(LngLatAlt c, long index) {
		return DoubleStream.of(
			index == 0 ? Argument.MOVETO : Argument.LINETO,
			c.getLongitude(),
			c.getLatitude()
		);
	}

	private static int sizeLLL(List<List<List<LngLatAlt>>> coordinates) {
		return coordinates.stream().mapToInt(GeoJsonDataset::sizeLL).sum();
	}

	private static int sizeLL(List<List<LngLatAlt>> coordinates) {
		return coordinates.stream().mapToInt(GeoJsonDataset::sizeL).sum();
	}

	private static int sizeL(List<LngLatAlt> coordinates) {
		return coordinates.size();
	}

	@Override
	public void close() throws MapyrusException {
		// do nothing
	}
}
