// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Details on a person.
 */
@JsonPropertyOrder({"name", "website", "metaData", "customData"})
public class Person {
    private Object customData;
    private MetaData metaData;
    private String name;
    private String website;

    public Object getCustomData() { return customData; }
    public void setCustomData(Object value) { this.customData = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getWebsite() { return website; }
    public void setWebsite(String value) { this.website = value; }
}
