package eu.albina.util;

import com.github.openjson.JSONObject;
import com.google.common.io.Resources;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class XMLResourceBundleControl extends ResourceBundle.Control {
	private static final String XML = "xml";

	@Override
	public List<String> getFormats(String baseName) {
		return Collections.singletonList(XML);
	}

	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {

		if ((baseName == null) || (locale == null) || (format == null) || (loader == null)) {
			throw new NullPointerException();
		}
		if (!format.equals(XML)) {
			return null;
		}

		if ("micro-regions_names".equals(baseName)) {
			String resourceName = "micro-regions_names/" + locale + ".json";
			URL url = loader.getResource(resourceName);
			if (url == null) {
				return null;
			}
			JSONObject object = new JSONObject(Resources.toString(url, StandardCharsets.UTF_8));
			XMLResourceBundle bundle = new XMLResourceBundle();
			object.keySet().forEach(key -> bundle.put(key, object.get(key)));
			return bundle;
		}

		String resourceName = toResourceName(toBundleName(baseName, locale), format);
		URL url = loader.getResource(resourceName);
		if (url == null) {
			return null;
		}
		URLConnection connection = url.openConnection();
		if (connection == null) {
			return null;
		}
		if (reload) {
			connection.setUseCaches(false);
		}
		InputStream stream = connection.getInputStream();
		if (stream == null) {
			return null;
		}
		BufferedInputStream bis = new BufferedInputStream(stream);
		XMLResourceBundle bundle = new XMLResourceBundle();
		bundle.loadFromXML(stream);
		bis.close();

		return bundle;
	}
}
