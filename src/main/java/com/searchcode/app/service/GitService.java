/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.service;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;

public class GitService {

    /**
     * Given a repository location, revision and file path will retrieve that files contents. N.B. it returns the whole
     * file so you MAY end up running into serious memory issues, and should be aware of this
     */
    public String fetchFileRevision(String repoLocation, String revision, String filePath) throws MissingObjectException, IncorrectObjectTypeException, IOException {
        Repository localRepository = new FileRepository(new File(repoLocation));

        ObjectId id = localRepository.resolve(revision);
        ObjectReader reader = localRepository.newObjectReader();

        try {
            RevWalk walk = new RevWalk(reader);
            RevCommit commit = walk.parseCommit(id);
            RevTree tree = commit.getTree();
            TreeWalk treewalk = TreeWalk.forPath(reader, filePath, tree);

            if (treewalk != null) {
                byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
                return new String(data, "utf-8");
            } else {
                return "";
            }
        } finally {
            reader.close();
        }
    }
}
