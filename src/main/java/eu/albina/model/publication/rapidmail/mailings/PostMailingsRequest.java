/*******************************************************************************
 * Copyright (C) 2019 Clesius srl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.model.publication.rapidmail.mailings;

import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Generated("com.robohorse.robopojogenerator")
public class PostMailingsRequest {

	@JsonProperty("check_robinson")
	private String checkRobinson;

	@JsonProperty("from_email")
	private String fromEmail;

	// @JsonProperty("attachments")
	// private Integer attachments;

	@JsonProperty("file")
	private PostMailingsRequestPostFile file;

	@JsonProperty("check_ecg")
	private String checkEcg;

	@JsonProperty("subject")
	private String subject;

	@JsonProperty("destinations")
	private List<PostMailingsRequestDestination> destinations;

	@JsonProperty("host")
	private String host;

	@JsonProperty("send_at")
	private String sendAt;

	// @JsonProperty("feature_mailingsplit")
	// private String featureMailingsplit;

	@JsonProperty("from_name")
	private String fromName;

	@JsonProperty("status")
	private String status;

	public void setCheckRobinson(String checkRobinson) {
		this.checkRobinson = checkRobinson;
	}

	public String getCheckRobinson() {
		return checkRobinson;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	// public void setAttachments(Integer attachments){
	// this.attachments = attachments;
	// }
	//
	// public Integer getAttachments(){
	// return attachments;
	// }

	public void setFile(PostMailingsRequestPostFile file) {
		this.file = file;
	}

	public PostMailingsRequestPostFile getFile() {
		return file;
	}

	public void setCheckEcg(String checkEcg) {
		this.checkEcg = checkEcg;
	}

	public String getCheckEcg() {
		return checkEcg;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	public void setDestinations(List<PostMailingsRequestDestination> destinations) {
		this.destinations = destinations;
	}

	public List<PostMailingsRequestDestination> getDestinations() {
		return destinations;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setSendAt(String sendAt) {
		this.sendAt = sendAt;
	}

	public String getSendAt() {
		return sendAt;
	}

	// public void setFeatureMailingsplit(String featureMailingsplit){
	// this.featureMailingsplit = featureMailingsplit;
	// }
	//
	// public String getFeatureMailingsplit(){
	// return featureMailingsplit;
	// }

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getFromName() {
		return fromName;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "SendMail{" + "check_robinson = '" + checkRobinson + '\'' + ",from_email = '" + fromEmail + '\'' +
		// ",attachments = '" + attachments + '\'' +
				",file = '" + file + '\'' + ",check_ecg = '" + checkEcg + '\'' + ",subject = '" + subject + '\''
				+ ",destinations = '" + destinations + '\'' + ",host = '" + host + '\'' + ",send_at = '" + sendAt + '\''
				+
				// ",feature_mailingsplit = '" + featureMailingsplit + '\'' +
				",from_name = '" + fromName + '\'' + ",status = '" + status + '\'' + "}";
	}

	public PostMailingsRequest checkRobinson(String checkRobinson) {
		this.checkRobinson = checkRobinson;
		return this;
	}

	public PostMailingsRequest fromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
		return this;
	}

	// public PostMailingsRequest attachments(Integer attachments) {
	// this.attachments = attachments;
	// return this;
	// }

	public PostMailingsRequest file(PostMailingsRequestPostFile file) {
		this.file = file;
		return this;
	}

	public PostMailingsRequest checkEcg(String checkEcg) {
		this.checkEcg = checkEcg;
		return this;
	}

	public PostMailingsRequest subject(String subject) {
		this.subject = subject;
		return this;
	}

	public PostMailingsRequest destinations(List<PostMailingsRequestDestination> destinations) {
		this.destinations = destinations;
		return this;
	}

	public PostMailingsRequest host(String host) {
		this.host = host;
		return this;
	}

	public PostMailingsRequest sendAt(String sendAt) {
		this.sendAt = sendAt;
		return this;
	}

	// public PostMailingsRequest featureMailingsplit(String featureMailingsplit) {
	// this.featureMailingsplit = featureMailingsplit;
	// return this;
	// }

	public PostMailingsRequest fromName(String fromName) {
		this.fromName = fromName;
		return this;
	}

	public PostMailingsRequest status(String status) {
		this.status = status;
		return this;
	}
}
