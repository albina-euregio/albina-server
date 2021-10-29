package eu.albina.map;

import org.mapyrus.ContextStack;
import org.mapyrus.FileOrURL;
import org.mapyrus.Interpreter;
import org.mapyrus.MapyrusException;

import javax.script.SimpleBindings;
import java.io.IOException;
import java.net.URL;

class MapyrusInterpreter {
	final Interpreter interpreter = new Interpreter();
	final ContextStack context = new ContextStack();

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
