package lucenesearch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author arashdn
 */
public class ClusterTranslate
{
    String mainTag;
    String clusterFilePath;
    String gramsFilePath = "./data/grams/1gram.csv";
    String top_wordsFilePath = "./data/clusters/top_words.txt";
    HashSet<String> topWords;
    HashMap<Integer,String[]> clusters;
    HashMap<String,Integer> tagCluster;
    HashMap<String, HashMap<String, Double>> word_tag_count = new HashMap<>();
    HashMap<String, HashMap<Integer, Double>> word_cluster_prob = new HashMap<>();
    HashMap<Integer, HashMap<String, Double>> cluster_word_prob = new HashMap<>();
    HashMap<Integer, TreeMap<String, Double>> sorted_cluster_word_prob = new HashMap<>();
    
    String tags[];
    
    public ClusterTranslate(String mainTag) throws Exception
    {
        this.mainTag = mainTag;
        this.clusterFilePath = "./data/clusters/cluster_"+mainTag+"_final.csv";
        setupClusters();
        setupTopWords();
        if (mainTag.equalsIgnoreCase("java"))
               tags = new String[] 
               {
                "algorithm", "android", "annotations", "ant", "apache", "applet", "arraylist", "arrays", "awt", "c#", "c++", "class", "collections", "concurrency", "database", "date", "design-patterns", "eclipse", "encryption", "exception", "file-io", "file", "generics", "google-app-engine", "gwt", "hadoop", "hashmap", "hibernate", "html", "http", "image", "inheritance", "intellij-idea", "io", "jar", "java-ee", "java", "javafx", "javascript", "jaxb", "jboss", "jdbc", "jersey", "jframe", "jni", "jpa", "jpanel", "jquery", "jsf", "json", "jsp", "jtable", "junit", "jvm", "libgdx", "linux", "list", "log4j", "logging", "loops", "maven", "methods", "multithreading", "mysql", "netbeans", "nullpointerexception", "object", "oop", "oracle", "osx", "parsing", "performance", "php", "python", "reflection", "regex", "rest", "scala", "security", "selenium", "serialization", "servlets", "soap", "sockets", "sorting", "spring-mvc", "spring-security", "spring", "sql", "sqlite", "string", "struts2", "swing", "swt", "tomcat", "unit-testing", "user-interface", "web-services", "windows", "xml"
               };
        else if (mainTag.equalsIgnoreCase("php"))
               tags = new String[] 
               {
                   ".htaccess","ajax","android","apache","api","arrays","authentication","caching","cakephp","class","codeigniter","cookies","cron","css","csv","curl","database","date","datetime","doctrine","doctrine2","dom","drupal","email","encryption","facebook","facebook-graph-api","file","file-upload","foreach","forms","function","gd","get","html","html5","http","if-statement","image","include","java","javascript","joomla","jquery","json","laravel","laravel-4","linux","login","loops","magento","mod-rewrite","mongodb","multidimensional-array","mysql","mysqli","object","oop","pagination","parsing","paypal","pdf","pdo","performance","php","phpmyadmin","phpunit","post","preg-match","preg-replace","python","redirect","regex","rest","search","security","select","session","simplexml","soap","sorting","sql","sql-server","string","symfony2","table","twitter","upload","url","utf-8","validation","variables","web-services","wordpress","wordpress-plugin","xampp","xml","yii","zend-framework","zend-framework2"
               };
        else
            throw new Exception("Tags not found");
    }
    
