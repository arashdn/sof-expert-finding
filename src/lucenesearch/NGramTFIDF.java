package lucenesearch;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author arashdn and mahmood.neshati@gmail.com
 */
public class NGramTFIDF
{

    String filepath = "./data/grams/1gram_final.csv";
    String TFpath = "./data/grams/TF.csv";
    String TFIDFpath = "./data/grams/TFIDF.csv";
    HashMap<String, HashMap<String, Double>> word_tag_count = new HashMap<>();
    HashMap<String, HashSet<String>> word_tag_idf = new HashMap<>();

    String tags[];

    String mainTag;

    public NGramTFIDF(String mainTag) throws Exception
    {
        this.mainTag = mainTag;
        if (mainTag.equalsIgnoreCase("java"))
               tags = new String[] 
               {
                "algorithm", "android", "annotations", "ant", "apache", "applet", "arraylist", "arrays", "awt", "c#", "c++", "class", "collections", "concurrency", "database", "date", "design-patterns", "eclipse", "encryption", "exception", "file-io", "file", "generics", "google-app-engine", "gwt", "hadoop", "hashmap", "hibernate", "html", "http", "image", "inheritance", "intellij-idea", "io", "jar", "java-ee", "java", "javafx", "javascript", "jaxb", "jboss", "jdbc", "jersey", "jframe", "jni", "jpa", "jpanel", "jquery", "jsf", "json", "jsp", "jtable", "junit", "jvm", "libgdx", "linux", "list", "log4j", "logging", "loops", "maven", "methods", "multithreading", "mysql", "netbeans", "nullpointerexception", "object", "oop", "oracle", "osx", "parsing", "performance", "php", "python", "reflection", "regex", "rest", "scala", "security", "selenium", "serialization", "servlets", "soap", "sockets", "sorting", "spring-mvc", "spring-security", "spring", "sql", "sqlite", "string", "struts2", "swing", "swt", "tomcat", "unit-testing", "user-interface", "web-services", "windows", "xml"
               };
        else if (mainTag.equalsIgnoreCase("php"))
               tags = new String[] 
               {
                   ".htaccess","ajax","android","apache","api","arrays","authentication","caching","cakephp","class","codeigniter","cookies","cron","css","csv","curl","database","date","datetime","doctrine","doctrine2","dom","drupal","email","encryption","facebook","facebook-graph-api","file","file-upload","foreach","forms","function","gd","get","html","html5","http","if-statement","image","include","java","javascript","joomla","jquery","json","laravel","laravel-4","linux","login","loops","magento","mod-rewrite","mongodb","multidimensional-array","mysql","mysqli","object","oop","pagination","parsing","paypal","pdf","pdo","performance","phpmyadmin","phpunit","post","preg-match","preg-replace","python","redirect","regex","rest","search","security","select","session","simplexml","smarty","soap","sorting","sql","sql-server","string","symfony2","table","twitter","upload","url","utf-8","validation","variables","web-services","wordpress","wordpress-plugin","xampp","xml","yii","zend-framework","zend-framework"
               };
        else
            throw new Exception("Tags not found");
    }
    
    

    public void calculate() throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        String line = "";
        while ((line = reader.readLine()) != null)
        {
            String[] parts = line.split(",");
            String docid = parts[0];
            String term = parts[1];
            ArrayList<String> tags = getTags(parts[2]);
            addToTFMatrix(term, tags);
            addToIDFMatrix(term, tags, docid);

        }
        reader.close();
        String output1 = generateTFMatrix();
        String output2 = generateTFIDFMatrix();

        printToFile(output1, TFpath);
        printToFile(output2, TFIDFpath);

    }

    private String generateTFIDFMatrix()
    {
        StringBuilder builder = new StringBuilder();
        for (String term : word_tag_count.keySet())
        {
            String line = term + "\t";
            for (String tag : tags)
            {
                double count = (word_tag_count.get(term)).get(tag) == null ? 0 : (word_tag_count.get(term)).get(tag);
                count *= Math.log(100.0 / word_tag_idf.get(term).size());
                line += count + ",";
            }
            line = line.substring(0, line.length() - 1) + "\r\n";
            builder.append(line);

        }
        return builder.toString().trim();
    }

    private String generateTFMatrix()
    {
        StringBuilder builder = new StringBuilder();
        for (String term : word_tag_count.keySet())
        {
            String line = term + "\t";
            for (String tag : tags)
            {
                double count = (word_tag_count.get(term)).get(tag) == null ? 0 : (word_tag_count.get(term)).get(tag);
                line += count + ",";
            }
            line = line.substring(0, line.length() - 1) + "\r\n";
            builder.append(line);

        }
        return builder.toString().trim();
    }

    private void printToFile(String output1, String path) throws IOException
    {
        PrintWriter writer = new PrintWriter(new FileWriter(path));
        writer.print(output1);
        writer.close();
    }

    private void addToIDFMatrix(String term, ArrayList<String> tags, String docid)
    {
        if (word_tag_idf.get(term) == null)
        {
            HashSet<String> values = new HashSet<>();
            word_tag_idf.put(term, values);
        }
        HashSet<String> set = word_tag_idf.get(term);
        set.addAll(tags);
        word_tag_idf.put(term, set);
    }

    private void addToTFMatrix(String term, ArrayList<String> tags)
    {
        if (word_tag_count.get(term) == null)
        {
            HashMap<String, Double> values = new HashMap<>();
            word_tag_count.put(term, values);
        }
        assert (word_tag_count.get(term) != null);
        HashMap<String, Double> term_row = word_tag_count.get(term);
        for (String tag : tags)
        {
            if (term_row.get(tag) == null)
            {

                term_row.put(tag, 1.0);
            }
            else
            {
                term_row.put(tag, term_row.get(tag) + 1.0);
            }
        }

    }

    private ArrayList<String> getTags(String tags)
    {
        String[] tagarray = tags.split("\t");
        ArrayList<String> out = new ArrayList<>();
        int i = 0;
        while (i < tagarray.length)
        {
            String s = tagarray[i];
            out.add(i, s);
            i++;
        }
        return out;
    }

}
