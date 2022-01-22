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
package eu.albina.model.publication.rapidmail.recipientlist;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Generated("com.robohorse.robopojogenerator")
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
