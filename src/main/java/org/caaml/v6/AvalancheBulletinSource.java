package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Details about the issuer/AWS of the bulletin.
 *
 * Information about the bulletin source. Either as in a person or with a provider element
 * to specify details about the AWS.
 */
@JsonPropertyOrder({"provider", "person"})
public class AvalancheBulletinSource {
    private Person person;
    private AvalancheBulletinProvider provider;

    public Person getPerson() { return person; }
    public void setPerson(Person value) { this.person = value; }

    public AvalancheBulletinProvider getProvider() { return provider; }
    public void setProvider(AvalancheBulletinProvider value) { this.provider = value; }
}
