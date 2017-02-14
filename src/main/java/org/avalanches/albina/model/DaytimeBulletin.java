package org.avalanches.albina.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.avalanches.albina.model.enumerations.AvalancheType;
import org.avalanches.albina.model.enumerations.TriggerType;
import org.avalanches.albina.util.GlobalVariables;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

@Entity
@Table(name = "DAYTIME_BULLETINS")
public class DaytimeBulletin extends AbstractPersistentObject implements AvalancheInformationObject {

	@Column(name = "TRIGGER_TYPE")
	@Enumerated(EnumType.STRING)
	private TriggerType triggerType;

	@Column(name = "AVALANCHE_TYPE")
	@Enumerated(EnumType.STRING)
	private AvalancheType avalancheType;

	@Column(name = "VALID_FROM")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime validFrom;

	@Column(name = "VALID_UNTIL")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime validUntil;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "DAYTIME_BULLETIN_SUBREGION_BULLETINS", joinColumns = @JoinColumn(name = "DAYTIME_BULLETIN_ID"), inverseJoinColumns = @JoinColumn(name = "SUBREGION_BULLETIN_ID"))
	private Set<SubregionBulletin> subregionBulletins;

	public DaytimeBulletin() {
		this.subregionBulletins = new HashSet<SubregionBulletin>();
	}

	public DaytimeBulletin(JSONObject json) {
		this();

		this.triggerType = TriggerType.valueOf(json.getString("triggerType").replaceAll(" ", "_").toLowerCase());
		this.avalancheType = AvalancheType.valueOf(json.getString("avalancheType").toLowerCase());

		JSONObject validity = json.getJSONObject("validity");
		this.validFrom = new DateTime(validity.getString("from"));
		this.validUntil = new DateTime(validity.getString("until"));

		JSONArray subregionBulletins = json.getJSONArray("subregionBulletins");
		for (Object entry : subregionBulletins) {
			this.subregionBulletins.add(new SubregionBulletin((JSONObject) entry));
		}
	}

	public TriggerType getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(TriggerType triggerType) {
		this.triggerType = triggerType;
	}

	public AvalancheType getAvalancheType() {
		return avalancheType;
	}

	public void setAvalancheType(AvalancheType avalancheType) {
		this.avalancheType = avalancheType;
	}

	public DateTime getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(DateTime validFrom) {
		this.validFrom = validFrom;
	}

	public DateTime getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(DateTime validUntil) {
		this.validUntil = validUntil;
	}

	public Set<SubregionBulletin> getSubregionBulletins() {
		return subregionBulletins;
	}

	public void setSubregionBulletins(Set<SubregionBulletin> subregionBulletins) {
		this.subregionBulletins = subregionBulletins;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("triggerType", this.triggerType.toString());
		json.put("avalancheType", this.avalancheType.toString());

		JSONObject validity = new JSONObject();
		validity.put("from", this.validFrom.toString(GlobalVariables.formatterDateTime));
		validity.put("until", this.validUntil.toString(GlobalVariables.formatterDateTime));
		json.put("validity", validity);

		JSONArray subregionBulletins = new JSONArray();
		for (SubregionBulletin subregionBulletin : this.subregionBulletins) {
			subregionBulletins.put(subregionBulletin.toJSON());
		}
		json.put("subregionBulletins", subregionBulletins);

		return json;
	}

}
