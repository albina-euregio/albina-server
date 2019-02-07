package eu.albina.model.socialmedia;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A TwitterConfig.
 */
@Entity
@Table(name = "socialmedia_twitter_config")
// @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
// property = "id",scope = TwitterConfig.class)
public class TwitterConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ACCESS_SECRET")
	private String accessSecret;

	@Column(name = "ACCESS_KEY")
	private String accessKey;

	@Column(name = "CONSUMER_KEY")
	private String consumerKey;

	@Column(name = "CONSUMER_SECRET")
	private String consumerSecret;

	@ManyToOne
	@JoinColumn(name = "PROVIDER_ID")
	private Provider provider;

	@JsonIgnoreProperties(value = { "region", "messengerPeopleConfig", "twitterConfig", "rapidMailConfig", "shipments",
			"channels" }, allowSetters = true)
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(unique = true, name = "REGION_CONFIGURATION_ID")
	private RegionConfiguration regionConfiguration;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccessSecret() {
		return accessSecret;
	}

	public TwitterConfig accessSecret(String accessSecret) {
		this.accessSecret = accessSecret;
		return this;
	}

	public void setAccessSecret(String accessSecret) {
		this.accessSecret = accessSecret;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public TwitterConfig accessKey(String accessKey) {
		this.accessKey = accessKey;
		return this;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public TwitterConfig consumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
		return this;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public TwitterConfig consumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
		return this;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public RegionConfiguration getRegionConfiguration() {
		return regionConfiguration;
	}

	public TwitterConfig regionConfiguration(RegionConfiguration regionConfiguration) {
		this.regionConfiguration = regionConfiguration;
		return this;
	}

	public void setRegionConfiguration(RegionConfiguration regionConfiguration) {
		this.regionConfiguration = regionConfiguration;
	}

	public Provider getProvider() {
		return provider;
	}

	public TwitterConfig provider(Provider provider) {
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
		TwitterConfig twitterConfig = (TwitterConfig) o;
		if (twitterConfig.getId() == null || getId() == null) {
			return false;
		}
		return Objects.equals(getId(), twitterConfig.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return "TwitterConfig{" + "id=" + getId() + ", accessSecret='" + getAccessSecret() + "'" + ", accessKey='"
				+ getAccessKey() + "'" + ", consumerKey='" + getConsumerKey() + "'" + ", consumerSecret='"
				+ getConsumerSecret() + "'" + "}";
	}
}
