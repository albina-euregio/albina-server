package eu.albina.util;

import eu.albina.model.AvalancheBulletin;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

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

    @Test
    @Ignore("requires albina-euregio/avalanche-warning-maps")
    public void testMapyrusMaps() throws Exception {
        final InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("2019-01-17.json");
        final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
        MapUtil.createMapyrusMaps(bulletins);
    }

    @Test
    public void testMayrusInput() {
        final InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("2019-01-17.json");
        final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
        final String input = MapUtil.createMayrusInput(bulletins);
        assertEquals("" +
                "sys_bid;bid;region;date;am_pm;validelevation;dr_h;dr_l;aspect_h;aspect_l;avprob_h;avprob_l\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-10;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-11;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-01;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-12;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-24;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-13;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-02;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-03;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-04;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-26;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-05;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-17;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-06;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-18;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-07;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "0;6385c958-018d-4c89-aa67-5eddc31ada5a;AT-07-08;2019-01-17;fd;0;3;3;0;0;0;0\n" +
                "1;cd6faf0f-5188-4904-91cc-524c3a446b0c;AT-07-27;2019-01-17;fd;1600;3;2;0;0;0;0\n" +
                "1;cd6faf0f-5188-4904-91cc-524c3a446b0c;AT-07-28;2019-01-17;fd;1600;3;2;0;0;0;0\n" +
                "1;cd6faf0f-5188-4904-91cc-524c3a446b0c;AT-07-25;2019-01-17;fd;1600;3;2;0;0;0;0\n" +
                "2;f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd;AT-07-15;2019-01-17;fd;2200;3;2;0;0;0;0\n" +
                "2;f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd;AT-07-16;2019-01-17;fd;2200;3;2;0;0;0;0\n" +
                "2;f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd;AT-07-19;2019-01-17;fd;2200;3;2;0;0;0;0\n" +
                "2;f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd;AT-07-09;2019-01-17;fd;2200;3;2;0;0;0;0\n" +
                "2;f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd;AT-07-20;2019-01-17;fd;2200;3;2;0;0;0;0\n" +
                "2;f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd;AT-07-21;2019-01-17;fd;2200;3;2;0;0;0;0\n" +
                "2;f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd;AT-07-22;2019-01-17;fd;2200;3;2;0;0;0;0\n" +
                "2;f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd;AT-07-23;2019-01-17;fd;2200;3;2;0;0;0;0\n" +
                "2;f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd;AT-07-14;2019-01-17;fd;2200;3;2;0;0;0;0\n" +
                "3;853733e5-cb48-4cf1-91a2-ebde59dda31f;IT-32-TN-02;2019-01-17;fd;2500;2;1;0;0;0;0\n" +
                "3;853733e5-cb48-4cf1-91a2-ebde59dda31f;IT-32-TN-13;2019-01-17;fd;2500;2;1;0;0;0;0\n" +
                "3;853733e5-cb48-4cf1-91a2-ebde59dda31f;IT-32-TN-01;2019-01-17;fd;2500;2;1;0;0;0;0\n" +
                "3;853733e5-cb48-4cf1-91a2-ebde59dda31f;IT-32-TN-04;2019-01-17;fd;2500;2;1;0;0;0;0\n" +
                "3;853733e5-cb48-4cf1-91a2-ebde59dda31f;IT-32-TN-05;2019-01-17;fd;2500;2;1;0;0;0;0\n" +
                "3;853733e5-cb48-4cf1-91a2-ebde59dda31f;IT-32-TN-16;2019-01-17;fd;2500;2;1;0;0;0;0\n" +
                "3;853733e5-cb48-4cf1-91a2-ebde59dda31f;IT-32-TN-19;2019-01-17;fd;2500;2;1;0;0;0;0\n" +
                "3;853733e5-cb48-4cf1-91a2-ebde59dda31f;IT-32-TN-07;2019-01-17;fd;2500;2;1;0;0;0;0\n" +
                "3;853733e5-cb48-4cf1-91a2-ebde59dda31f;IT-32-TN-09;2019-01-17;fd;2500;2;1;0;0;0;0\n" +
                "3;853733e5-cb48-4cf1-91a2-ebde59dda31f;IT-32-TN-20;2019-01-17;fd;2500;2;1;0;0;0;0\n" +
                "4;2683b4d4-a4d3-48e0-ac4b-1e8a04676925;IT-32-BZ-14;2019-01-17;fd;0;3;1;0;0;0;0\n" +
                "5;24fce71f-ad60-47d6-b607-d7cefca5ba3f;IT-32-TN-12;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "5;24fce71f-ad60-47d6-b607-d7cefca5ba3f;IT-32-TN-15;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "5;24fce71f-ad60-47d6-b607-d7cefca5ba3f;IT-32-TN-03;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "5;24fce71f-ad60-47d6-b607-d7cefca5ba3f;IT-32-TN-14;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "5;24fce71f-ad60-47d6-b607-d7cefca5ba3f;IT-32-TN-17;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "5;24fce71f-ad60-47d6-b607-d7cefca5ba3f;IT-32-TN-06;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "5;24fce71f-ad60-47d6-b607-d7cefca5ba3f;IT-32-TN-08;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "5;24fce71f-ad60-47d6-b607-d7cefca5ba3f;IT-32-TN-18;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "5;24fce71f-ad60-47d6-b607-d7cefca5ba3f;IT-32-TN-11;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "5;24fce71f-ad60-47d6-b607-d7cefca5ba3f;IT-32-TN-10;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "5;24fce71f-ad60-47d6-b607-d7cefca5ba3f;IT-32-TN-21;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "6;67e2d91b-2335-45f5-abad-1dc48b86c777;IT-32-BZ-16;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "6;67e2d91b-2335-45f5-abad-1dc48b86c777;IT-32-BZ-17;2019-01-17;fd;0;1;1;0;0;0;0\n" +
                "7;0d9c6af5-53cc-4c80-8901-065a3f5b3195;IT-32-BZ-07;2019-01-17;fd;0;3;2;0;0;0;0\n" +
                "7;0d9c6af5-53cc-4c80-8901-065a3f5b3195;IT-32-BZ-09;2019-01-17;fd;0;3;2;0;0;0;0\n" +
                "7;0d9c6af5-53cc-4c80-8901-065a3f5b3195;IT-32-BZ-10;2019-01-17;fd;0;3;2;0;0;0;0\n" +
                "7;0d9c6af5-53cc-4c80-8901-065a3f5b3195;IT-32-BZ-11;2019-01-17;fd;0;3;2;0;0;0;0\n" +
                "7;0d9c6af5-53cc-4c80-8901-065a3f5b3195;IT-32-BZ-12;2019-01-17;fd;0;3;2;0;0;0;0\n" +
                "7;0d9c6af5-53cc-4c80-8901-065a3f5b3195;IT-32-BZ-01;2019-01-17;fd;0;3;2;0;0;0;0\n" +
                "7;0d9c6af5-53cc-4c80-8901-065a3f5b3195;IT-32-BZ-13;2019-01-17;fd;0;3;2;0;0;0;0\n" +
                "7;0d9c6af5-53cc-4c80-8901-065a3f5b3195;IT-32-BZ-02;2019-01-17;fd;0;3;2;0;0;0;0\n" +
                "7;0d9c6af5-53cc-4c80-8901-065a3f5b3195;IT-32-BZ-03;2019-01-17;fd;0;3;2;0;0;0;0\n" +
                "7;0d9c6af5-53cc-4c80-8901-065a3f5b3195;IT-32-BZ-04;2019-01-17;fd;0;3;2;0;0;0;0\n" +
                "7;0d9c6af5-53cc-4c80-8901-065a3f5b3195;IT-32-BZ-05;2019-01-17;fd;0;3;2;0;0;0;0\n" +
                "7;0d9c6af5-53cc-4c80-8901-065a3f5b3195;IT-32-BZ-06;2019-01-17;fd;0;3;2;0;0;0;0\n" +
                "8;5bf03ed5-7cac-493b-aad4-f2bc4cb4ba34;AT-07-29;2019-01-17;fd;2000;2;1;0;0;0;0\n" +
                "9;9d365268-1218-49b8-b05b-e4cc6de13402;IT-32-BZ-18;2019-01-17;fd;2000;2;1;0;0;0;0\n" +
                "9;9d365268-1218-49b8-b05b-e4cc6de13402;IT-32-BZ-19;2019-01-17;fd;2000;2;1;0;0;0;0\n" +
                "9;9d365268-1218-49b8-b05b-e4cc6de13402;IT-32-BZ-08;2019-01-17;fd;2000;2;1;0;0;0;0\n" +
                "9;9d365268-1218-49b8-b05b-e4cc6de13402;IT-32-BZ-20;2019-01-17;fd;2000;2;1;0;0;0;0\n" +
                "9;9d365268-1218-49b8-b05b-e4cc6de13402;IT-32-BZ-15;2019-01-17;fd;2000;2;1;0;0;0;0", input);
    }
}
