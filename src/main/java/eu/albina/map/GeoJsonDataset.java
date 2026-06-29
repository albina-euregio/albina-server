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
		// geodata.Euregio/micro_regions_elevation_a_simplified.prj
		// PROJCS["Google_Maps_Global_Mercator",           GEOGCS["GCS_WGS_1984",DATUM["D_WGS_1984",SPHEROID["WGS_1984",6378137.0,298.257223563]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]],PROJECTION["Mercator"],                 PARAMETER["False_Easting",0.0],PARAMETER["False_Northing",0.0],PARAMETER["Central_Meridian",0.0],PARAMETER["Standard_Parallel_1",0.0],                                       UNIT["Meter",1.0]]
		// https://epsg.io/900913
		// PROJCS["Google_Maps_Global_Mercator",           GEOGCS["GCS_WGS_1984",DATUM["D_WGS_1984",SPHEROID["WGS_1984",6378137.0,298.257223563]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]],PROJECTION["Mercator_Auxiliary_Sphere"],PARAMETER["False_Easting",0.0],PARAMETER["False_Northing",0.0],PARAMETER["Central_Meridian",0.0],PARAMETER["Standard_Parallel_1",0.0],PARAMETER["Auxiliary_Sphere_Type",0.0],UNIT["Meter",1.0]]
		// https://epsg.io/3857
		// PROJCS["WGS_1984_Web_Mercator_Auxiliary_Sphere",GEOGCS["GCS_WGS_1984",DATUM["D_WGS_1984",SPHEROID["WGS_1984",6378137.0,298.257223563]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]],PROJECTION["Mercator_Auxiliary_Sphere"],PARAMETER["False_Easting",0.0],PARAMETER["False_Northing",0.0],PARAMETER["Central_Meridian",0.0],PARAMETER["Standard_Parallel_1",0.0],PARAMETER["Auxiliary_Sphere_Type",0.0],UNIT["Meter",1.0]]
		return "GEOGCS[\"wgs84\",DATUM[\"WGS_1984\",SPHEROID[\"wgs84\",6378137,298.257223563],TOWGS84[0.000,0.000,0.000]],PRIMEM[\"Greenwich\",0],UNIT[\"degree\",0.0174532925199433]]";
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
