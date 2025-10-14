// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication.rapidmail.recipientlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
public class RapidMailRecipientListResponse {

	@JsonProperty("_links")
	private RapidMailRecipientListResponseLinks links;

	@JsonProperty("_embedded")
	private RapidMailRecipientListResponseEmbedded embedded;

	@JsonProperty("page")
	private int page;

	@JsonProperty("total_items")
	private int totalItems;

	@JsonProperty("page_count")
	private int pageCount;

	@JsonProperty("page_size")
	private int pageSize;

	public void setLinks(RapidMailRecipientListResponseLinks links) {
		this.links = links;
	}

	public RapidMailRecipientListResponseLinks getLinks() {
		return links;
	}

	public void setEmbedded(RapidMailRecipientListResponseEmbedded embedded) {
		this.embedded = embedded;
	}

	public RapidMailRecipientListResponseEmbedded getEmbedded() {
		return embedded;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPage() {
		return page;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

	public int getTotalItems() {
		return totalItems;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageSize() {
		return pageSize;
	}

	@Override
	public String toString() {
		return "RapidMailRecipientListResponse{" + "_links = '" + links + '\'' + ",_embedded = '" + embedded + '\''
				+ ",page = '" + page + '\'' + ",total_items = '" + totalItems + '\'' + ",page_count = '" + pageCount
				+ '\'' + ",page_size = '" + pageSize + '\'' + "}";
	}
}
