package eu.albina.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "AVALANCHE_SITUATION_ASPECTS", joinColumns = @JoinColumn(name = "AVALANCHE_SITUATION_ID", referencedColumnName = "ID"))
	@Column(name = "ASPECT")
	private Set<Aspect> aspects;

	@Column(name = "ELEVATION_HIGH")
	private int elevationHigh;

	@Column(name = "TREELINE_HIGH")
	private boolean treelineHigh;

	@Column(name = "ELEVATION_LOW")
	private int elevationLow;

	@Column(name = "TREELINE_LOW")
	private boolean treelineLow;

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
		if (json.has("elevationHigh"))
			this.elevationHigh = json.getInt("elevationHigh");
		if (json.has("treelineHigh"))
			this.treelineHigh = json.getBoolean("treelineHigh");
		if (json.has("elevationLow"))
			this.elevationLow = json.getInt("elevationLow");
		if (json.has("treelineLow"))
			this.treelineLow = json.getBoolean("treelineLow");
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

	public int getElevationHigh() {
		return elevationHigh;
	}

	public void setElevationHigh(int elevationHigh) {
		this.elevationHigh = elevationHigh;
	}

	public boolean getTreelineHigh() {
		return treelineHigh;
	}

	public void setTreelineHigh(boolean treelineHigh) {
		this.treelineHigh = treelineHigh;
	}

	public int getElevationLow() {
		return elevationLow;
	}

	public void setElevationLow(int elevationLow) {
		this.elevationLow = elevationLow;
	}

	public boolean getTreelineLow() {
		return treelineLow;
	}

	public void setTreelineLow(boolean treelineLow) {
		this.treelineLow = treelineLow;
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

		if (treelineHigh)
			json.put("treelineHigh", treelineHigh);
		else if (elevationHigh > 0)
			json.put("elevationHigh", elevationHigh);
		if (treelineLow)
			json.put("treelineLow", treelineLow);
		else if (elevationLow > 0)
			json.put("elevationLow", elevationLow);

		return json;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!AvalancheSituation.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final AvalancheSituation other = (AvalancheSituation) obj;

		if (this.avalancheSituation != other.avalancheSituation)
			return false;
		if (!this.aspects.containsAll(other.getAspects()) || !other.getAspects().containsAll(this.aspects))
			return false;
		if (this.elevationHigh != other.elevationHigh)
			return false;
		if (this.elevationLow != other.elevationLow)
			return false;

		return true;
	}
}
