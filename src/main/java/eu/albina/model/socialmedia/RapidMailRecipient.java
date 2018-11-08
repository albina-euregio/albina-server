package eu.albina.model.socialmedia;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A RapidMailRecipients.
 */
@Entity
@Table(name = "socialmedia_rapid_mail_recipients")
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id",scope = RapidMailRecipient.class)
public class RapidMailRecipient implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "REFERENCE")
    private String reference;

    @ManyToOne
    @JoinColumn(name = "RAPIDMAIL_CONFIG_ID")
    @JsonIgnoreProperties({"username","password","rapidMailRecipients","provider","regionConfiguration"})
    private RapidMailConfig rapidMailConfig;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public RapidMailRecipient reference(String reference) {
        this.reference = reference;
        return this;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public RapidMailConfig getRapidMailConfig() {
        return rapidMailConfig;
    }

    public RapidMailRecipient rapidMail(RapidMailConfig rapidMail) {
        this.rapidMailConfig = rapidMail;
        return this;
    }

    public void setRapidMailConfig(RapidMailConfig rapidMailConfig) {
        this.rapidMailConfig = rapidMailConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RapidMailRecipient rapidMailRecipients = (RapidMailRecipient) o;
        if (rapidMailRecipients.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), rapidMailRecipients.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RapidMailRecipients{" +
            "id=" + getId() +
            ", reference='" + getReference() + "'" +
            "}";
    }
}
