package eu.albina.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record StatusInformation(boolean ok, String title, String message) {

	public String toLogLine() {
		return String.format("[%s] %s - %s",ok ? "OK" : "FAIL",	title, message);
	}
}
