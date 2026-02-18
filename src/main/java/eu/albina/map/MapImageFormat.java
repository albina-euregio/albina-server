package eu.albina.map;

import com.google.common.base.Preconditions;
import com.google.common.base.StandardSystemProperty;
import com.google.common.io.MoreFiles;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public enum MapImageFormat {

	pdf {
		@Override
		Path convertFrom(Path file) {
			throw new UnsupportedOperationException();
		}
	}, png {
		@Override
		Path convertFrom(Path pdfFile) throws IOException, InterruptedException {
			Path pngFile = checkAndReplaceExtension(pdfFile, "pdf", "png");
			logger.debug("Converting {} to {}", pdfFile, pngFile);
			try (PDDocument document = Loader.loadPDF(Files.readAllBytes(pdfFile))) {
				PDFRenderer renderer = new PDFRenderer(document);
				BufferedImage image = renderer.renderImageWithDPI(0, dpi, ImageType.ARGB);
				ImageIOUtil.writeImage(image, pngFile.toString(), dpi);
			}
			return pngFile;
		}
	}, pngTransparent {
		@Override
		Path convertFrom(Path pdfFile) throws IOException, InterruptedException {
			Path pngFile = checkAndReplaceExtension(pdfFile, "pdf", "png");
			logger.debug("Creating transparency for {}", pngFile);
			try (PDDocument document = Loader.loadPDF(Files.readAllBytes(pdfFile))) {
				PDFRenderer renderer = new PDFRenderer(document);
				BufferedImage image = renderer.renderImageWithDPI(0, dpi, ImageType.ARGB);
				for (int y = 0; y < image.getHeight(); y++) {
					for (int x = 0; x < image.getWidth(); x++) {
						Color color = new Color(image.getRGB(x, y));
						if (color.equals(Color.white)) {
							image.setRGB(x, y, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0).getRGB());
						}
					}
				}
				ImageIOUtil.writeImage(image, pngFile.toString(), dpi);
			}
			return pngFile;
		}
	}, jpg {
		@Override
		Path convertFrom(Path pngFile) throws IOException, InterruptedException {
			Path jpgFile = checkAndReplaceExtension(pngFile, "png", "jpg");
			logger.debug("Converting {} to {}", pngFile, jpgFile);
			if (IS_OS_WINDOWS) {
				new ProcessBuilder("cmd.exe", "/C", "convert",
					pngFile.toString(),
					jpgFile.toString()
				).inheritIO().start().waitFor();
			} else {
				new ProcessBuilder("convert",
					pngFile.toString(),
					jpgFile.toString()
				).inheritIO().start().waitFor();
			}
			return jpgFile;
		}
	}, webp {
		@Override
		Path convertFrom(Path pngFile) throws IOException, InterruptedException {
			Path webpFile = checkAndReplaceExtension(pngFile, "png", "webp");
			logger.debug("Converting {} to {}", pngFile, webpFile);
			if (IS_OS_WINDOWS) {
				new ProcessBuilder("cmd.exe", "/C", "cwebp",
					pngFile.toString(),
					"-o",
					webpFile.toString())
					.inheritIO().start().waitFor();
			} else {
				new ProcessBuilder("cwebp",
					pngFile.toString(),
					"-o",
					webpFile.toString())
					.inheritIO().start().waitFor();
			}
			return webpFile;
		}
	};
	private static final int dpi = 300;
	private static final boolean IS_OS_WINDOWS = StandardSystemProperty.OS_NAME.value().contains("Windows");
	private static final Logger logger = LoggerFactory.getLogger(MapImageFormat.class);

	abstract Path convertFrom(Path file) throws IOException, InterruptedException;

	private static Path checkAndReplaceExtension(Path file, String src, String dst) {
		Preconditions.checkArgument(MoreFiles.getFileExtension(file).equals(src));
		String filename = MoreFiles.getNameWithoutExtension(file) + "." + dst;
		return file.resolveSibling(filename);
	}
}
