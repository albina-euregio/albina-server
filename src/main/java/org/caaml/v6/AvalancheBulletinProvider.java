package org.caaml.v6;

/**
 * Information about the bulletin provider. Defines the name, website and/or contactPerson
 * (which could be the author) of the issuing AWS.
 */
public class AvalancheBulletinProvider {
    private Person contactPerson;
    private Object customData;
    private MetaData metaData;
    private String name;
    private String website;

    public Person getContactPerson() { return contactPerson; }
    public void setContactPerson(Person value) { this.contactPerson = value; }

    public Object getCustomData() { return customData; }
    public void setCustomData(Object value) { this.customData = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getWebsite() { return website; }
    public void setWebsite(String value) { this.website = value; }
}
