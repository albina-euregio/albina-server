package eu.albina.map;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.Region;
import eu.albina.model.Regions;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.util.GeoJson;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.precision.GeometryPrecisionReducer;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public interface BulletinRegions {
	static void createBulletinRegions(List<AvalancheBulletin> bulletins, Regions regions) {
		final Path outputDirectory = MapUtil.getOutputDirectory(bulletins);
		for (DaytimeDependency daytimeDependency : DaytimeDependency.of(bulletins)) {
			try {
				final Path regionFile = outputDirectory.resolve(daytimeDependency + "_regions.json");
				MapUtil.logger.info("Creating region file {}", regionFile);
				createBulletinRegions(bulletins, daytimeDependency, regionFile, regions);
			} catch (IOException | AlbinaException ex) {
				throw new AlbinaMapException("Failed to create region file", ex);
			}
		}
	}

	static void createBulletinRegions(List<AvalancheBulletin> bulletins, DaytimeDependency daytimeDependency,
									  Path regionFile, Regions regions) throws IOException, AlbinaException {
		final GeoJson.FeatureCollection featureCollection = new GeoJson.FeatureCollection();
		if (bulletins.get(0).getPublicationDate() != null) {
			featureCollection.properties.put("creation_date", DateTimeFormatter.ISO_INSTANT.format(bulletins.get(0).getPublicationDate()));
		}
		featureCollection.properties.put("valid_date", bulletins.get(0).getValidityDateString());
		featureCollection.properties.put("valid_daytime", daytimeDependency.name());
		for (AvalancheBulletin bulletin : bulletins) {
			final GeoJson.Feature feature = new GeoJson.Feature();
			final AvalancheBulletinDaytimeDescription description = daytimeDependency.getBulletinDaytimeDescription(bulletin);
			feature.properties.put("bid", bulletin.getId());
			feature.properties.put("daytime", daytimeDependency.name());
			feature.properties.put("elevation", description.getElevation());
			feature.properties.put("dl_hi", DangerRating.getString(description.dangerRating(true)));
			feature.properties.put("dl_lo", DangerRating.getString(description.dangerRating(false)));
			feature.properties.put("problem_1", eu.albina.model.enumerations.AvalancheSituation.toCaamlv5String(description.getAvalancheSituation1()));
			feature.properties.put("problem_2", eu.albina.model.enumerations.AvalancheSituation.toCaamlv5String(description.getAvalancheSituation2()));
			final double bufferDistance = 1e-4;
			feature.geometry = regions.getRegionsForBulletin(bulletin, false).map(Region::getPolygon)
					// use buffer to avoid artifacts when building polygon union
					.map(polygon -> BufferOp.bufferOp(polygon, bufferDistance))
					.collect(Collectors.collectingAndThen(Collectors.toList(), UnaryUnionOp::union));
			feature.geometry = BufferOp.bufferOp(feature.geometry, -bufferDistance);
			// round coordinates to 4 decimal digits in order to reduce the file size
			feature.geometry = GeometryPrecisionReducer.reduce(feature.geometry, new PrecisionModel(1e4));
			featureCollection.features.add(feature);
		}
		new ObjectMapper().writeValue(regionFile.toFile(), featureCollection);
	}
}
