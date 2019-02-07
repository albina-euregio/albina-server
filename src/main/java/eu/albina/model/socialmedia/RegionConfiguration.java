package eu.albina.model.socialmedia;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.albina.model.Region;

/**
 * A Region.
 */
@Entity
@Table(name = "socialmedia_region")
// @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
// property = "id",scope = RegionConfiguration.class)
public class RegionConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "REGION_ID", updatable = false, insertable = false)
	@JsonIgnoreProperties({ "polygon", "parentRegion", "subregions", "aggregatedRegion" })
	private Region region;

	@OneToOne(mappedBy = "regionConfiguration")
	private MessengerPeopleConfig messengerPeopleConfig;

	@OneToOne(mappedBy = "regionConfiguration")
	private TwitterConfig twitterConfig;

	@OneToOne(mappedBy = "regionConfiguration")
	private RapidMailConfig rapidMailConfig;

	@OneToMany(mappedBy = "regionConfiguration")
	@JsonIgnore
	@Transient
	private Set<Shipment> shipments = new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "socialmedia_channel_region", joinColumns = @JoinColumn(name = "REGION_ID", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "CHANNEL_ID", referencedColumnName = "ID"))
	private Set<Channel> channels = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public RegionConfiguration region(Region region) {
		this.region = region;
		return this;
	}

	public MessengerPeopleConfig getMessengerPeopleConfig() {
		return messengerPeopleConfig;
	}

	public RegionConfiguration messengerPeopleConfig(MessengerPeopleConfig messengerPeopleConfig) {
		this.messengerPeopleConfig = messengerPeopleConfig;
		return this;
	}

	public void setMessengerPeopleConfig(MessengerPeopleConfig messengerPeopleConfig) {
		this.messengerPeopleConfig = messengerPeopleConfig;
	}

	public TwitterConfig getTwitterConfig() {
		return twitterConfig;
	}

	public RegionConfiguration twitterConfig(TwitterConfig twitterConfig) {
		this.twitterConfig = twitterConfig;
		return this;
	}

	public void setTwitterConfig(TwitterConfig twitterConfig) {
		this.twitterConfig = twitterConfig;
	}

	public RapidMailConfig getRapidMailConfig() {
		return rapidMailConfig;
	}

	public RegionConfiguration rapidMailConfig(RapidMailConfig rapidMailConfig) {
		this.rapidMailConfig = rapidMailConfig;
		return this;
	}

	public void setRapidMailConfig(RapidMailConfig rapidMailConfig) {
		this.rapidMailConfig = rapidMailConfig;
	}

	public Set<Shipment> getShipments() {
		return shipments;
	}

	public RegionConfiguration shipments(Set<Shipment> shipments) {
		this.shipments = shipments;
		return this;
	}

	public RegionConfiguration addShipment(Shipment shipment) {
		this.shipments.add(shipment);
		shipment.setRegion(this);
		return this;
	}

	public RegionConfiguration removeShipment(Shipment shipment) {
		this.shipments.remove(shipment);
		shipment.setRegion(null);
		return this;
	}

	public void setShipments(Set<Shipment> shipments) {
		this.shipments = shipments;
	}

	public Set<Channel> getChannels() {
		return channels;
	}

	public RegionConfiguration channels(Set<Channel> channels) {
		this.channels = channels;
		return this;
	}

	public RegionConfiguration addChannel(Channel channel) {
		this.channels.add(channel);
		channel.getRegions().add(this);
		return this;
	}

	public RegionConfiguration removeChannel(Channel channel) {
		this.channels.remove(channel);
		channel.getRegions().remove(this);
		return this;
	}

	public void setChannels(Set<Channel> channels) {
		this.channels = channels;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RegionConfiguration region = (RegionConfiguration) o;
		if (region.getId() == null || getId() == null) {
			return false;
		}
		return Objects.equals(getId(), region.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return "Region{" + "id=" + getId() + "}";
	}

}
