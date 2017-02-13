package com.searchcode.app.util;

import com.searchcode.app.dto.*;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.classifier.Classifier;
import com.searchcode.app.util.classifier.FileClassifier;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SearchcodeLibTest extends TestCase {

    public void testCleanPipeline() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.codeCleanPipeline("{AB3FBE3A-410C-4FB2-84E0-B2D3434D1995}");
        assertThat(actual).contains(" AB3FBE3A-410C-4FB2-84E0-B2D3434D1995 ");
        assertThat(actual).contains(" {AB3FBE3A-410C-4FB2-84E0-B2D3434D1995} ");
        assertThat(actual).contains(" AB3FBE3A 410C 4FB2 84E0 B2D3434D1995 ");
    }

    public void testCleanPipelineTwo() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.codeCleanPipeline("\"_updatedDate\"");

        assertThat(actual).contains(" _updatedDate ");
        assertThat(actual).contains(" updatedDate ");
        assertThat(actual).contains(" \"_updatedDate\" ");
    }

    public void testCleanPipelineThree() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.codeCleanPipeline("'shop_order_log',");

        assertTrue(actual.indexOf(" 'shop_order_log' ") != -1);
    }

    public void testCleanPipelineCustom() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.codeCleanPipeline("context.config.URL_REWRITE.iteritems():");

        assertThat(actual).contains(" URL_REWRITE ");
    }

    public void testCleanPipelineCustom2() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.codeCleanPipeline("task :install_something do");

        assertThat(actual).contains(" install_something ");
    }

    public void testIsBinary() {
        SearchcodeLib sl = new SearchcodeLib();

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("a");

        assertFalse(sl.isBinary(codeLines, "").isBinary());
    }

    public void testIsBinaryAllNonAscii() {
        SearchcodeLib sl = new SearchcodeLib();

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("你");

        assertTrue(sl.isBinary(codeLines, "").isBinary());
    }

    public void testIsBinaryFalse() {
        SearchcodeLib sl = new SearchcodeLib();

        String minified = "";
        for (int i=0; i < 256; i++) {
            minified += "a";
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified);

        assertFalse(sl.isBinary(codeLines, "").isBinary());
    }

    public void testIsBinaryTrue() {
        SearchcodeLib sl = new SearchcodeLib();

        String minified = "";
        for (int i=0; i < 256; i++) {
            minified += "你";
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified);

        assertTrue(sl.isBinary(codeLines, "").isBinary());
    }

    public void testIsBinaryWhiteListedExtension() {
        SearchcodeLib sl = new SearchcodeLib();
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("你你你你你你你你你你你你你你你你你你你你你你你你你你你");

        FileClassifier fileClassifier = new FileClassifier();

        for(Classifier classifier: fileClassifier.getClassifier()) {
            for(String extension: classifier.extensions) {
                BinaryFinding isBinary = sl.isBinary(codeLines, "myfile." + extension);
                assertThat(isBinary.isBinary()).isFalse();
            }
        }
    }

    public void testIsBinaryWhiteListedPropertyExtension() {
        SearchcodeLib sl = new SearchcodeLib();
        sl.WHITELIST = new String[1];
        sl.WHITELIST[0] = "java";
        FileClassifier fileClassifier = new FileClassifier();
        sl.fileClassifier = fileClassifier;
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("你你你你你你你你你你你你你你你你你你你你你你你你你你你");

        assertThat(sl.isBinary(codeLines, "myfile.JAVA").isBinary()).isFalse();
    }

    public void testIsBinaryBlackListedPropertyExtension() {
        SearchcodeLib sl = new SearchcodeLib();

        sl.BLACKLIST = new String[1];
        sl.BLACKLIST[0] = "png";

        FileClassifier fileClassifier = new FileClassifier();
        fileClassifier.setClassifier(new ArrayList<>());
        sl.fileClassifier = fileClassifier;

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("this file is not binary");

        assertThat(sl.isBinary(codeLines, "myfile.PNG").isBinary()).isTrue();
    }

    public void testIsBinaryEmptyTrue() {
        SearchcodeLib sl = new SearchcodeLib();
        ArrayList<String> codeLines = new ArrayList<>();
        assertTrue(sl.isBinary(codeLines, "").isBinary());
    }

    public void testIsBinaryEdge1() {
        SearchcodeLib sl = new SearchcodeLib();

        String minified = "";
        for (int i=0; i < 95; i++) {
            minified += "你";
        }
        minified += "aaaaa";

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified);

        assertThat(sl.isBinary(codeLines, "").isBinary()).isTrue();
    }

    public void testIsBinaryEdge2() {
        SearchcodeLib sl = new SearchcodeLib();

        String minified = "";
        for (int i=0; i < 96; i++) {
            minified += "你";
        }
        minified += "aaaa";

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified);

        assertTrue(sl.isBinary(codeLines, "").isBinary());
    }

    public void testIsBinaryEdge3() {
        SearchcodeLib sl = new SearchcodeLib();

        String minified = "";
        for (int i=0; i < 200; i++) {
            minified += "a";
        }
        ArrayList<String> codeLines = new ArrayList<>();

        for (int i=0; i < 200; i++) {
            codeLines.add(minified);
        }

        assertFalse(sl.isBinary(codeLines, "").isBinary());
    }

    public void testIsMinifiedTrue() {
        SearchcodeLib sl = new SearchcodeLib();

        String minified = "";
        for (int i=0; i < 256; i++) {
            minified += "a";
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified);

        assertTrue(sl.isMinified(codeLines, "something.something"));
    }

    public void testIsMinifiedWhiteListAlwaysWins() {
        SearchcodeLib sl = new SearchcodeLib();


        ArrayList<String> whiteList = new ArrayList<>();
        whiteList.add("something");
        sl.WHITELIST = whiteList.toArray(new String[whiteList.size()]);

        String minified = "";
        for (int i=0; i < 500; i++) {
            minified += "a";
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified);

        assertFalse(sl.isMinified(codeLines, "something.something"));
    }

    public void testIsMinifiedFalse() {
        SearchcodeLib sl = new SearchcodeLib();

        String minified = "";
        for (int i=0; i < 255; i++) {
            minified += "a";
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified);

        assertFalse(sl.isMinified(codeLines, "something.something"));
    }

    public void testCodeOwnerSameTimeDifferntCount() {
        SearchcodeLib sl = new SearchcodeLib();

        List<CodeOwner> codeOwners = new ArrayList<>();
        codeOwners.add(new CodeOwner("Ben", 20, 1449809107));
        codeOwners.add(new CodeOwner("Tim", 50, 1449809107));

        String result = sl.codeOwner(codeOwners);
        assertEquals("Tim", result);
    }

    public void testCodeOwnerManyOwners() {
        SearchcodeLib sl = new SearchcodeLib();

        // 86400 seconds in a day
        int daySeconds = 86400;

        long currentUnix = System.currentTimeMillis() / 1000L;

        List<CodeOwner> codeOwners = new ArrayList<>();
        codeOwners.add(new CodeOwner("Ben", 250, ((int) currentUnix - (daySeconds * 22 ))));
        codeOwners.add(new CodeOwner("Steve", 5, ((int) currentUnix - (daySeconds * 50 ))));
        codeOwners.add(new CodeOwner("Tim", 1, (int) currentUnix - (daySeconds * 1)));

        String result = sl.codeOwner(codeOwners);
        assertEquals("Tim", result);
    }

    public void testCodeOwnerManyOwnersFirstWins() {
        SearchcodeLib sl = new SearchcodeLib();

        // 86400 seconds in a day
        int daySeconds = 86400;

        long currentUnix = System.currentTimeMillis() / 1000L;

        List<CodeOwner> codeOwners = new ArrayList<>();
        codeOwners.add(new CodeOwner("Ben", 250,  (int)currentUnix - (daySeconds * 22 )));
        codeOwners.add(new CodeOwner("Steve", 5,  (int)currentUnix - (daySeconds * 50 )));
        codeOwners.add(new CodeOwner("Tim",   1,  (int)currentUnix - (daySeconds * 1  )));
        codeOwners.add(new CodeOwner("Terry", 1,  (int)currentUnix - (daySeconds * 1  )));
        codeOwners.add(new CodeOwner("Zhang", 1,  (int)currentUnix - (daySeconds * 1  )));

        String result = sl.codeOwner(codeOwners);
        assertEquals("Tim", result);
    }

    public void testCodeOwnerManyOwnersRandom() {
        SearchcodeLib sl = new SearchcodeLib();

        // 86400 seconds in a day
        int daySeconds = 86400;

        long currentUnix = System.currentTimeMillis() / 1000L;

        List<CodeOwner> codeOwners = new ArrayList<>();
        codeOwners.add(new CodeOwner("Ben",  40,  (int)currentUnix - (daySeconds * 365 )));
        codeOwners.add(new CodeOwner("Steve", 5,  (int)currentUnix - (daySeconds * 50  )));
        codeOwners.add(new CodeOwner("Tim",   1,  (int)currentUnix - (daySeconds * 1   )));
        codeOwners.add(new CodeOwner("Terry", 1,  (int)currentUnix - (daySeconds * 1   )));
        codeOwners.add(new CodeOwner("Zhang", 8,  (int)currentUnix - (daySeconds * 1   )));

        String result = sl.codeOwner(codeOwners);
        assertEquals("Zhang", result);
    }

    public void testCodeOwnerManyOwnersOldFile() {
        SearchcodeLib sl = new SearchcodeLib();

        // 86400 seconds in a day
        int daySeconds = 86400;

        long currentUnix = System.currentTimeMillis() / 1000L;

        List<CodeOwner> codeOwners = new ArrayList<>();
        codeOwners.add(new CodeOwner("Ben",  40,  (int)currentUnix - (daySeconds * 365 )));
        codeOwners.add(new CodeOwner("Steve", 5,  (int)currentUnix - (daySeconds * 300  )));

        String result = sl.codeOwner(codeOwners);
        assertEquals("Ben", result);
    }

    public void testSplitKeywords() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.splitKeywords("testSplitKeywords");
        assertEquals(" test Split Keywords", actual);
    }

    public void testSplitKeywords2() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.splitKeywords("map.put(\"isCommunity\", ISCOMMUNITY);");
        assertEquals(" is Community", actual);
    }

    public void testSplitKeywords3() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.splitKeywords("TestSplitKeywords");
        assertEquals(" Test Split Keywords", actual);
    }

    public void testInterestingKeywords() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.findInterestingKeywords("PURIFY_EXE=/depot/pure/purify.i386_linux2.7.4.14/purify");
        assertEquals(" i386 linux2.7.4", actual);
    }

    public void testInterestingKeywordsNull() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.findInterestingKeywords(null);
        assertThat(actual).isEqualTo("");
    }

    public void testInterestingCharacters() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.findInterestingCharacters("this 你好 chinese");
        assertThat(actual).contains(" 你 ");
        assertThat(actual).contains(" 好 ");
    }

    public void testInterestingCharactersNullExpectEmpty() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.findInterestingCharacters(null);
        assertThat(actual).isEqualTo("");
    }

    public void testLanguageGuesserText() {
        SearchcodeLib sl = new SearchcodeLib();
        String language = sl.languageGuesser("test.txt", new ArrayList<>());
        assertEquals("Text", language);
    }

    public void testLanguageGuesserXAML() {
        SearchcodeLib sl = new SearchcodeLib();
        String language = sl.languageGuesser("test.xaml", new ArrayList<>());
        assertEquals("XAML", language);
    }

    public void testLanguageGuesserASPNET() {
        SearchcodeLib sl = new SearchcodeLib();
        String language = sl.languageGuesser("test.ascx", new ArrayList<>());
        assertEquals("ASP.Net", language);
    }

    public void testLanguageGuesserHTML() {
        SearchcodeLib sl = new SearchcodeLib();
        String language = sl.languageGuesser("test.html", new ArrayList<>());
        assertEquals("HTML", language);
    }

    public void testLanguageGuesserUnknown() {
        SearchcodeLib sl = new SearchcodeLib();
        String language = sl.languageGuesser("test.shouldnotexist", new ArrayList<>());
        assertEquals("Unknown", language);
    }

    public void testLanguageGuesserNoExtension() {
        SearchcodeLib sl = new SearchcodeLib();
        String language = sl.languageGuesser("Jenkinsfile", new ArrayList<>());
        assertEquals("Jenkins Buildfile", language);
    }

    public void testLanguageGuesseKotlin() {
        SearchcodeLib sl = new SearchcodeLib();
        String language = sl.languageGuesser("test.kt", new ArrayList<>());
        assertEquals("Kotlin", language);
    }

    // TODO update this with actual conflicting type and check that it classifies correctly
    public void testLanguageGuesserMake() {
        SearchcodeLib sl = new SearchcodeLib();

        List<String> codeLines = new ArrayList<>();
        codeLines.add("packagecom.searchcode.app.util;importcom.google.common.base.Joiner;importcom.google.common.base.Splitter;importcom.google.common.base.Strings;importorg.apache.commons.lang3.ArrayUtils;importorg.apache.commons.lang3.StringUtils;importjava.util.*;publicclassSearchcodeLib{publicStringhash(Stringcontents){inthashLength=20;if(contents.length()==0){returnStrings.padStart(\"\",hashLength,'0');}StringallowedCharacters=\"BCDFGHIJKLMNOPQRSUVWXYZbcdfghijklmnopqrsuvwxyz1234567890\";//removeallspacesJoinerjoiner=Joiner.on(\"\").skipNulls();StringtoHash=joiner.join(Splitter.on('').trimResults().omitEmptyStrings().split(contents));//removeallnonacceptablecharactersfor(inti=0;i<toHash.length();i++){charc=toHash.charAt(i);if(allowedCharacters.indexOf(c)!=-1){//allowedsokeepit}}return\"\";}publicList<Classifier>classifier=newLinkedList<>();{classifier.add(newClassifier(\"text\",\"txt,text\",\"\"));classifier.add(newClassifier(\"XAML\",\"xaml\",\"setter,value,style,margin,sstring,textblock,height,offset,gradientstop,stackpanel,width,propertymargin,trigger,lineargradientbrush,storyboard,image,duration,rectangle,settervalue,doubleanimation\"));classifier.add(newClassifier(\"ASP.Net\",\"ascx,config,asmx,asax,master,aspx,sitemap\",\"version,cultureneutral,runatserver,systemwebextensions,publickeytokenbfade,section,customerrors,error,value,systemweb,configuration,include,attribute,position,setting,connectionstrings,absolute,dependentassembly,stylezindex,below\"));classifier.add(newClassifier(\"HTML\",\"htm,html\",\"classpspanspan,classpspan,spanspan,classw,bgcoloreeeeff,classwspanspan,classospanspan,classnavbarcell,bgcolorwhite,classmispanspan,classospan,classcsingleline,valigntop,border,cellpadding,cellspacing,classs,classnf,titleclass,classcm\"));classifier.add(newClassifier(\"C#\",\"cs\",\"summary,param,public,static,string,return,value,summarypublic,class,object,double,private,values,method,using,license,which,version,false,override\"));classifier.add(newClassifier(\"C/C++Header\",\"h,hpp\",\"return,nsscriptable,nsimethod,define,license,const,version,under,public,class,struct,nsastring,interface,retval,nserrornullpointer,function,attribute,value,terms,ifndef\"));classifier.add(newClassifier(\"C++\",\"cpp,cc,c\",\"return,const,object,license,break,result,false,software,value,public,stdstring,copyright,version,without,buffer,sizet,general,unsigned,string,jsfalse\"));classifier.add(newClassifier(\"Python\",\"py\",\"return,import,class,value,false,response,article,field,model,software,default,should,print,input,except,modelscharfieldmaxlength,fclean,object,valid,typetext\"));classifier.add(newClassifier(\"Java\",\"java\",\"public,return,private,string,static,param,final,throws,license,catch,javaxswinggrouplayoutpreferredsize,class,override,software,value,exception,boolean,object,general,version\"));//classifier.add(newClassifier(\"\",\"\",\"\"));}publicStringlanguageGuesser(StringfileName,List<String>codeLines){String[]split=fileName.split(\"\\\\.\");Stringextension=split[split.length-1].toLowerCase();//FindalllanguagesthatmightbethisoneObject[]matching=classifier.stream().filter(x->ArrayUtils.contains(x.extensions,extension)).toArray();if(matching.length==0){return\"Unknown\";}if(matching.length==1){return((Classifier)matching[0]).language;}//Morethenonepossiblematch,checkwhichoneismostlikelyisandreturnthatStringlanguageGuess=\"\";intbestKeywords=0;//foreachmatchfor(Objectc:matching){Classifierclassi=(Classifier)c;intmatchingKeywords=0;for(Stringline:codeLines){for(Stringkeyword:classi.keywords){matchingKeywords+=StringUtils.countMatches(line,keyword);}}if(matchingKeywords>bestKeywords){bestKeywords=matchingKeywords;languageGuess=classi.language;}}//findouthowmanyofitskeywordsexistinthecode//ifgreatermatchesthentheprevioussavereturnlanguageGuess;}classClassifier{publicStringlanguage=null;publicString[]extensions={};publicString[]keywords={};publicClassifier(Stringlanguage,Stringextensions,Stringkeywords){this.language=language;this.extensions=extensions.toLowerCase().split(\",\");this.keywords=keywords.toLowerCase().split(\",\");}}}");

        String language = sl.languageGuesser("test.java", codeLines);
        assertEquals("Java", language);
    }

    public void testCountFilteredLinesSingleLine() {
        SearchcodeLib scl = new SearchcodeLib();

        ArrayList<String> lst = new ArrayList<>();
        lst.add("one");
        lst.add("");

        assertEquals(1, scl.countFilteredLines(lst));
    }

    public void testCountFilteredLinesCommentLines() {
        SearchcodeLib scl = new SearchcodeLib();

        ArrayList<String> lst = new ArrayList<>();
        lst.add("// one");
        lst.add("    // one");
        lst.add("# comment");
        lst.add("    # comment");
        lst.add("");

        assertEquals(0, scl.countFilteredLines(lst));
    }

    public void testCountFilteredLinesMixCommentLines() {
        SearchcodeLib scl = new SearchcodeLib();

        ArrayList<String> lst = new ArrayList<>();
        lst.add("// one");
        lst.add("    // one");
        lst.add("not a comment");
        lst.add("# comment");
        lst.add("    # comment");
        lst.add("");
        lst.add("Also not a comment but has one // comment");

        assertEquals(2, scl.countFilteredLines(lst));
    }

    public void testCountFilteredCommentTypes() {
        SearchcodeLib scl = new SearchcodeLib();

        ArrayList<String> lst = new ArrayList<>();
        lst.add("// comment");
        lst.add("# comment");
        lst.add("<!-- comment ");
        lst.add("!* comment");
        lst.add("-- comment");
        lst.add("% comment");
        lst.add("; comment");
        lst.add("/* comment");
        lst.add("* comment");
        lst.add("* comment");
        lst.add("* comment");

        assertEquals(0, scl.countFilteredLines(lst));
    }

    public void testLanguageCostIgnore() {
        SearchcodeLib scl = new SearchcodeLib();
        assertTrue(scl.languageCostIgnore("Text"));
        assertTrue(scl.languageCostIgnore("JSON"));
        assertTrue(scl.languageCostIgnore("Unknown"));
        assertTrue(scl.languageCostIgnore("INI File"));
        assertTrue(scl.languageCostIgnore("ReStructuredText"));
        assertTrue(scl.languageCostIgnore("Configuration"));
    }

    public void testFormatQueryStringAnd() {
        SearchcodeLib scl = new SearchcodeLib();

        assertEquals("test   AND string", scl.formatQueryStringAndDefault("test string"));
        assertEquals("test   AND string   AND other\\|", scl.formatQueryStringAndDefault("test string other|"));
        assertEquals("test", scl.formatQueryStringAndDefault("test"));
        assertEquals("test", scl.formatQueryStringAndDefault("test  "));
        assertEquals("test", scl.formatQueryStringAndDefault("    test  "));
        assertEquals("test", scl.formatQueryStringAndDefault("    test"));
    }

    public void testFormatQueryStringOperators() {
        SearchcodeLib scl = new SearchcodeLib();
        assertEquals("test   AND   string", scl.formatQueryStringAndDefault("test AND string"));
        assertEquals("(test   AND   string)", scl.formatQueryStringAndDefault("(test AND string)"));
    }

    public void testFormatQueryStringDefaultAnd() {
        SearchcodeLib scl = new SearchcodeLib();
        assertEquals("test   AND string", scl.formatQueryStringAndDefault("test string"));
    }

    public void testFormatQueryStringOperatorsOr() {
        SearchcodeLib scl = new SearchcodeLib();
        assertEquals("test  AND  string", scl.formatQueryStringOrDefault("test AND string"));
        assertEquals("(test  AND  string)", scl.formatQueryStringOrDefault("(test AND string)"));
    }

    public void testFormatQueryStringDefaultOr() {
        SearchcodeLib scl = new SearchcodeLib();
        assertEquals("test  string", scl.formatQueryStringOrDefault("test string"));
    }

    public void testGenerateAltQueries() {
        SearchcodeLib scl = new SearchcodeLib();

        assertEquals(0, scl.generateAltQueries("supercalifragilisticexpialidocious").size());
        assertEquals("something", scl.generateAltQueries("something*").get(0));
        assertEquals("a b", scl.generateAltQueries("a* b*").get(0));

        Singleton.getSpellingCorrector().putWord("deh");
        assertEquals("dep", scl.generateAltQueries("dep*").get(0));
        assertEquals("deh", scl.generateAltQueries("den*").get(1));

        Singleton.getSpellingCorrector().putWord("ann");
        assertEquals("stuff OR other", scl.generateAltQueries("stuff AND other").get(1));
        assertEquals("stuff other", scl.generateAltQueries("stuff NOT other").get(0));
    }

    public void testGenerateAltQueriesNoDupes() {
        SearchcodeLib scl = new SearchcodeLib();
        assertEquals(1, scl.generateAltQueries("test*").size());
    }

    public void testGenerateAltNeverEmptyString() {
        SearchcodeLib scl = new SearchcodeLib();
        assertEquals(0, scl.generateAltQueries("+").size());
    }

    public void testGenerateBusBlurb() {
        SearchcodeLib scl = new SearchcodeLib();

        List<CodeFacetOwner> codeFacetOwners = new ArrayList<>();
        codeFacetOwners.add(new CodeFacetOwner("Ben", 1));
        List<CodeFacetLanguage> codeFacetLanguages = new ArrayList<>();
        codeFacetLanguages.add(new CodeFacetLanguage("Java", 10));
        codeFacetLanguages.add(new CodeFacetLanguage("Javascript", 8));
        codeFacetLanguages.add(new CodeFacetLanguage("C#", 7));

        String busBlurb = scl.generateBusBlurb(new ProjectStats(10, 1, codeFacetLanguages, codeFacetOwners));
        assertThat(busBlurb).contains("In this repository 1 committer has contributed to 1 file.");
        assertThat(busBlurb).contains("The most important languages in this repository are Java, Javascript and C#.");
        assertThat(busBlurb).contains("The project has a low bus factor of 1 and will be in trouble if Ben is hit by a bus.");
    }

    public void testGenerateBusBlurbMore() {
        SearchcodeLib scl = new SearchcodeLib();

        List<CodeFacetOwner> codeFacetOwners = new ArrayList<>();
        codeFacetOwners.add(new CodeFacetOwner("Ben", 6));
        codeFacetOwners.add(new CodeFacetOwner("Terry", 4));
        List<CodeFacetLanguage> codeFacetLanguages = new ArrayList<>();
        codeFacetLanguages.add(new CodeFacetLanguage("Java", 10));

        String busBlurb = scl.generateBusBlurb(new ProjectStats(10, 10, codeFacetLanguages, codeFacetOwners));
        assertThat(busBlurb).contains("In this repository 2 committers have contributed to 10 files.");
        assertThat(busBlurb).contains("The most important language in this repository is Java");
        assertThat(busBlurb).contains("The average person who commits this project has ownership of 50% of files.");
        assertThat(busBlurb).contains("The project relies on the following people; Ben, Terry.");
    }

    public void testGenerateBusBlurbStress() {
        SearchcodeLib scl = new SearchcodeLib();

        for (int i=0; i < 1000; i++) {

            List<CodeFacetOwner> codeFacetOwners = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                codeFacetOwners.add(new CodeFacetOwner("" + j, j));
            }


            List<CodeFacetLanguage> codeFacetLanguages = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                codeFacetLanguages.add(new CodeFacetLanguage("" + j, j));
            }

            scl.generateBusBlurb(new ProjectStats(i, i, codeFacetLanguages, codeFacetOwners));
        }
    }

    /**
     * Fuzzy testing of the generate alt queries where we try random things to see if we can introduce an exception
     */
    public void testGenerateAltQueriesFuzz() {
        Random rand = new Random();
        SearchcodeLib scl = new SearchcodeLib();

        for(int i = 0; i < 10; i++) {

            StringBuilder bf = new StringBuilder();
            for(int j=0; j < 5; j++) {

                if (j % 2 == 0) {
                    bf.append(RandomStringUtils.randomAscii(rand.nextInt(10) + 1) + " ");
                }
                else {
                    bf.append(RandomStringUtils.randomAlphabetic(rand.nextInt(10) + 1) + " ");
                }

                Singleton.getSpellingCorrector().putWord(RandomStringUtils.randomAlphabetic(rand.nextInt(10) + 1));

                switch(rand.nextInt(5)) {
                    case 1:
                        bf.append(" AND ");
                        break;
                    case 2:
                        bf.append(" OR ");
                        break;
                    case 3:
                        bf.append(" NOT ");
                        break;
                    case 4:
                        bf.append(RandomStringUtils.randomAlphabetic(rand.nextInt(10) + 1));
                        break;
                    default:
                        break;
                }
            }

            scl.generateAltQueries(bf.toString());
        }
    }
}






















