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

import eu.albina.caaml.Caaml;
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
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface RssUtil {

	static String getRss(LanguageCode language, Region region, java.nio.file.Path directory) throws ParserConfigurationException, IOException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();
		Document document = docBuilder.newDocument();
		Node rss = document.appendChild(document.createElement("rss"));
		Node channel = rss.appendChild(document.createElement("channel"));
		channel.appendChild(document.createElement("title")).setTextContent("albina media files");
		channel.appendChild(document.createElement("description")).setTextContent("albina media files");
		channel.appendChild(document.createElement("language")).setTextContent(language.name());
		channel.appendChild(document.createElement("link")).setTextContent(LinkUtil.getWebsiteUrl(language, region));
		channel.appendChild(document.createElementNS("http://www.itunes.com/dtds/podcast-1.0.dtd", "author")).setTextContent("avalanche.report");

		list(directory).sorted(Comparator.comparing(p -> p.getFileName().toString(), Comparator.reverseOrder())).forEach(path -> {
			try {
				Node item = channel.appendChild(document.createElement("item"));
				item.appendChild(document.createElement("title")).setTextContent(path.getFileName().toString());
				item.appendChild(document.createElement("description")).setTextContent(path.getFileName().toString());
				String pubDate = Files.getLastModifiedTime(path).toInstant().toString();
				item.appendChild(document.createElement("pubDate")).setTextContent(pubDate); // FIXME "Tue, 14 Mar 2017 12:00:00 GMT"
				item.appendChild(document.createElement("guid")).setTextContent(UUID.nameUUIDFromBytes((path.getFileName() + pubDate).getBytes(StandardCharsets.UTF_8)).toString());
				Element enclosure = (Element) item.appendChild(document.createElement("enclosure"));
				enclosure.setAttribute("url", LinkUtil.getStaticContentUrl(language, region) + "/" + directory.getFileName() + "/" + path.getFileName());
				enclosure.setAttribute("type", "audio/mpeg");
				enclosure.setAttribute("length", Long.toString(Files.size(path)));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		return Caaml.convertDocToString(document);
	}

	static Stream<Path> list(Path directory) throws IOException {
		return StreamSupport.stream(Files.newDirectoryStream(directory, "*.mp3").spliterator(), false);
	}
}
