package eu.albina.map;

import com.google.common.base.Preconditions;
import com.google.common.io.MoreFiles;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public enum MapImageFormat {

	pdf, png, jpg, webp;

	static final int dpi = 300;

	static BufferedImage renderPDF(Path file) throws IOException {
		try (PDDocument document = Loader.loadPDF(Files.readAllBytes(file))) {
			PDFRenderer renderer = new PDFRenderer(document);
			return renderer.renderImageWithDPI(0, dpi, ImageType.RGB);
		}
	}

	static BufferedImage makeTransparent(BufferedImage image) {
		int white = Color.white.getRGB();
		int transparent = 0xFFFFFF;

		BufferedImage argbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		argbImage.getGraphics().drawImage(image, 0, 0, null);
		image = argbImage;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (image.getRGB(x, y) == white) {
					image.setRGB(x, y, transparent);
				}
			}
		}
		return image;
	}

	static BufferedImage stitchVertically(BufferedImage amImage,  BufferedImage pmImage) {
		int width = Math.max(amImage.getWidth(), pmImage.getWidth());
		int height = amImage.getHeight() + pmImage.getHeight();
		BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		combined.getGraphics().drawImage(amImage, 0, 0, null);
		combined.getGraphics().drawImage(pmImage, 0, amImage.getHeight(), null);
		return combined;
	}

	static Path checkAndReplaceExtension(Path file, MapImageFormat src, MapImageFormat dst) {
		Preconditions.checkArgument(MoreFiles.getFileExtension(file).equals(src.name()));
		String filename = MoreFiles.getNameWithoutExtension(file) + "." + dst;
		return file.resolveSibling(filename);
	}
}
