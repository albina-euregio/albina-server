package eu.albina.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapUtilTest {

    @Test
    public void testOverviewMapFilename() {
        assertEquals("fd_albina_map.jpg", MapUtil.getOverviewMapFilename(null, false, false, false));
        assertEquals("fd_tyrol_map.jpg", MapUtil.getOverviewMapFilename("AT-07", false, false, false));
        assertEquals("fd_tyrol_map_bw.jpg", MapUtil.getOverviewMapFilename("AT-07", false, false, true));
        assertEquals("am_tyrol_map.jpg", MapUtil.getOverviewMapFilename("AT-07", false, true, false));
        assertEquals("pm_tyrol_map.jpg", MapUtil.getOverviewMapFilename("AT-07", true, true, false));
        assertEquals("fd_southtyrol_map_bw.jpg", MapUtil.getOverviewMapFilename("IT-32-BZ", false, false, true));
        assertEquals("fd_trentino_map_bw.jpg", MapUtil.getOverviewMapFilename("IT-32-TN", false, false, true));
    }
}
