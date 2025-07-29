package eu.albina.util;

import com.google.common.io.MoreFiles;
import eu.albina.RegionTestUtils;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.rest.MediaFileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RssUtilTest {

	@Test
	public void rss(@TempDir Path folder) throws Exception {
		String websiteName = "Lawinen.report";
		ServerInstance serverInstance = new ServerInstance();
		serverInstance.setMediaPath(folder.resolve("media_files").toString());
		Path directory = MediaFileService.getMediaPath(serverInstance, RegionTestUtils.regionTyrol, LanguageCode.de);
		Files.createDirectories(directory);
		MoreFiles.touch(directory.resolve("2020-12-12.mp3"));
		System.out.println(directory.resolve("2020-12-12.mp3").toAbsolutePath());
		MoreFiles.touch(directory.resolve("2022-12-14.mp3"));
		Files.setLastModifiedTime(directory.resolve("2020-12-12.mp3"), FileTime.from(Instant.parse("2020-12-12T17:30:00Z")));
		Files.setLastModifiedTime(directory.resolve("2022-12-14.mp3"), FileTime.from(Instant.parse("2022-12-14T17:45:00Z")));
		final String rss = RssUtil.getRss(LanguageCode.de, new Region("AT-07"), directory, websiteName).replace("\r\n", "\n");
		assertEquals("" +
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
			"<rss version=\"2.0\">\n" +
			"  <channel>\n" +
			"    <title>albina media files</title>\n" +
			"    <description>albina media files</description>\n" +
			"    <language>de</language>\n" +
			"    <link>https://lawinen.report</link>\n" +
			"    <author xmlns=\"http://www.itunes.com/dtds/podcast-1.0.dtd\">Lawinen.report</author>\n" +
			"    <owner xmlns=\"http://www.itunes.com/dtds/podcast-1.0.dtd\">\n" +
			"      <name>Lawinen.report</name>\n" +
			"      <email>info@lawinen.report</email>\n" +
			"    </owner>\n" +
			"    <item>\n" +
			"      <title>2022-12-14.mp3</title>\n" +
			"      <description>2022-12-14.mp3</description>\n" +
			"      <pubDate>Wed, 14 Dec 2022 18:45:00 +0100</pubDate>\n" +
			"      <guid isPermaLink=\"false\">829be41f-9f6b-3f63-9f84-e171ae5ed20c</guid>\n" +
			"      <enclosure length=\"0\" type=\"audio/mpeg\" url=\"https://static.avalanche.report/media_files/AT-07/de/2022-12-14.mp3\"/>\n" +
			"    </item>\n" +
			"    <item>\n" +
			"      <title>2020-12-12.mp3</title>\n" +
			"      <description>2020-12-12.mp3</description>\n" +
			"      <pubDate>Sat, 12 Dec 2020 18:30:00 +0100</pubDate>\n" +
			"      <guid isPermaLink=\"false\">5bf489b2-7653-343b-8d77-ff9f4f410fe0</guid>\n" +
			"      <enclosure length=\"0\" type=\"audio/mpeg\" url=\"https://static.avalanche.report/media_files/AT-07/de/2020-12-12.mp3\"/>\n" +
			"    </item>\n" +
			"  </channel>\n" +
			"</rss>\n", rss);
	}
}
