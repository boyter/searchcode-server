package com.searchcode.app.util;

import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;
import org.eclipse.jetty.util.ConcurrentArrayQueue;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class UniqueRepoQueueTest extends TestCase {
    public void testEnqueNull() {
        UniqueRepoQueue queue = new UniqueRepoQueue(new ConcurrentArrayQueue<>());

        queue.add(null);
    }

    public void testEnqueueMultipleTimes() {
        UniqueRepoQueue queue = new UniqueRepoQueue(new ConcurrentArrayQueue<>());

        RepoResult rr = new RepoResult(1, "name", "scm", "url", "username", "password", "source", "branch", "");

        queue.add(rr);
        queue.add(rr);

        assertEquals(1, queue.size());
    }

    public void testEnqueueSameRepoMultipleTimes() {
        UniqueRepoQueue queue = new UniqueRepoQueue(new ConcurrentArrayQueue<>());

        RepoResult rr1 = new RepoResult(1, "name", "scm", "url", "username", "password", "source", "branch", "");
        RepoResult rr2 = new RepoResult(1, "name", "scm", "url", "username", "password", "source", "branch", "");

        queue.add(rr1);
        queue.add(rr2);

        assertEquals(1, queue.size());
    }

    public void testEnqueueDifferent() {
        UniqueRepoQueue queue = new UniqueRepoQueue(new ConcurrentArrayQueue<>());

        RepoResult rr1 = new RepoResult(1, "name", "scm", "url", "username", "password", "source", "branch", "");
        RepoResult rr2 = new RepoResult(2, "name2", "scm", "url", "username", "password", "source", "branch", "");

        queue.add(rr1);
        queue.add(rr2);

        assertEquals(2, queue.size());
    }

    public void testOrderAndPoll() {
        UniqueRepoQueue queue = new UniqueRepoQueue(new ConcurrentArrayQueue<>());

        RepoResult rr1 = new RepoResult(1, "name", "scm", "url", "username", "password", "source", "branch", "");
        RepoResult rr2 = new RepoResult(2, "name2", "scm", "url", "username", "password", "source", "branch", "");

        queue.add(rr1);
        queue.add(rr2);

        assertEquals(2, queue.size());
        assertTrue(rr1.equals(queue.poll()));
        assertEquals(1, queue.size());
        assertTrue(rr2.equals(queue.poll()));
        assertEquals(0, queue.size());
    }


    public void testBleedingBetweenUniqueQueues() {
        UniqueRepoQueue queue1 = Singleton.getUniqueGitRepoQueue();
        UniqueRepoQueue queue2 = Singleton.getUniqueSvnRepoQueue();
        queue1.clear();
        queue2.clear();

        RepoResult rr1 = new RepoResult(1, "name", "git", "url", "username", "password", "source", "branch", "");
        RepoResult rr2 = new RepoResult(2, "name2", "svn", "url", "username", "password", "source", "branch", "");

        queue1.add(rr1);
        queue2.add(rr2);

        assertEquals(1, queue1.size());
        assertEquals(1, queue2.size());
        assertTrue(rr1.equals(queue1.poll()));
        assertTrue(rr2.equals(queue2.poll()));
    }

    public void testQueueClear() {
        UniqueRepoQueue queue = new UniqueRepoQueue();

        queue.add(new RepoResult(1, "name", "git", "url", "username", "password", "source", "branch", ""));
        queue.add(new RepoResult(2, "name2", "svn", "url", "username", "password", "source", "branch", ""));

        assertThat(queue.size()).isEqualTo(2);
        queue.clear();
        assertThat(queue.size()).isEqualTo(0);
    }
}
