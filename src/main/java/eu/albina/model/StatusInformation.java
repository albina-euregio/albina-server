package eu.albina.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record StatusInformation(boolean ok, String message) {
}