    private void setupTopWords() throws FileNotFoundException, IOException
    {
        topWords = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(top_wordsFilePath));
        String line = "";
        while ((line = reader.readLine()) != null)
        {
            if(line.trim().length()>=1)   
                topWords.add(line.trim());
        }
        
    }
    
    private void setupClusters() throws IOException
    {
        int i = 0;
        HashMap<Integer,String[]> clusters = new HashMap<>();
        this.tagCluster = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(clusterFilePath));
        String line = "";
        while ((line = reader.readLine()) != null)
        {
            String[] parts = line.split(",");
            clusters.put(i, parts);
            for (String part : parts)
            {
                tagCluster.put(part, i);
            }
            i++;
        }
        reader.close();
        this.clusters = clusters;
        
    }
    
    public void calculate(int numberOfTransaltions,int minWordCount) throws FileNotFoundException, IOException
    {
        System.out.println("calcualting TF weights");
        BufferedReader reader = new BufferedReader(new FileReader(gramsFilePath));
        String line = "";
        while ((line = reader.readLine()) != null)
        {
            String[] parts = line.split(",");
            String docid = parts[0];
            String term = parts[1];
            if(!topWords.contains(term))
                continue;
            ArrayList<String> tags = getTags(parts[2]);
            addToTFMatrix(term, tags);

        }
        reader.close();
        System.out.println("calculating cluster probs");
        getClusterProbs(minWordCount);
        System.out.println("Sorting probs");
        
        int c = 0;
        int size = this.cluster_word_prob.size();
        for (Integer cluster : this.cluster_word_prob.keySet())
        {
            System.out.println((++c)+"/"+size);
            HashMap<String, Double> probs = cluster_word_prob.get(cluster);
            ClusterValueComparator bvc = new ClusterValueComparator(probs);
            TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
            sorted_map.putAll(probs);
            sorted_cluster_word_prob.put(cluster, sorted_map);
        }
        
        System.out.println("Saving results:");
        String s;
        PrintWriter out = new PrintWriter("./data/cluster_translations.txt");
        for (String tag : tagCluster.keySet())
        {
            s = tag+"~";
            TreeMap<String, Double> map = sorted_cluster_word_prob.get(tagCluster.get(tag));
            Iterator it = map.entrySet().iterator();
            int i = 0;
            while (it.hasNext() && i++<numberOfTransaltions)
            {
               Map.Entry pair = (Map.Entry)it.next();
               s = s+(String) pair.getKey()+",";
//               s = s+(String) pair.getKey()+":"+pair.getValue()+",";
            }
            if(i > 0)
                s = s.substring(0,s.length()-1);//remove last comma
            out.println(s);
        }
        out.close();
        System.out.println("Done!");
    }
    
    private void getClusterProbs(int minWordCount)
    {
        int size = word_tag_count.size();
        int i = 0;
        for (Integer cluster : this.clusters.keySet())
        {
            this.cluster_word_prob.put(cluster, new HashMap<>());
        }
        for (String word : this.word_tag_count.keySet())
        {
            System.out.println((++i)+"/"+size+" => "+word);
            if(word.length()<1)
                continue;
            HashMap<String, Double> tag_count = this.word_tag_count.get(word);
            double tf_word = 0;
            HashMap<Integer,Double> cluster_count = new HashMap<>();
            Integer cluster = null;
            for (String tag : tag_count.keySet())
            {
                tf_word += tag_count.get(tag);
                cluster = tagCluster.get(tag);
                if(cluster_count.get(cluster) == null)
                {
                    cluster_count.put(cluster, tag_count.get(tag));
                }
                else
                {
                    cluster_count.replace(cluster, cluster_count.get(cluster) + tag_count.get(tag));
                }
            }
            if(tf_word < minWordCount)
            {
                continue;
            }
            for (Integer cls : cluster_count.keySet())
            {
                double res = cluster_count.get(cls)/tf_word;
                cluster_count.replace(cls, res);
//                System.out.println(word + " in "+cls + " = "+res);
                this.cluster_word_prob.get(cls).put(word, res);
            }
            word_cluster_prob.put(word, cluster_count);
        }
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
            if(Arrays.asList(this.tags).contains(tag))
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


class ClusterValueComparator implements Comparator<String> 
{
    Map<String, Double> base;

    public ClusterValueComparator(Map<String, Double> base) 
    {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(String a, String b) 
    {
        if (base.get(a) >= base.get(b)) 
        {
            return -1;
        } 
        else 
        {
            return 1;
        } // returning 0 would merge keys
    }
}