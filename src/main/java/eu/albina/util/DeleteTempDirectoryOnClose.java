// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.MoreFiles;

public record DeleteTempDirectoryOnClose(Path tempDirectory) implements Closeable {
	public static DeleteTempDirectoryOnClose of(String prefix) throws IOException {
		return new DeleteTempDirectoryOnClose(Files.createTempDirectory(prefix));
	}

	@Override
	public void close() throws IOException {
		MoreFiles.deleteRecursively(tempDirectory);
	}
}
