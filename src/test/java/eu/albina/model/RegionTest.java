package eu.albina.model;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.github.openjson.JSONObject;
import com.google.common.io.Resources;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class RegionTest {

	@Test
	public void toJSON() throws Exception {
		final URL resource = Resources.getResource("AT-07-08.geojson");
		final JSONObject expected = new JSONObject(Resources.toString(resource, StandardCharsets.UTF_8));

		final Region region = createRegion();
		final JSONObject actual = region.toJSON();
		assertEquals(expected.toString(4), actual.toString(4));
	}

	@Test
	public void readRegion() throws Exception {
		final Region expected = createRegion();
		final URL resource = Resources.getResource("AT-07-08.geojson");
		assertEquals(expected, Region.readRegion(resource));
	}

	private Region createRegion() throws Exception {
		final Region region = new Region();
		region.setId("AT-07-08");
		region.setNameDe("'Zentrale Lechtaler Alpen");
		region.setNameIt("Alpi della Lechtal centrali");
		region.setNameEn("Central Lechtal Alps");
		region.setPolygon((Polygon) new WKTReader().read("Polygon (( 10.52623 47.3312, 10.52702 47.33149, 10.52818 47.33197, 10.52826 47.332, 10.53162 47.32524, 10.53511 47.32324, 10.53595 47.32291, 10.55753 47.31456, 10.58469 47.30408, 10.59425 47.30227, 10.60086 47.2951, 10.60222 47.28393, 10.61406 47.28069, 10.61984 47.27911, 10.62802 47.26818, 10.62752 47.2579, 10.63128 47.25051, 10.63956 47.24692, 10.64333 47.24323, 10.64408 47.23287, 10.65613 47.23062, 10.66381 47.2259, 10.66528 47.22109, 10.66758 47.21359, 10.67251 47.20745, 10.6732 47.20361, 10.67268 47.20365, 10.67252 47.20366, 10.67047 47.2037, 10.6687 47.20347, 10.66651 47.20291, 10.66356 47.20162, 10.66081 47.20069, 10.65679 47.19929, 10.65517 47.19881, 10.65248 47.19801, 10.65089 47.1972, 10.64989 47.19669, 10.64858 47.19559, 10.64782 47.19448, 10.64759 47.19414, 10.64676 47.19233, 10.64603 47.19108, 10.64491 47.18967, 10.64451 47.18917, 10.64275 47.18771, 10.64132 47.18677, 10.63949 47.18602, 10.63624 47.18528, 10.63001 47.18434, 10.62483 47.18324, 10.62141 47.18214, 10.6196 47.18126, 10.61699 47.17964, 10.61366 47.17695, 10.60905 47.17416, 10.60763 47.1733, 10.60582 47.17219, 10.6033 47.17115, 10.60068 47.17024, 10.59691 47.16916, 10.59674 47.16911, 10.59562 47.16867, 10.59443 47.16802, 10.594 47.1677, 10.59306 47.16701, 10.59167 47.16545, 10.59009 47.16411, 10.58807 47.16312, 10.58466 47.1616, 10.58064 47.1601, 10.57948 47.1596, 10.57767 47.15884, 10.57527 47.15757, 10.57411 47.15676, 10.57369 47.15627, 10.57357 47.15554, 10.57358 47.15553, 10.57384 47.15489, 10.5747 47.15412, 10.57634 47.15351, 10.57815 47.15258, 10.57898 47.15163, 10.57892 47.15055, 10.57845 47.14982, 10.57699 47.14885, 10.57378 47.1477, 10.57349 47.1476, 10.56956 47.14626, 10.56635 47.14478, 10.56558 47.14411, 10.56534 47.14364, 10.56312 47.14334, 10.56136 47.14295, 10.55795 47.1419, 10.55501 47.14121, 10.55203 47.14076, 10.54906 47.14052, 10.5455 47.14044, 10.5433 47.14056, 10.54128 47.14038, 10.53822 47.13965, 10.53543 47.13874, 10.53174 47.13744, 10.528 47.13643, 10.525 47.1358, 10.52209 47.13533, 10.52045 47.13482, 10.51847 47.13423, 10.51822 47.13415, 10.51791 47.13411, 10.51692 47.13395, 10.51546 47.13387, 10.51356 47.13403, 10.51246 47.13403, 10.5113 47.13373, 10.51029 47.1332, 10.50901 47.13211, 10.50759 47.13101, 10.50623 47.13038, 10.50589 47.13022, 10.50499 47.12988, 10.50387 47.12947, 10.50147 47.12866, 10.49986 47.12771, 10.49894 47.12671, 10.49752 47.12503, 10.49671 47.12327, 10.4955 47.12104, 10.49463 47.12, 10.49377 47.11915, 10.49309 47.11852, 10.49232 47.11791, 10.49204 47.11765, 10.49197 47.1176, 10.49179 47.11714, 10.48974 47.11712, 10.48767 47.11712, 10.48404 47.11731, 10.48101 47.1178, 10.47808 47.11855, 10.47626 47.11943, 10.47416 47.12055, 10.47102 47.12162, 10.46757 47.12274, 10.46414 47.124, 10.46221 47.1248, 10.46079 47.12539, 10.45687 47.12658, 10.45416 47.12731, 10.45095 47.12787, 10.44653 47.12867, 10.44261 47.12928, 10.44079 47.12952, 10.43904 47.12975, 10.43655 47.12999, 10.4322 47.1305, 10.43034 47.13084, 10.42913 47.13123, 10.42728 47.13215, 10.4251 47.13352, 10.42346 47.13447, 10.42122 47.13551, 10.41961 47.13665, 10.41851 47.13785, 10.4178 47.13897, 10.41626 47.14057, 10.41512 47.14218, 10.41394 47.14344, 10.41266 47.14432, 10.41099 47.14514, 10.4097 47.14585, 10.40724 47.14704, 10.4051 47.14794, 10.40207 47.1487, 10.39826 47.14969, 10.39605 47.14996, 10.3938 47.14989, 10.39177 47.14969, 10.38999 47.14972, 10.3883 47.14995, 10.38824 47.14996, 10.38642 47.15052, 10.3845 47.15086, 10.38268 47.15084, 10.38048 47.15055, 10.3798 47.15702, 10.37661 47.16278, 10.37986 47.16803, 10.37959 47.1745, 10.38622 47.17995, 10.39128 47.18773, 10.40226 47.19108, 10.40764 47.19685, 10.41225 47.20722, 10.41324 47.21039, 10.4141 47.21314, 10.42166 47.22053, 10.42257 47.22157, 10.42708 47.2267, 10.43663 47.23071, 10.44186 47.23227, 10.44348 47.2346, 10.44243 47.23661, 10.43425 47.2379, 10.42384 47.24222, 10.41847 47.24444, 10.40982 47.25668, 10.40469 47.26632, 10.40447 47.27148, 10.40625 47.27199, 10.40891 47.27251, 10.41001 47.2728, 10.41135 47.27347, 10.41143 47.27351, 10.41267 47.27448, 10.41338 47.27549, 10.414 47.27727, 10.41476 47.27885, 10.41581 47.27998, 10.4178 47.28105, 10.41966 47.28167, 10.42146 47.2818, 10.42236 47.28178, 10.42346 47.28176, 10.42503 47.28163, 10.42693 47.28147, 10.42793 47.28144, 10.42897 47.28157, 10.43007 47.28176, 10.43387 47.28312, 10.43715 47.28422, 10.44038 47.28548, 10.44381 47.28707, 10.44666 47.28865, 10.44731 47.2891, 10.45079 47.29147, 10.45369 47.2936, 10.45536 47.29506, 10.45688 47.29603, 10.45887 47.29674, 10.46125 47.29709, 10.46254 47.29718, 10.46463 47.29732, 10.46719 47.29729, 10.47024 47.2968, 10.47314 47.29625, 10.47542 47.2959, 10.47718 47.2958, 10.47913 47.29606, 10.48065 47.29661, 10.48212 47.29735, 10.48492 47.29952, 10.48787 47.30172, 10.49011 47.30376, 10.49191 47.30563, 10.49301 47.30741, 10.49386 47.30909, 10.49414 47.30949, 10.49491 47.31061, 10.49662 47.31226, 10.49938 47.31462, 10.50237 47.31649, 10.50484 47.31779, 10.50689 47.31866, 10.5105 47.32002, 10.5144 47.3215, 10.51639 47.32239, 10.51808 47.32331, 10.51974 47.32435, 10.52129 47.32564, 10.52245 47.32671, 10.52357 47.32782, 10.52436 47.32865, 10.52493 47.32944, 10.52543 47.3303, 10.52552 47.33044, 10.52578 47.3308, 10.52623 47.3312 ))"));
		return region;
	}
}
