package eu.albina.map;

import com.google.common.base.Preconditions;
import com.google.common.io.MoreFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

enum MapImageFormat {

	pdf {
		@Override
		Path convertFrom(Path file) {
			throw new UnsupportedOperationException();
		}
	}, png {
		@Override
		Path convertFrom(Path pdfFile) throws IOException, InterruptedException {
			Path pngFile = checkAndReplaceExtension(pdfFile, "pdf", "png");
			logger.info("Converting {} to {}", pdfFile, pngFile);
			int dpi = 300;
			new ProcessBuilder("gs",
				"-sDEVICE=png16m",
				"-dTextAlphaBits=4",
				"-dGraphicsAlphaBits=4",
				"-r" + dpi,
				"-o",
				pngFile.toString(),
				pdfFile.toString()
			).inheritIO().start().waitFor();
			return pngFile;
		}
	}, pngTransparent {
		@Override
		Path convertFrom(Path pngFile) throws IOException, InterruptedException {
			checkAndReplaceExtension(pngFile, "png", "png");
			logger.info("Creating transparency for {}", pngFile);
			new ProcessBuilder("convert",
				"-transparent",
				"white",
				pngFile.toString(),
				pngFile.toString()
			).inheritIO().start().waitFor();
			return pngFile;
		}
	}, jpg {
		@Override
		Path convertFrom(Path pngFile) throws IOException, InterruptedException {
			Path jpgFile = checkAndReplaceExtension(pngFile, "png", "jpg");
			logger.info("Converting {} to {}", pngFile, jpgFile);
			new ProcessBuilder("convert",
				pngFile.toString(),
				jpgFile.toString()
			).inheritIO().start().waitFor();
			return jpgFile;
		}
	}, webp {
		@Override
		Path convertFrom(Path pngFile) throws IOException, InterruptedException {
			Path webpFile = checkAndReplaceExtension(pngFile, "png", "webp");
			logger.info("Converting {} to {}", pngFile, webpFile);
			new ProcessBuilder("cwebp",
				pngFile.toString(),
				"-o",
				webpFile.toString())
				.inheritIO().start().waitFor();
			return webpFile;
		}
	};
	private static final Logger logger = LoggerFactory.getLogger(MapImageFormat.class);

	abstract Path convertFrom(Path file) throws IOException, InterruptedException;

	private static Path checkAndReplaceExtension(Path file, String src, String dst) {
		Preconditions.checkArgument(MoreFiles.getFileExtension(file).equals(src));
		String filename = MoreFiles.getNameWithoutExtension(file) + "." + dst;
		return file.resolveSibling(filename);
	}
}
