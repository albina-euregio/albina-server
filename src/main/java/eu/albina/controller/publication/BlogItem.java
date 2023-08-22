package eu.albina.controller.publication;

import java.time.OffsetDateTime;

public interface BlogItem {
	String getId();

	String getTitle();

	OffsetDateTime getPublished();

	String getAttachmentUrl();
}
