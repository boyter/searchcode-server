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

        RepoResult rr = new RepoResult()
                .setRowId(1)
                .setName("exists")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}");

        queue.add(rr);
        queue.add(rr);

        assertEquals(1, queue.size());
    }

    public void testEnqueueSameRepoMultipleTimes() {
        UniqueRepoQueue queue = new UniqueRepoQueue(new ConcurrentArrayQueue<>());

        RepoResult rr1 = new RepoResult()
                .setRowId(1)
                .setName("exists")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}");
        RepoResult rr2 = new RepoResult()
                .setRowId(1)
                .setName("exists")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}");

        queue.add(rr1);
        queue.add(rr2);

        assertEquals(1, queue.size());
    }

    public void testEnqueueDifferent() {
        UniqueRepoQueue queue = new UniqueRepoQueue(new ConcurrentArrayQueue<>());

        RepoResult rr1 = new RepoResult()
                .setRowId(1)
                .setName("name1")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}");
        RepoResult rr2 = new RepoResult()
                .setRowId(2)
                .setName("name2")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}");

        queue.add(rr1);
        queue.add(rr2);

        assertEquals(2, queue.size());
    }

    public void testOrderAndPoll() {
        UniqueRepoQueue queue = new UniqueRepoQueue(new ConcurrentArrayQueue<>());

        RepoResult rr1 = new RepoResult()
                .setRowId(1)
                .setName("name1")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}");
        RepoResult rr2 = new RepoResult()
                .setRowId(2)
                .setName("name2")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}");

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

        RepoResult rr1 = new RepoResult()
                .setRowId(1)
                .setName("name1")
                .setScm("git")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}");
        RepoResult rr2 = new RepoResult()
                .setRowId(2)
                .setName("name2")
                .setScm("svn")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}");

        queue1.add(rr1);
        queue2.add(rr2);

        assertEquals(1, queue1.size());
        assertEquals(1, queue2.size());
        assertTrue(rr1.equals(queue1.poll()));
        assertTrue(rr2.equals(queue2.poll()));
    }

    public void testQueueClear() {
        UniqueRepoQueue queue = new UniqueRepoQueue();

        queue.add(new RepoResult()
                .setRowId(1)
                .setName("name1")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"));
        queue.add(new RepoResult()
                .setRowId(2)
                .setName("name2")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"));

        assertThat(queue.size()).isEqualTo(2);
        queue.clear();
        assertThat(queue.size()).isEqualTo(0);
    }
}
