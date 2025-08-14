// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.util.List;

/**
 * Meta data for various uses. Can be used to link to external files like maps, thumbnails
 * etc.
 */
public class MetaData {
    private String comment;
    private List<ExternalFile> extFiles;

    public String getComment() { return comment; }
    public void setComment(String value) { this.comment = value; }

    public List<ExternalFile> getEXTFiles() { return extFiles; }
    public void setEXTFiles(List<ExternalFile> value) { this.extFiles = value; }
}
