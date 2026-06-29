package eu.albina.map;

import com.google.common.collect.Streams;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
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
import java.util.Collection;
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
	public Row fetch() {
		if (!featureIterator.hasNext()) {
			return null;
		}
		final Feature feature = featureIterator.next();
		final Row row = new Row();
		for (String fieldName : fieldNames) {
			if (GEOMETRY.equals(fieldName)) {
				row.add(new Argument(Argument.GEOMETRY_POLYGON, coordinates((Polygon) feature.getGeometry())));
			} else {
				row.add(new Argument(Argument.STRING, String.valueOf(feature.getProperties().get(fieldName))));
			}
		}
		return row;
	}

	/**
	 * @see org.mapyrus.Argument#createOGCWKT
	 */
	private static double[] coordinates(Polygon geometry) {
		return Stream.of(
			DoubleStream.of(Argument.GEOMETRY_POLYGON),
			DoubleStream.of(geometry.getCoordinates().stream().mapToInt(Collection::size).sum()),
			geometry.getCoordinates().stream().flatMapToDouble(GeoJsonDataset::coordinates),
			DoubleStream.of(0, 0, 0, 0)
		).reduce(DoubleStream.of(), DoubleStream::concat).toArray();
	}

	private static DoubleStream coordinates(List<LngLatAlt> lngLatAlts) {
		return Streams.mapWithIndex(lngLatAlts.stream(), GeoJsonDataset::coordinates).flatMapToDouble(x -> x);
	}

	private static DoubleStream coordinates(LngLatAlt c, long index) {
		return DoubleStream.of(
			index == 0 ? Argument.MOVETO : Argument.LINETO,
			c.getLongitude(),
			c.getLatitude()
		);
	}

	@Override
	public void close() throws MapyrusException {
		// do nothing
	}
}
