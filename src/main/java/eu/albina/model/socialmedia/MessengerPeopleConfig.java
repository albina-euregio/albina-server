package eu.albina.model.socialmedia;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A MessengerPeopleConfig.
 */
@Entity
@Table(name = "socialmedia_messenger_people_config")
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id",scope = MessengerPeopleConfig.class)
public class MessengerPeopleConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CHANNEL_NAME")
    private String channelName;

    @Column(name = "API_KEY")
    private String apiKey;

    @Column(name = "MOBILE_NUMBER")
    private String mobileNumber;

    //    @JsonIgnoreProperties("messengerPeopleConfigs")
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

    public String getChannelName() {
        return channelName;
    }

    public MessengerPeopleConfig channelName(String channelName) {
        this.channelName = channelName;
        return this;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public MessengerPeopleConfig apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public MessengerPeopleConfig mobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
        return this;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public RegionConfiguration getRegionConfiguration() {
        return regionConfiguration;
    }

    public MessengerPeopleConfig regionConfiguration(RegionConfiguration regionConfiguration) {
        this.regionConfiguration = regionConfiguration;
        return this;
    }

    public void setRegionConfiguration(RegionConfiguration regionConfiguration) {
        this.regionConfiguration = regionConfiguration;
    }

    public Provider getProvider() {
        return provider;
    }

    public MessengerPeopleConfig provider(Provider provider) {
        this.provider = provider;
        return this;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MessengerPeopleConfig messengerPeopleConfig = (MessengerPeopleConfig) o;
        if (messengerPeopleConfig.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), messengerPeopleConfig.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MessengerPeopleConfig{" +
            "id=" + getId() +
            ", channelName='" + getChannelName() + "'" +
            ", apiKey='" + getApiKey() + "'" +
            ", mobileNumber='" + getMobileNumber() + "'" +
            "}";
    }
}
