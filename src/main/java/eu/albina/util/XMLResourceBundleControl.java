// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import java.util.TreeMap;

public class XMLResourceBundleControl extends ResourceBundle.Control {
	private static final String XML = "xml";

	@Override
	public List<String> getFormats(String baseName) {
		return Collections.singletonList(XML);
	}

	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
		throws IOException {

		if ((baseName == null) || (locale == null) || (format == null) || (loader == null)) {
			throw new NullPointerException();
		}
		if (!format.equals(XML)) {
			return null;
		}

		if ("micro-regions_names".equals(baseName)) {
			URL resource = loader.getResource("micro-regions_names/" + locale + ".json");
			TreeMap<String, String> strings;
			try (InputStream stream = resource.openStream()) {
				strings = new ObjectMapper().readValue(stream, new TypeReference<>() {
				});
			}
			XMLResourceBundle bundle = new XMLResourceBundle();
			strings.forEach(bundle::put);
			return bundle;
		}

		if ("i18n.caaml".equals(baseName)) {
			URL resource = loader.getResource("i18n/caaml/" + locale + ".json");
			Map<String, Object> tree;
			try (InputStream stream = resource.openStream()) {
				tree = new ObjectMapper().readValue(stream, new TypeReference<>() {
				});
			}
			XMLResourceBundle bundle = new XMLResourceBundle();
			flatten(null, tree, bundle);
			return bundle;
		}

		String resourceName = toResourceName(toBundleName(baseName, locale), format);
		try (InputStream stream = loader.getResource(resourceName).openStream()) {
			XMLResourceBundle bundle = new XMLResourceBundle();
			bundle.loadFromXML(stream);
			return bundle;
		}
	}

	@SuppressWarnings("unchecked")
	private static void flatten(String prefix, Map<String, Object> tree, XMLResourceBundle bundle) {
		tree.forEach((key, value) -> {
			String fullKey = prefix == null ? key : prefix + "." + key;
			if (value instanceof Map) {
				flatten(fullKey, (Map<String, Object>) value, bundle);
			} else {
				bundle.put(fullKey, value);
			}
		});
	}
}
