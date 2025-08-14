// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.MoreFiles;

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
			Path tmpDir = Paths.get(GlobalVariables.getTmpMapsPath());
			if (Files.exists(tmpDir)) {
				MoreFiles.deleteDirectoryContents(tmpDir);
			}
			logger.info("Temporary files deleted.");
		} catch (IOException e) {
			logger.warn("Temporary files could not be deleted!");
		}
		logger.info("TmpDeletion job finished!");;
	}
}
