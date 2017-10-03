package com.searchcode.app.util;


import com.searchcode.app.dto.FileClassifierResult;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class FileClassifierResultTest extends TestCase {
    public void testDatabaseControl() {
        FileClassifier fileClassifier = new FileClassifier(new ArrayList<>());

        assertThat(fileClassifier.getDatabase()).isEmpty();
        List<FileClassifierResult> database = new ArrayList<>();

        database.add(new FileClassifierResult("", "", ""));
        fileClassifier.setDatabase(database);
        assertThat(fileClassifier.getDatabase().size()).isEqualTo(1);
    }

    public void testIdentifyLanguage() {
        List<FileClassifierResult> database = new ArrayList<>();
        database.add(new FileClassifierResult("boyterlang", "boyter", ""));

        FileClassifier fileClassifier = new FileClassifier(database);
        String languageGuess = fileClassifier.languageGuesser("test.boyter", new ArrayList<>());

        assertThat(languageGuess).isEqualTo("boyterlang");
    }

    public void testIdentifyLanguageAdditionalDots() {
        List<FileClassifierResult> database = new ArrayList<>();
        database.add(new FileClassifierResult("Typescript", "ts", ""));
        database.add(new FileClassifierResult("Typings Definition", "d.ts", ""));

        FileClassifier fileClassifier = new FileClassifier(database);
        String languageGuess = fileClassifier.languageGuesser("test.d.ts", new ArrayList<>());

        assertThat(languageGuess).isEqualTo("Typings Definition");
    }

    public void testLanguageGuesserText() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.txt", new ArrayList<>());
        assertEquals("Text", language);
    }

    public void testLanguageGuesserXAML() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.xaml", new ArrayList<>());
        assertEquals("XAML", language);
    }

    public void testLanguageGuesserASPNET() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.ascx", new ArrayList<>());
        assertEquals("ASP.Net", language);
    }

    public void testLanguageGuesserHTML() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.html", new ArrayList<>());
        assertEquals("HTML", language);
    }

    public void testLanguageGuesserUnknown() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.shouldnotexist", new ArrayList<>());
        assertEquals("Unknown", language);
    }

    public void testLanguageGuesserNoExtension() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("Jenkinsfile", new ArrayList<>());
        assertEquals("Jenkins Buildfile", language);
    }

    public void testLanguageGuesserKotlin() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.kt", new ArrayList<>());
        assertEquals("Kotlin", language);
    }

    public void testLanguageGuesserMultiple() {
        // Multiple languages match c so need to add logic here to check they are correct
        FileClassifier fileClassifier = new FileClassifier();
        ArrayList<String> lines = new ArrayList<String>() {{
            add("#include<stdio.h>");
            add("int main(void) {");
            add("printf(\"Hello World\\n\");");
            add("return 0;");
            add("}");
        }};

        String language = fileClassifier.languageGuesser("test.c", lines);
        assertEquals("C", language);

        lines = new ArrayList<String>() {{
            add("#include <iostream>");
            add("using namespace std;");
            add("void main()");
            add("{ cout << \"Hello World!\" << endl;   cout << \"Welcome to C++ Programming\" << endl; }");
        }};

        language = fileClassifier.languageGuesser("test.c", lines);
        assertEquals("C++", language);
    }

    // TODO update this with actual conflicting type and check that it classifies correctly
    public void testLanguageGuesserMake() {
        FileClassifier fileClassifier = new FileClassifier();

        List<String> codeLines = new ArrayList<>();
        codeLines.add("packagecom.searchcode.app.util;importcom.google.common.base.Joiner;importcom.google.common.base.Splitter;importcom.google.common.base.Strings;importorg.apache.commons.lang3.ArrayUtils;importorg.apache.commons.lang3.StringUtils;importjava.util.*;publicclassSearchcodeLib{publicStringhash(Stringcontents){inthashLength=20;if(contents.length()==0){returnStrings.padStart(\"\",hashLength,'0');}StringallowedCharacters=\"BCDFGHIJKLMNOPQRSUVWXYZbcdfghijklmnopqrsuvwxyz1234567890\";//removeallspacesJoinerjoiner=Joiner.on(\"\").skipNulls();StringtoHash=joiner.join(Splitter.on('').trimResults().omitEmptyStrings().split(contents));//removeallnonacceptablecharactersfor(inti=0;i<toHash.length();i++){charc=toHash.charAt(i);if(allowedCharacters.indexOf(c)!=-1){//allowedsokeepit}}return\"\";}publicList<Classifier>classifier=newLinkedList<>();{classifier.add(newClassifier(\"text\",\"txt,text\",\"\"));classifier.add(newClassifier(\"XAML\",\"xaml\",\"setter,value,style,margin,sstring,textblock,height,offset,gradientstop,stackpanel,width,propertymargin,trigger,lineargradientbrush,storyboard,image,duration,rectangle,settervalue,doubleanimation\"));classifier.add(newClassifier(\"ASP.Net\",\"ascx,config,asmx,asax,master,aspx,sitemap\",\"version,cultureneutral,runatserver,systemwebextensions,publickeytokenbfade,section,customerrors,error,value,systemweb,configuration,include,attribute,position,setting,connectionstrings,absolute,dependentassembly,stylezindex,below\"));classifier.add(newClassifier(\"HTML\",\"htm,html\",\"classpspanspan,classpspan,spanspan,classw,bgcoloreeeeff,classwspanspan,classospanspan,classnavbarcell,bgcolorwhite,classmispanspan,classospan,classcsingleline,valigntop,border,cellpadding,cellspacing,classs,classnf,titleclass,classcm\"));classifier.add(newClassifier(\"C#\",\"cs\",\"summary,param,public,static,string,return,value,summarypublic,class,object,double,private,values,method,using,license,which,version,false,override\"));classifier.add(newClassifier(\"C/C++Header\",\"h,hpp\",\"return,nsscriptable,nsimethod,define,license,const,version,under,public,class,struct,nsastring,interface,retval,nserrornullpointer,function,attribute,value,terms,ifndef\"));classifier.add(newClassifier(\"C++\",\"cpp,cc,c\",\"return,const,object,license,break,result,false,software,value,public,stdstring,copyright,version,without,buffer,sizet,general,unsigned,string,jsfalse\"));classifier.add(newClassifier(\"Python\",\"py\",\"return,import,class,value,false,response,article,field,model,software,default,should,print,input,except,modelscharfieldmaxlength,fclean,object,valid,typetext\"));classifier.add(newClassifier(\"Java\",\"java\",\"public,return,private,string,static,param,final,throws,license,catch,javaxswinggrouplayoutpreferredsize,class,override,software,value,exception,boolean,object,general,version\"));//classifier.add(newClassifier(\"\",\"\",\"\"));}publicStringlanguageGuesser(StringfileName,List<String>codeLines){String[]split=fileName.split(\"\\\\.\");Stringextension=split[split.length-1].toLowerCase();//FindalllanguagesthatmightbethisoneObject[]matching=classifier.stream().filter(x->ArrayUtils.contains(x.extensions,extension)).toArray();if(matching.length==0){return\"Unknown\";}if(matching.length==1){return((Classifier)matching[0]).language;}//Morethenonepossiblematch,checkwhichoneismostlikelyisandreturnthatStringlanguageGuess=\"\";intbestKeywords=0;//foreachmatchfor(Objectc:matching){Classifierclassi=(Classifier)c;intmatchingKeywords=0;for(Stringline:codeLines){for(Stringkeyword:classi.keywords){matchingKeywords+=StringUtils.countMatches(line,keyword);}}if(matchingKeywords>bestKeywords){bestKeywords=matchingKeywords;languageGuess=classi.language;}}//findouthowmanyofitskeywordsexistinthecode//ifgreatermatchesthentheprevioussavereturnlanguageGuess;}classClassifier{publicStringlanguage=null;publicString[]extensions={};publicString[]keywords={};publicClassifier(Stringlanguage,Stringextensions,Stringkeywords){this.language=language;this.extensions=extensions.toLowerCase().split(\",\");this.keywords=keywords.toLowerCase().split(\",\");}}}");

        String language = fileClassifier.languageGuesser("test.java", codeLines);
        assertEquals("Java", language);
    }

    public void testLanguageGuesserNoMatching() {
        FileClassifier fileClassifier = new FileClassifier();
        fileClassifier.DEEP_GUESS = true;
        ArrayList<String> lines = new ArrayList<String>() {{
            add("#include<stdio.h>");
            add("int main(void) {");
            add("printf(\"Hello World\\n\");");
            add("return 0;");
            add("}");
        }};

        String language = fileClassifier.languageGuesser("noidea", lines);
        assertEquals("C", language);
    }

    public void testDeepGuess() {
        FileClassifier fileClassifier = new FileClassifier();
        ArrayList<String> lines = new ArrayList<String>() {{
            add("#include<stdio.h>");
            add("int main(void) {");
            add("printf(\"Hello World\\n\");");
            add("return 0;");
            add("}");
        }};

        String language = fileClassifier.deepGuess("noidea", lines);
        assertEquals("C", language);
    }
}
