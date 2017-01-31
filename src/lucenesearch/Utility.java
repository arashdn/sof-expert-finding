package lucenesearch;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 *
 * @author arashdn
 */
public class Utility
{

    public static ArrayList<String> getTags()
    {
        ArrayList<String> res = new ArrayList<>();
        res.add("algorithm");
        res.add("android");
        res.add("annotations");
        res.add("ant");
        res.add("apache");
        res.add("applet");
        res.add("arraylist");
        res.add("arrays");
        res.add("awt");
        res.add("c#");
        res.add("c++");
        res.add("class");
        res.add("collections");
        res.add("concurrency");
        res.add("database");
        res.add("date");
        res.add("design-patterns");
        res.add("eclipse");
        res.add("encryption");
        res.add("exception");
        res.add("file-io");
        res.add("file");
        res.add("generics");
        res.add("google-app-engine");
        res.add("gwt");
        res.add("hadoop");
        res.add("hashmap");
        res.add("hibernate");
        res.add("html");
        res.add("http");
        res.add("image");
        res.add("inheritance");
        res.add("intellij-idea");
        res.add("io");
        res.add("jar");
        res.add("java-ee");
        res.add("java");
        res.add("javafx");
        res.add("javascript");
        res.add("jaxb");
        res.add("jboss");
        res.add("jdbc");
        res.add("jersey");
        res.add("jframe");
        res.add("jni");
        res.add("jpa");
        res.add("jpanel");
        res.add("jquery");
        res.add("jsf");
        res.add("json");
        res.add("jsp");
        res.add("jtable");
        res.add("junit");
        res.add("jvm");
        res.add("libgdx");
        res.add("linux");
        res.add("list");
        res.add("log4j");
        res.add("logging");
        res.add("loops");
        res.add("maven");
        res.add("methods");
        res.add("multithreading");
        res.add("mysql");
        res.add("netbeans");
        res.add("nullpointerexception");
        res.add("object");
        res.add("oop");
        res.add("oracle");
        res.add("osx");
        res.add("parsing");
        res.add("performance");
        res.add("php");
        res.add("python");
        res.add("reflection");
        res.add("regex");
        res.add("rest");
        res.add("scala");
        res.add("security");
        res.add("selenium");
        res.add("serialization");
        res.add("servlets");
        res.add("soap");
        res.add("sockets");
        res.add("sorting");
        res.add("spring-mvc");
        res.add("spring-security");
        res.add("spring");
        res.add("sql");
        res.add("sqlite");
        res.add("string");
        res.add("struts2");
        res.add("swing");
        res.add("swt");
        res.add("tomcat");
        res.add("unit-testing");
        res.add("user-interface");
        res.add("web-services");
        res.add("windows");
        res.add("xml");
        
        
        return res;
        
    }
    
    public static String getGoldenFileName(String tag)
    {
        return "DataSetFor"+tag+".csv";
    }
    
}
