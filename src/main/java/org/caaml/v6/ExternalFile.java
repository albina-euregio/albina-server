// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import io.micronaut.serde.annotation.Serdeable;

/**
 * External file is used to link to external files like maps, thumbnails etc.
 */
@Serdeable
public class ExternalFile {
    private String description;
    private String fileReferenceURI;
    private String fileType;

    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }

    public String getFileReferenceURI() { return fileReferenceURI; }
    public void setFileReferenceURI(String value) { this.fileReferenceURI = value; }

    public String getFileType() { return fileType; }
    public void setFileType(String value) { this.fileType = value; }
}
