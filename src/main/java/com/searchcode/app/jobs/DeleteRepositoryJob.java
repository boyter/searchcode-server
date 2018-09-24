/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.jobs;

import com.searchcode.app.config.Values;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Properties;
import org.apache.commons.io.FileUtils;
import org.quartz.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The job which deletes repositories from the database index and disk where one exists in the deletion queue.
 * TODO fix race condition where it can start deleting while the repo has been re-added to be indexed
 * TODO add some tests for this to ensure everything such as the early return occurs correctly
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class DeleteRepositoryJob implements Job {
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        // Always attempt to clean the trash directory
        try {
            String newLocation = Properties.getProperties().getProperty(Values.TRASH_LOCATION, Values.DEFAULT_TRASH_LOCATION);
            FileUtils.deleteDirectory(Paths.get(newLocation).toFile());
        } catch (IOException ex) {
            Singleton.getLogger().warning("Error when trying to clean trash " + ex);
        }

        // TODO make this loop able to be set in properties file
        for (int i = 0; i < 10; i++) {
            List<String> persistentDelete = Singleton.getDataService().getPersistentDelete();
            if (persistentDelete.isEmpty()) {
                return;
            }

            if (Singleton.getIndexService().getReindexingAll()) {
                return;
            }

            Optional<RepoResult> repoResult = Singleton.getRepo().getRepoByName(persistentDelete.get(0));
            if (!repoResult.isPresent()) {
                Singleton.getDataService().removeFromPersistentDelete(persistentDelete.get(0));
                return;
            }

            repoResult.ifPresent(x -> Singleton.getUniqueGitRepoQueue().delete(x));

            if (Singleton.getRunningIndexRepoJobs().containsKey(repoResult.map(RepoResult::getName).orElse(Values.EMPTYSTRING))) {
                return;
            }

            repoResult.ifPresent(x -> {
                try {
                    Singleton.getLogger().info("Deleting repository. " + x.getName());
                    Singleton.getIndexService().deleteByRepo(x);
                    String repoLocations = Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION);

                    // remove the directory
                    Singleton.getHelpers().tryDelete(repoLocations + x.getDirectoryName() + "/");

                    // Remove from the database
                    Singleton.getRepo().deleteRepoByName(x.getName());
                    // Remove from the persistent queue
                    Singleton.getDataService().removeFromPersistentDelete(x.getName());
                } catch (IOException ex) {
                    Singleton.getLogger().warning("Error when trying to remove repository with exception " + ex);
                }
            });
        }
    }
}
