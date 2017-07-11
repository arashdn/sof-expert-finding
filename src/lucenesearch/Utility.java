package lucenesearch;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * @author arashdn
 */
public class Utility
{

    public static ArrayList<String> getTags(String tag)
    {
        ArrayList<String> res = new ArrayList<>();

        if(tag.equalsIgnoreCase("java") )
        {
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
        }
        else if(tag.equalsIgnoreCase("php"))
        {
            res.add("php");
            res.add("mysql");
            res.add("javascript");
            res.add("html");
            res.add("jquery");
            res.add("arrays");
            res.add("ajax");
            res.add("wordpress");
            res.add("sql");
            res.add("codeigniter");
            res.add("regex");
            res.add("forms");
            res.add("json");
            res.add("apache");
            res.add("database");
            res.add(".htaccess");
            res.add("symfony2");
            res.add("laravel");
            res.add("xml");
            res.add("zend-framework");
            res.add("curl");
            res.add("session");
            res.add("pdo");
            res.add("css");
            res.add("mysqli");
            res.add("facebook");
            res.add("cakephp");
            res.add("email");
            res.add("magento");
            res.add("yii");
            res.add("laravel-4");
            res.add("oop");
            res.add("string");
            res.add("post");
            res.add("image");
            res.add("function");
            res.add("variables");
            res.add("api");
            res.add("date");
            res.add("mod-rewrite");
            res.add("android");
            res.add("security");
            res.add("foreach");
            res.add("multidimensional-array");
            res.add("redirect");
            res.add("url");
            res.add("class");
            res.add("validation");
            res.add("java");
            res.add("doctrine2");
            res.add("linux");
            res.add("file-upload");
            res.add("joomla");
            res.add("cookies");
            res.add("loops");
            res.add("facebook-graph-api");
            res.add("file");
            res.add("drupal");
            res.add("soap");
            res.add("datetime");
            res.add("login");
            res.add("preg-replace");
            res.add("parsing");
            res.add("csv");
            res.add("if-statement");
            res.add("zend-framework2");
            res.add("html5");
            res.add("upload");
            res.add("paypal");
            res.add("preg-match");
            res.add("sorting");
            res.add("phpmyadmin");
            res.add("search");
            res.add("get");
            res.add("sql-server");
            res.add("doctrine");
            res.add("performance");
            res.add("web-services");
            res.add("table");
            res.add("pdf");
            res.add("utf-8");
            res.add("simplexml");
            res.add("object");
            res.add("phpunit");
            res.add("mongodb");
            res.add("dom");
            res.add("select");
            res.add("http");
            res.add("include");
            res.add("authentication");
            res.add("caching");
            res.add("cron");
            res.add("pagination");
            res.add("xampp");
            res.add("twitter");
            res.add("python");
            res.add("rest");
            res.add("encryption");
            res.add("wordpress-plugin");
            res.add("gd");
        }
        else
            throw new NotImplementedException();
        
        
        return res;
        
    }
    
    public static String getGoldenFileName(String tag)
    {
        return "DataSetFor"+tag+".csv";
    }
    
}
