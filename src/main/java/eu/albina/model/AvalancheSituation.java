package eu.albina.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.albina.model.enumerations.Aspect;

@Audited
@Entity
@Table(name = "AVALANCHE_SITUATION")
public class AvalancheSituation extends AbstractPersistentObject implements AvalancheInformationObject {

	@OneToOne
	@PrimaryKeyJoinColumn
	private AvalancheBulletinDaytimeDescription avalancheBulletinDaytimeDescription;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_SITUATION")
	private eu.albina.model.enumerations.AvalancheSituation avalancheSituation;

	@ElementCollection
	@CollectionTable(name = "AVALANCHE_SITUATION_ASPECTS", joinColumns = @JoinColumn(name = "AVALANCHE_SITUATION_ID", referencedColumnName = "ID"))
	@Column(name = "ASPECT")
	private Set<Aspect> aspects;

	public AvalancheSituation() {
		this.aspects = new HashSet<Aspect>();
	}

	public AvalancheSituation(JSONObject json) {
		this();

		if (json.has("avalancheSituation"))
			this.avalancheSituation = eu.albina.model.enumerations.AvalancheSituation
					.valueOf(json.getString("avalancheSituation").toLowerCase());
		if (json.has("aspects")) {
			JSONArray aspects = json.getJSONArray("aspects");
			for (Object entry : aspects) {
				this.aspects.add(Aspect.valueOf(((String) entry).toUpperCase()));
			}
		}
	}

	public eu.albina.model.enumerations.AvalancheSituation getAvalancheSituation() {
		return avalancheSituation;
	}

	public void setAvalancheSituation(eu.albina.model.enumerations.AvalancheSituation avalancheSituation) {
		this.avalancheSituation = avalancheSituation;
	}

	public Set<Aspect> getAspects() {
		return aspects;
	}

	public void setAspects(Set<Aspect> aspects) {
		this.aspects = aspects;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		if (avalancheSituation != null)
			json.put("avalancheSituation", this.avalancheSituation.toString());
		if (aspects != null && aspects.size() > 0) {
			JSONArray aspects = new JSONArray();
			for (Aspect aspect : this.aspects) {
				aspects.put(aspect.toString());
			}
			json.put("aspects", aspects);
		}
		return json;
	}

}
