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
package eu.albina.model.rapidmail.mailings;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Generated("com.robohorse.robopojogenerator")
public class RapidMailMailingsResponseLinks {

	@JsonProperty("self")
	private RapidMailMailingsResponseHref self;

	@JsonProperty("first")
	private RapidMailMailingsResponseHref first;

	@JsonProperty("last")
	private RapidMailMailingsResponseHref last;

	public void setSelf(RapidMailMailingsResponseHref self) {
		this.self = self;
	}

	public RapidMailMailingsResponseHref getSelf() {
		return self;
	}

	public void setFirst(RapidMailMailingsResponseHref first) {
		this.first = first;
	}

	public RapidMailMailingsResponseHref getFirst() {
		return first;
	}

	public void setLast(RapidMailMailingsResponseHref last) {
		this.last = last;
	}

	public RapidMailMailingsResponseHref getLast() {
		return last;
	}

	@Override
	public String toString() {
		return "Links{" + "self = '" + self + '\'' + "first = '" + first + '\'' + "last = '" + last + '\'' + "}";
	}
}
