package eu.albina.map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.FeatureCollection;
import org.mapyrus.Context;
import org.mapyrus.ContextStack;
import org.mapyrus.FileOrURL;
import org.mapyrus.Interpreter;
import org.mapyrus.MapyrusException;

import javax.script.SimpleBindings;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class MapyrusInterpreter {
	final Interpreter interpreter = new Interpreter();
	final ContextStack context = new ContextStack() {
		@Override
		public void setDataset(String type, String name, String extras, InputStream stdin) throws MapyrusException {
			if ("geojson".equals(type) || name.endsWith(".geojson") || name.endsWith(".geojson.json")) {
				setGeoJsonDataset(Paths.get(name));
			} else {
				super.setDataset(type, name, extras, stdin);
			}
		}

		private FeatureCollection getFeatureCollection(Path path) throws MapyrusException {
			try (InputStream src = Files.newInputStream(path)) {
				return new ObjectMapper().readValue(src, FeatureCollection.class);
			} catch (IOException e) {
				throw new MapyrusException("Failed to read " + path + ": " + e);
			}
		}

		private void setGeoJsonDataset(Path path) throws MapyrusException {
			final FeatureCollection featureCollection = getFeatureCollection(path);
			try {
				final GeoJsonDataset dataset = new GeoJsonDataset(featureCollection);
				final Method getCurrentContext = ContextStack.class.getDeclaredMethod("getCurrentContext");
				getCurrentContext.setAccessible(true);
				final Context context = (Context) getCurrentContext.invoke(this);
				context.setDataset(dataset);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new MapyrusException("Failed to set dataset: " + e);
			}
		}
	};

	MapyrusInterpreter(SimpleBindings bindings) {
		context.setBindings(bindings);
	}

	void interpret(FileOrURL fileOrURL) throws MapyrusException, IOException, InterruptedException {
		interpreter.interpret(context, fileOrURL, System.in, System.out);
	}

	void interpret(URL resource) throws MapyrusException, IOException, InterruptedException {
		interpret(new FileOrURL(resource.toString()));
	}
}
