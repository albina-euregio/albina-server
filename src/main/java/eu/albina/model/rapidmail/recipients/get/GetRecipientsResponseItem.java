package eu.albina.model.rapidmail.recipients.get;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Generated("com.robohorse.robopojogenerator")
public class GetRecipientsResponseItem {

	@JsonProperty("mailtype")
	private String mailtype;

	@JsonProperty("firstname")
	private String firstname;

	@JsonProperty("extra8")
	private String extra8;

	@JsonProperty("birthdate")
	private Object birthdate;

	@JsonProperty("extra9")
	private String extra9;

	@JsonProperty("gender")
	private String gender;

	@JsonProperty("_links")
	private GetRecipientsResponseLinks links;

	@JsonProperty("extra2")
	private String extra2;

	@JsonProperty("extra3")
	private String extra3;

	@JsonProperty("extra1")
	private String extra1;

	@JsonProperty("title")
	private String title;

	@JsonProperty("extra6")
	private String extra6;

	@JsonProperty("extra7")
	private String extra7;

	@JsonProperty("extra4")
	private String extra4;

	@JsonProperty("extra5")
	private String extra5;

	@JsonProperty("id")
	private String id;

	@JsonProperty("extra10")
	private String extra10;

	@JsonProperty("email")
	private String email;

	@JsonProperty("zip")
	private String zip;

	@JsonProperty("created")
	private String created;

	@JsonProperty("lastname")
	private String lastname;

	@JsonProperty("foreign_id")
	private String foreignId;

	@JsonProperty("recipientlist_id")
	private String recipientlistId;

	@JsonProperty("created_ip")
	private String createdIp;

	@JsonProperty("updated")
	private String updated;

	@JsonProperty("created_host")
	private String createdHost;

	@JsonProperty("status")
	private String status;

	public void setMailtype(String mailtype) {
		this.mailtype = mailtype;
	}

	public String getMailtype() {
		return mailtype;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setExtra8(String extra8) {
		this.extra8 = extra8;
	}

	public String getExtra8() {
		return extra8;
	}

	public void setBirthdate(Object birthdate) {
		this.birthdate = birthdate;
	}

	public Object getBirthdate() {
		return birthdate;
	}

	public void setExtra9(String extra9) {
		this.extra9 = extra9;
	}

	public String getExtra9() {
		return extra9;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getGender() {
		return gender;
	}

	public void setLinks(GetRecipientsResponseLinks links) {
		this.links = links;
	}

	public GetRecipientsResponseLinks getLinks() {
		return links;
	}

	public void setExtra2(String extra2) {
		this.extra2 = extra2;
	}

	public String getExtra2() {
		return extra2;
	}

	public void setExtra3(String extra3) {
		this.extra3 = extra3;
	}

	public String getExtra3() {
		return extra3;
	}

	public void setExtra1(String extra1) {
		this.extra1 = extra1;
	}

	public String getExtra1() {
		return extra1;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setExtra6(String extra6) {
		this.extra6 = extra6;
	}

	public String getExtra6() {
		return extra6;
	}

	public void setExtra7(String extra7) {
		this.extra7 = extra7;
	}

	public String getExtra7() {
		return extra7;
	}

	public void setExtra4(String extra4) {
		this.extra4 = extra4;
	}

	public String getExtra4() {
		return extra4;
	}

	public void setExtra5(String extra5) {
		this.extra5 = extra5;
	}

	public String getExtra5() {
		return extra5;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setExtra10(String extra10) {
		this.extra10 = extra10;
	}

	public String getExtra10() {
		return extra10;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getZip() {
		return zip;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getCreated() {
		return created;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setForeignId(String foreignId) {
		this.foreignId = foreignId;
	}

	public String getForeignId() {
		return foreignId;
	}

	public void setRecipientlistId(String recipientlistId) {
		this.recipientlistId = recipientlistId;
	}

	public String getRecipientlistId() {
		return recipientlistId;
	}

	public void setCreatedIp(String createdIp) {
		this.createdIp = createdIp;
	}

	public String getCreatedIp() {
		return createdIp;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public String getUpdated() {
		return updated;
	}

	public void setCreatedHost(String createdHost) {
		this.createdHost = createdHost;
	}

	public String getCreatedHost() {
		return createdHost;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "RecipientsItem{" + "mailtype = '" + mailtype + '\'' + ",firstname = '" + firstname + '\''
				+ ",extra8 = '" + extra8 + '\'' + ",birthdate = '" + birthdate + '\'' + ",extra9 = '" + extra9 + '\''
				+ ",gender = '" + gender + '\'' + ",_links = '" + links + '\'' + ",extra2 = '" + extra2 + '\''
				+ ",extra3 = '" + extra3 + '\'' + ",extra1 = '" + extra1 + '\'' + ",title = '" + title + '\''
				+ ",extra6 = '" + extra6 + '\'' + ",extra7 = '" + extra7 + '\'' + ",extra4 = '" + extra4 + '\''
				+ ",extra5 = '" + extra5 + '\'' + ",id = '" + id + '\'' + ",extra10 = '" + extra10 + '\'' + ",email = '"
				+ email + '\'' + ",zip = '" + zip + '\'' + ",created = '" + created + '\'' + ",lastname = '" + lastname
				+ '\'' + ",foreign_id = '" + foreignId + '\'' + ",recipientlist_id = '" + recipientlistId + '\''
				+ ",created_ip = '" + createdIp + '\'' + ",updated = '" + updated + '\'' + ",created_host = '"
				+ createdHost + '\'' + ",status = '" + status + '\'' + "}";
	}
}