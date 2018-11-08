package eu.albina.model.socialmedia;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A RapidMailConfig.
 */
@Entity
@Table(name = "socialmedia_rapid_mail_config")
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id",scope = RapidMailConfig.class)
public class RapidMailConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "PASSWORD")
    private String password;


    @OneToMany(mappedBy = "rapidMailConfig",fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true)
    private Set<RapidMailRecipient> rapidMailRecipients = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "PROVIDER_ID")
    private Provider provider;

    @JsonIgnoreProperties(value = {"region","messengerPeopleConfig","twitterConfig","rapidMailConfig","shipments","channels"},allowSetters = true)
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(unique = true, name = "REGION_CONFIGURATION_ID")
    private RegionConfiguration regionConfiguration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public RapidMailConfig username(String username) {
        this.username = username;
        return this;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public RapidMailConfig password(String password) {
        this.password = password;
        return this;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RegionConfiguration getRegionConfiguration() {
        return regionConfiguration;
    }

    public RapidMailConfig regionConfiguration(RegionConfiguration regionConfiguration) {
        this.regionConfiguration = regionConfiguration;
        return this;
    }

    public void setRegionConfiguration(RegionConfiguration regionConfiguration) {
        this.regionConfiguration = regionConfiguration;
    }
    public Provider getProvider() {
        return provider;
    }

    public RapidMailConfig provider(Provider provider) {
        this.provider = provider;
        return this;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Set<RapidMailRecipient> getRapidMailRecipients() {
        return rapidMailRecipients;
    }

    public RapidMailConfig rapidMailRecipients(Set<RapidMailRecipient> rapidMailRecipients) {
        this.rapidMailRecipients = rapidMailRecipients;
        return this;
    }

    public RapidMailConfig addRapidMailRecipients(RapidMailRecipient rapidMailRecipients) {
        this.rapidMailRecipients.add(rapidMailRecipients);
        rapidMailRecipients.setRapidMailConfig(this);
        return this;
    }

    public RapidMailConfig removeRapidMailRecipients(RapidMailRecipient rapidMailRecipients) {
        this.rapidMailRecipients.remove(rapidMailRecipients);
        rapidMailRecipients.setRapidMailConfig(null);
        return this;
    }

    public void setRapidMailRecipients(Set<RapidMailRecipient> rapidMailRecipients) {
        this.rapidMailRecipients = rapidMailRecipients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RapidMailConfig rapidMailConfig = (RapidMailConfig) o;
        if (rapidMailConfig.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), rapidMailConfig.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RapidMailConfig{" +
            "id=" + getId() +
            ", username='" + getUsername() + "'" +
            ", password='" + getPassword() + "'" +
            "}";
    }
}
