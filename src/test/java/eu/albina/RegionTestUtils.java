package eu.albina;

import com.google.common.io.Resources;
import eu.albina.model.Region;

public interface RegionTestUtils {

	Region regionEuregio = Region.readRegion(Resources.getResource("region_EUREGIO.json"));
	Region regionTyrol = Region.readRegion(Resources.getResource("region_AT-07.json"));
	Region regionSouthTyrol = Region.readRegion(Resources.getResource("region_IT-32-BZ.json"));
	Region regionTrentino = Region.readRegion(Resources.getResource("region_IT-32-TN.json"));
	Region regionAran = Region.readRegion(Resources.getResource("region_ES-CT-L.json"));

}
