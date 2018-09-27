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
import com.searchcode.app.dao.Repo;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.DataService;
import com.searchcode.app.service.IIndexService;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
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

    private final LoggerWrapper logger;
    private final DataService dataService;
    private final IIndexService indexService;
    private final Repo repo;
    private final Helpers helpers;

    public DeleteRepositoryJob() {
        this.dataService = Singleton.getDataService();
        this.indexService = Singleton.getIndexService();
        this.repo = Singleton.getRepo();
        this.helpers = Singleton.getHelpers();
        this.logger = Singleton.getLogger();
    }

    public void execute(JobExecutionContext context) {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        // Always attempt to clean the trash directory
        try {
            String newLocation = Properties.getProperties().getProperty(Values.TRASH_LOCATION, Values.DEFAULT_TRASH_LOCATION);
            FileUtils.deleteDirectory(Paths.get(newLocation).toFile());
        } catch (IOException ex) {
            this.logger.severe(String.format("fb813e4f::error in class %s exception %s when trying to clean trash directory", ex.getClass(), ex.getMessage()));
        }

        // TODO make this loop able to be set in properties file
        for (int i = 0; i < 10; i++) {
            List<String> persistentDelete = this.dataService.getPersistentDelete();
            if (persistentDelete.isEmpty()) {
                return;
            }

            if (this.indexService.getReindexingAll()) {
                return;
            }

            Optional<RepoResult> repoResult = this.repo.getRepoByName(persistentDelete.get(0));
            if (!repoResult.isPresent()) {
                this.dataService.removeFromPersistentDelete(persistentDelete.get(0));
                return;
            }

            repoResult.ifPresent(x -> Singleton.getUniqueGitRepoQueue().delete(x));

            if (Singleton.getRunningIndexRepoJobs().containsKey(repoResult.map(RepoResult::getName).orElse(Values.EMPTYSTRING))) {
                return;
            }

            repoResult.ifPresent(x -> {
                try {
                    this.logger.info(String.format("050ac264::deleting repository %s", x.getName()));
                    this.indexService.deleteByRepo(x);
                    String repoLocations = Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION);

                    // remove the directory
                    this.helpers.tryDelete(repoLocations + x.getDirectoryName() + "/");

                    // Remove from the database
                    this.repo.deleteRepoByName(x.getName());
                    // Remove from the persistent queue
                    this.dataService.removeFromPersistentDelete(x.getName());
                } catch (IOException ex) {
                    this.logger.severe(String.format("52998af6::error in class %s exception %s for repository %s", ex.getClass(), ex.getMessage(), x.getName()));
                }
            });
        }
    }
}
