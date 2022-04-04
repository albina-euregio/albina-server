/*******************************************************************************
 * Copyright (C) 2022 Norbert Lanzanasto
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
package eu.albina.util;

public class GlobalVariablesTest {

	public static AutoCloseable withLauegiVariables() {
		final String serverImagesUrl = GlobalVariables.serverImagesUrl;
		GlobalVariables.serverImagesUrl = "https://static.lauegi.report/images/";
		GlobalVariables.serverMapsUrl = "https://static.lauegi.report/albina_files";
		GlobalVariables.serverWebsiteUrl = "https://www.lauegi.report/";
		return () -> {
			GlobalVariables.serverImagesUrl = serverImagesUrl;
			GlobalVariables.serverMapsUrl = "";
			GlobalVariables.serverWebsiteUrl = "";
		};
	}

}
