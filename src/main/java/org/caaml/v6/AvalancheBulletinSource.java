package org.caaml.v6;

/**
 * Details about the issuer/AWS of the bulletin.
 *
 * Information about the bulletin source. Either as in a person or with a provider element
 * to specify details about the AWS.
 */
public class AvalancheBulletinSource {
    private Person contactPerson;
    private AvalancheBulletinProvider provider;

    public Person getContactPerson() { return contactPerson; }
    public void setContactPerson(Person value) { this.contactPerson = value; }

    public AvalancheBulletinProvider getProvider() { return provider; }
    public void setProvider(AvalancheBulletinProvider value) { this.provider = value; }
}
