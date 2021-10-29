/*******************************************************************************
 * Copyright (C) 2021 Norbert Lanzanasto
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
package eu.albina.jobs;

import java.io.IOException;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.util.GlobalVariables;

/**
 * A {@code org.quartz.Job} handling all the tasks and logic necessary to
 * automatically publish blog posts.
 *
 * @author Norbert Lanzanasto
 *
 */
public class TmpDeletionJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(TmpDeletionJob.class);

	/**
	 * Execute all necessary tasks to delete tmp files.
	 *
	 * @param arg0
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("TmpDeletion job triggered!");
		try {
			File tmpDir = new File(GlobalVariables.getTmpMapsPath());
			if (tmpDir.exists())
				FileUtils.cleanDirectory(tmpDir);
			logger.info("Temporary files deleted.");
		} catch (IOException e) {
			logger.warn("Temporary files could not be deleted!");
		}
		logger.info("TmpDeletion job finished!");;
	}
}
