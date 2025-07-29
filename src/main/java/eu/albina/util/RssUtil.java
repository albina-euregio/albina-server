/*******************************************************************************
 * Copyright (C) 2021 albina
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.util;

import eu.albina.controller.RegionController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface RssUtil {

	String ITUNES_NS = "http://www.itunes.com/dtds/podcast-1.0.dtd";

	static String getRss(LanguageCode language, Region region, java.nio.file.Path directory, String websiteName) throws ParserConfigurationException, IOException, TransformerException {
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
		channel.appendChild(document.createElement("link")).setTextContent(LinkUtil.getWebsiteUrl(language, region));
		channel.appendChild(document.createElementNS(ITUNES_NS, "author")).setTextContent(websiteName);
		Node owner = channel.appendChild(document.createElementNS(ITUNES_NS, "owner"));
		owner.appendChild(document.createElementNS(ITUNES_NS, "name")).setTextContent(websiteName);
		owner.appendChild(document.createElementNS(ITUNES_NS, "email")).setTextContent(language.getBundleString("email", region));


		list(directory).sorted(Comparator.comparing(p -> p.getFileName().toString(), Comparator.reverseOrder())).limit(10).forEach(path -> {
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
					LinkUtil.getStaticContentUrl(language, region),
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

	static Stream<Path> list(Path directory) throws IOException {
		return StreamSupport.stream(Files.newDirectoryStream(directory, "*.mp3").spliterator(), false);
	}
}
