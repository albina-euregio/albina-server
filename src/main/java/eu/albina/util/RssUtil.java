// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;

public interface RssUtil {

	String ITUNES_NS = "http://www.itunes.com/dtds/podcast-1.0.dtd";

	static String getRss(LanguageCode language, Region region, java.nio.file.Path directory) throws ParserConfigurationException, IOException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();
		Document document = docBuilder.newDocument();
		Element rss = (Element) document.appendChild(document.createElement("rss"));
		rss.setAttribute("version", "2.0");
		Node channel = rss.appendChild(document.createElement("channel"));
		channel.appendChild(document.createElement("title")).setTextContent("albina media files");
		channel.appendChild(document.createElement("description")).setTextContent("albina media files");
		channel.appendChild(document.createElement("language")).setTextContent(language.name());
		channel.appendChild(document.createElement("link")).setTextContent(region.getWebsiteUrl(language));
		channel.appendChild(document.createElementNS(ITUNES_NS, "author")).setTextContent(region.getWebsiteName(language));
		Node owner = channel.appendChild(document.createElementNS(ITUNES_NS, "owner"));
		owner.appendChild(document.createElementNS(ITUNES_NS, "name")).setTextContent(region.getWebsiteName(language));
		owner.appendChild(document.createElementNS(ITUNES_NS, "email")).setTextContent(region.getWarningServiceEmail(language));


		list(directory).forEach(path -> {
			try {
				Node item = channel.appendChild(document.createElement("item"));
				item.appendChild(document.createElement("title")).setTextContent(path.getFileName().toString());
				item.appendChild(document.createElement("description")).setTextContent(path.getFileName().toString());
				Instant pubDate = Files.getLastModifiedTime(path).toInstant();
				item.appendChild(document.createElement("pubDate")).setTextContent(DateTimeFormatter.RFC_1123_DATE_TIME.format(pubDate.atZone(AlbinaUtil.localZone())));
				Element guid = (Element) item.appendChild(document.createElement("guid"));
				guid.setAttribute("isPermaLink", Boolean.FALSE.toString());
				guid.setTextContent(UUID.nameUUIDFromBytes((path.getFileName() + pubDate.toString()).getBytes(StandardCharsets.UTF_8)).toString());
				Element enclosure = (Element) item.appendChild(document.createElement("enclosure"));
				enclosure.setAttribute("url", String.format("%s/%s/%s/%s/%s",
					region.getStaticUrl(),
					directory.getName(directory.getNameCount() - 3),
					directory.getName(directory.getNameCount() - 2),
					directory.getName(directory.getNameCount() - 1),
					path.getFileName()));
				enclosure.setAttribute("type", "audio/mpeg");
				enclosure.setAttribute("length", Long.toString(Files.size(path)));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		return XmlUtil.convertDocToString(document);
	}

	static List<Path> list(Path directory) throws IOException {
		PathMatcher matcher = directory.getFileSystem().getPathMatcher("glob:*.mp3");
		try (Stream<Path> stream = Files.list(directory)) {
			return stream.filter(p -> matcher.matches(p.getFileName()))
				.sorted(Comparator.comparing(p -> p.getFileName().toString(), Comparator.reverseOrder()))
				.limit(10).toList();
		}
	}
}
