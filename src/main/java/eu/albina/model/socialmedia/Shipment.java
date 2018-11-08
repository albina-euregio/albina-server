package eu.albina.model.socialmedia;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A Shipment.
 */
@Entity
@Table(name = "socialmedia_shipment")
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id",scope = Shipment.class)
public class Shipment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DATE")
    private ZonedDateTime date;

    @Column(name = "NAME")
    private String name;

    @Column(name = "LANGUAGE")
    private String language;

    @Column(name = "ID_MP")
    private String idMp;

    @Column(name = "ID_RM")
    private String idRm;

    @Column(name = "ID_TW")
    private String idTw;

    @Lob
    @Column(name = "CONTENT")
    private String content;

    @ManyToOne
    @JsonIgnoreProperties("shipments")
    @JoinColumn(name = "REGION_ID")
    private RegionConfiguration region;

    @ManyToOne
    @JsonIgnoreProperties("shipments")
    @JoinColumn(name = "PROVIDER_ID")
    private Provider provider;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public Shipment date(ZonedDateTime date) {
        this.date = date;
        return this;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public Shipment name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public Shipment language(String language) {
        this.language = language;
        return this;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIdMp() {
        return idMp;
    }

    public Shipment idMp(String idMp) {
        this.idMp = idMp;
        return this;
    }

    public void setIdMp(String idMp) {
        this.idMp = idMp;
    }

    public String getIdRm() {
        return idRm;
    }

    public Shipment idRm(String idRm) {
        this.idRm = idRm;
        return this;
    }

    public void setIdRm(String idRm) {
        this.idRm = idRm;
    }

    public String getIdTw() {
        return idTw;
    }

    public Shipment idTw(String idTw) {
        this.idTw = idTw;
        return this;
    }

    public void setIdTw(String idTw) {
        this.idTw = idTw;
    }

    public String getContent() {
        return content;
    }

    public Shipment content(String content) {
        this.content = content;
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public RegionConfiguration getRegion() {
        return region;
    }

    public Shipment region(RegionConfiguration region) {
        this.region = region;
        return this;
    }

    public void setRegion(RegionConfiguration region) {
        this.region = region;
    }

    public Provider getProvider() {
        return provider;
    }

    public Shipment provider(Provider provider) {
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
        Shipment shipment = (Shipment) o;
        if (shipment.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), shipment.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Shipment{" +
            "id=" + getId() +
            ", date='" + getDate() + "'" +
            ", name='" + getName() + "'" +
            ", language='" + getLanguage() + "'" +
            ", idMp='" + getIdMp() + "'" +
            ", idRm='" + getIdRm() + "'" +
            ", idTw='" + getIdTw() + "'" +
            ", content='" + getContent() + "'" +
            "}";
    }
}
