package eu.albina.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFont;

public class Paragraph {

	/** width of this paragraph */
	private int width;

	/** text to write */
	private String text;

	/** font to use */
	private PDFont font;

	/** font size to use */
	private int fontSize;

	public Paragraph(int width, PDFont font, int fontSize, String text) {
		this.text = text;
		this.font = font;
		this.width = width;
		this.fontSize = fontSize;
	}

	/**
	 * Break the text in lines
	 * 
	 * @return
	 */
	public List<String> getLines() throws IOException {
		List<String> result = new ArrayList<String>();

		String[] split = text.split("(?<=\\W)");
		int[] possibleWrapPoints = new int[split.length];
		possibleWrapPoints[0] = split[0].length();
		for (int i = 1; i < split.length; i++) {
			possibleWrapPoints[i] = possibleWrapPoints[i - 1] + split[i].length();
		}

		int start = 0;
		int end = 0;
		for (int i : possibleWrapPoints) {
			float width = font.getStringWidth(text.substring(start, i)) / 1000 * fontSize;
			if (start < end && width > this.width) {
				result.add(text.substring(start, end));
				start = end;
			}
			end = i;
		}
		// Last piece of text
		result.add(text.substring(start));
		return result;
	}

	public float getFontHeight() {
		return font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
	}

	public int getWidth() {
		return width;
	}

	public String getText() {
		return text;
	}

	public PDFont getFont() {
		return font;
	}

	public int getFontSize() {
		return fontSize;
	}

}