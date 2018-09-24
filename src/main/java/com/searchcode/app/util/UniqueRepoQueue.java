/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.util;

import com.searchcode.app.model.RepoResult;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * As named this is a queue where the elements are guranteed to be unique even if you add
 * the repoResult multiple times there will only ever be a single copy in the queue.
 * Adding the same value will also not adjust its position in the queue which is
 * by default FIFO.
 */
public class UniqueRepoQueue {
    private Queue<RepoResult> repoIndexQueue;

    public UniqueRepoQueue() {
        this.repoIndexQueue = new ConcurrentLinkedQueue<>();
    }

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

    public synchronized void clear() {
        this.repoIndexQueue.clear();
    }
}
