// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

class XMLResourceBundle extends ResourceBundle {
	private final Properties props = new Properties();

	void loadFromXML(InputStream in) throws IOException {
		props.loadFromXML(in);
	}

	Object put(Object key, Object value) {
		return props.put(key, value);
	}

	@Override
	protected Object handleGetObject(String key) {
		return props.getProperty(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		Set<String> handleKeys = props.stringPropertyNames();
		return Collections.enumeration(handleKeys);
	}
}
