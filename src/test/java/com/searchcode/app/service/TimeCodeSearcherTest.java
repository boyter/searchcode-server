/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
 */

package com.searchcode.app.service;

import com.searchcode.app.dto.*;
import junit.framework.TestCase;
import org.eclipse.jetty.util.ConcurrentArrayQueue;

import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeCodeSearcherTest extends TestCase {

    public void testTest() {}


//    public void testTimeIndexIndexCreationAndSearch() {
//        try {
//            String contents = "this is some code that should be found";
//
//            CodeIndexDocument cid = new CodeIndexDocument("repoLocationRepoNameLocationFilename", "", "fileName", "fileLocation", "fileLocationFilename", "md5hash", "languageName", 0, contents, "repoRemoteLocation", "codeOwner");
//            cid.setRevision("99a5a271063def87b2473be79ce6f840d42d1f95");
//            cid.setYearMonthDay("20160101");
//
//            Queue queue = new ConcurrentArrayQueue<CodeIndexDocument>();
//            queue.add(cid);
//
//            CodeIndexer.indexTimeDocuments(queue);
//
//            TimeCodeSearcher cs = new TimeCodeSearcher();
//
//            assertThat(cs.search("this", 0).getCodeResultList()).hasAtLeastOneElementOfType(CodeResult.class);
//            assertThat(cs.search("this", 0).getLanguageFacetResults()).hasAtLeastOneElementOfType(CodeFacetLanguage.class);
//            assertThat(cs.search("this", 0).getOwnerFacetResults()).hasAtLeastOneElementOfType(CodeFacetOwner.class);
//            assertThat(cs.search("this", 0).getRepoFacetYear()).hasAtLeastOneElementOfType(CodeFacetYear.class);
//            assertThat(cs.search("this", 0).getRepoFacetYearMonth()).hasAtLeastOneElementOfType(CodeFacetYearMonth.class);
//            assertThat(cs.search("this", 0).getRepoFacetYearMonthDay()).hasAtLeastOneElementOfType(CodeFacetYearMonthDay.class);
//
//            assertThat(cs.search("this", 0).getRepoFacetRevision().get(0).getRevision()).isEqualTo("99a5a271063def87b2473be79ce6f840d42d1f95");
//            assertThat(cs.search("this", 0).getRepoFacetYear().get(0).getYear()).isEqualTo("2016");
//            assertThat(cs.search("this", 0).getRepoFacetYearMonth().get(0).getYearMonth()).isEqualTo("201601");
//            assertThat(cs.search("this", 0).getRepoFacetYearMonthDay().get(0).getYearMonthDay()).isEqualTo("20160101");
//        }
//        catch(Exception ex) {
//            assertTrue(false);
//        }
//    }
//
//    public void testTimeTEMP() {
//        try {
//            TimeCodeSearcher cs = new TimeCodeSearcher();
//
//            SearchResult searchResult = cs.search("import && (datemonthyear:201201)", 0);
//            //SearchResult searchResult = cs.search("import && (dateyearmonthday:20120106)", 0);
//
//            assertThat(searchResult.getTotalHits()).isNotZero();
//        }
//        catch(Exception ex) {
//            assertTrue(false);
//        }
//    }
}
