/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 *
 * After the following date 27 August 2019 this software version '1.2.3' or '1.2.4' is dual licenced under the
 * Fair Source Licence included in the LICENSE.txt file or under the GNU General Public License Version 3 with terms
 * specified at https://www.gnu.org/licenses/gpl-3.0.txt
 */

package com.searchcode.app.util;

import com.searchcode.app.model.RepoResult;

import java.util.Queue;

/**
 * As named this is a queue where the elements are guranteed to be unique even if you add
 * the repoResult multiple times there will only ever be a single copy in the queue.
 * Adding the same value will also not adjust its position in the queue which is
 * by default FIFO.
 */
public class UniqueRepoQueue {
    private Queue<RepoResult> repoIndexQueue;

    public UniqueRepoQueue(Queue<RepoResult> repoResultQueue) {
        this.repoIndexQueue = repoResultQueue;
    }

    public synchronized void add(RepoResult repoResult) {

        if (repoResult == null) {
            return;
        }

        if (!repoIndexQueue.contains(repoResult)) {
            repoIndexQueue.add(repoResult);
        }
    }

    public synchronized void delete(RepoResult repoResult) {
        repoIndexQueue.remove(repoResult);
    }

    public synchronized RepoResult poll() {
        return repoIndexQueue.poll();
    }

    public synchronized int size() {
        return repoIndexQueue.size();
    }
}
