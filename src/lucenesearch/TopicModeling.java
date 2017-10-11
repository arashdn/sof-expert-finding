package lucenesearch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author sajad, Integerated by Arashdn
 */
class TermTopicsWeight
{

    private HashMap<String, String> topicWeights;

    public TermTopicsWeight()
    {
        this.topicWeights = new HashMap<>();
    }

    public HashMap<String, String> getTopicWeights()
    {
        return topicWeights;
    }

    public void setTopicWeights(HashMap<String, String> topicWeights)
    {
        this.topicWeights = topicWeights;
    }
}

class TopicList
{

    private HashMap<String, Double> postTopicsList;

    public TopicList()
    {
        this.postTopicsList = new HashMap<>();
    }

    public HashMap<String, Double> getPostTopicsList()
    {
        return postTopicsList;
    }

    public void setPostTopicsList(HashMap<String, Double> postTopicsList)
    {
        this.postTopicsList = postTopicsList;
    }
}

class TextReader
{

    public static ArrayList<String> realRankedUsers;

    private static HashSet<String> postIDs = new HashSet<String>();
    private static ArrayList<String> queries = new ArrayList<>();

    public static ArrayList<String> getQueries(String mainTag)
    {

        ArrayList<String> tags = Utility.getTags(mainTag);
        return tags;
    }

    public static ArrayList<String> getQueriesTerms(String mainTag)
    {
        BufferedReader br = null;
        ArrayList<String> queriesTerms = new ArrayList<String>();
        ArrayList<String> tags = Utility.getTags(mainTag);
        for (String query : tags)
        {
            String[] qTerms = query.split("-");
            queriesTerms.addAll(Arrays.asList(qTerms));
        }
        return queriesTerms;
    }

    public static ArrayList<String> getExpertRealRanks(String query) throws IOException
    {
        realRankedUsers.clear();
        String directory = "./data/goldens/DataSetFor" + query.toLowerCase() + ".csv";
        BufferedReader br = null;
        try
        {
            String line;
            br = new BufferedReader(new FileReader(directory));
            line = br.readLine();
            line = br.readLine();
            while (line != null)
            {
                realRankedUsers.add(line.split(",")[0]);
                line = br.readLine();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return realRankedUsers;
    }

    public static void setupRankedList()
    {
        realRankedUsers = new ArrayList<>();
    }

    public static void unsetRealRankedUserList()
    {
        realRankedUsers.clear();
    }
}

public class TopicModeling
{

    private HashMap<String, Double> experts;
    private HashMap<String, Double> intExperts;
    private ArrayList<String> queriesTerms;

    private HashSet<Document> documentsList;
    private HashMap<String, TopicList> posts;

    private boolean isFirstTerm;

    private String[] queryTerms;

    private DirectoryReader iReader;
    private IndexSearcher iSearcher;
    private HashMap<String, TermTopicsWeight> words;

    private String mainTag;
    private String weight_file_path;
    private String topic_file_path;

    public TopicModeling(String mainTag) throws IOException
    {
        this.mainTag = mainTag;
    }

    public void setup(int param) throws IOException
    {
        weight_file_path = "./data/TopicModeling/word_weight_" + param + ".txt";
        topic_file_path = "./data/TopicModeling/doc_topics_" + param + ".txt";
        queriesTerms = new ArrayList<>();
        experts = new HashMap<>();
        intExperts = new HashMap<>();
        documentsList = new HashSet<>();
        posts = new HashMap<>();
        words = new HashMap<>();
        iReader = DirectoryReader.open(FSDirectory.open(Paths.get(new Searcher().getPostIndexPath())));
        iSearcher = new IndexSearcher(iReader);
        isFirstTerm = true;
        TextReader.setupRankedList();
        System.out.println("Started to make WordCountStructure");
        makeWordCountStructure(param);
        System.out.println("Started to make CompositionStructure");
        makeCompositionStructure(param);
        System.out.println("Setup end");

    }

    private void makeWordCountStructure(int param)
    {
        words.clear();
        HashMap<String, String> topicsWeight = new HashMap<>();
        BufferedReader br = null;
        try
        {
            String line;
            String[] lineArr;
            String[] item;
            queriesTerms = TextReader.getQueriesTerms(mainTag);
            br = new BufferedReader(new FileReader(weight_file_path));
            line = br.readLine();
            while (line != null)
            {
                lineArr = line.split(" ");
                TermTopicsWeight tw = new TermTopicsWeight();
                topicsWeight = tw.getTopicWeights();

                if (queriesTerms.contains(lineArr[1]))
                {
                    for (int i = 2; i < lineArr.length; i++)
                    {
                        item = lineArr[i].split(":");
                        topicsWeight.put(item[0], item[1]);
                    }
                    tw.setTopicWeights(topicsWeight);
                    words.put(lineArr[1], tw);
                }
                line = br.readLine();
            }
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }

    private void makeCompositionStructure(int param)
    {
        posts.clear();
        HashMap<String, Double> topicsList = new HashMap<>();
        BufferedReader br = null;
        try
        {
            String line;
            String[] lineArr;
            int total = 0;
            br = new BufferedReader(new FileReader((topic_file_path)));
            while(br.readLine() != null)
            {
                total++;
            }
                
            br.close();
            br = new BufferedReader(new FileReader((topic_file_path)));
            line = br.readLine();
            int cnt = 0;
            while (line != null)
            {
                if(++cnt % 10000 == 0)
                    System.out.println(cnt+"/"+total);
                lineArr = line.split("\t");
                TopicList tl = new TopicList();
                topicsList = tl.getPostTopicsList();
                for (int i = 2; i < lineArr.length; i++)
                {
                    try
                    {
                        topicsList.put(String.valueOf(i - 2), Double.parseDouble(lineArr[i]));
                    }
                    catch (NumberFormatException e)
                    {
                        topicsList.put(String.valueOf(i - 2), Double.parseDouble("0"));
                        System.out.println("not a number"); 
                    } 
                }
                tl.setPostTopicsList(topicsList);
                posts.put(lineArr[1], tl);
                line = br.readLine();
            }
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }

    public HashMap<String, Double> getExperts(int num, String query) throws IOException, ParseException
    {
        resetMap();
        isFirstTerm = true;
        luceneDefaultExpertRetrieval(num, query);
        HashMap<String, String> ttw = new HashMap<>();
        HashMap<String, Double> tl = new HashMap<>();
        parseQuery(query);
        double probOfTopicQuery = 0;
        int tokenCounts = 0;
        double probOfTopicDoc = 0;
        int skip = 0;
        for (int i = 0; i < queryTerms.length; i++)
        {
            if (words.containsKey(queryTerms[i]))
            {
                if (i != 0)
                {
                    isFirstTerm = false;
                }
                ttw = words.get(queryTerms[i]).getTopicWeights();
                for (String topic : ttw.keySet())
                {
                    tokenCounts += Integer.valueOf(ttw.get(topic));
                }
                for (String topic : ttw.keySet())
                {
                    probOfTopicQuery = Double.parseDouble(ttw.get(topic)) / (double) tokenCounts;                   // P(topic|q)
                    for (Document doc : documentsList)
                    {
                        try
                        {
                            tl = posts.get(doc.getField("SId").stringValue()).getPostTopicsList();
                            probOfTopicDoc = tl.get(topic);
                            if(doc.getField("SOwnerUserId") == null)
                            {
                                skip++;
                                continue;
                            }
                            insertEntryToSet(probOfTopicDoc * probOfTopicQuery, doc.getField("SOwnerUserId").stringValue());
                        }
                        catch(Exception e)
                        {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        }
        System.out.println(skip+" documnets skipped");
        if (isFirstTerm)
        {
            return sortHashMapByValues(experts);
        }
        return sortHashMapByValues(intExperts);
    }

    private void resetMap()
    {
        documentsList.clear();
        experts.clear();
        intExperts.clear();
    }

    private void parseQuery(String query)
    {
//        if (query.contains("-"))
        queryTerms = query.split("-");
    }

    private void insertEntryToSet(double new_point, String userId)
    {
        if (isFirstTerm)
        {
            if (experts.containsKey(userId))
            {
                double point = experts.get(userId);
                point += new_point;
                experts.put(userId, point);
            }
            else
            {
                experts.put(userId, new_point);
            }
        }
        else
        {
            double mul_point = experts.get(userId);
            double point = mul_point * new_point;
            if (intExperts.containsKey(userId))
            {
                double p = intExperts.get(userId);
                p += point;
                intExperts.put(userId, p);
            }
            else
            {
                intExperts.put(userId, point);
            }
        }
    }

    private void luceneDefaultExpertRetrieval(int n, String topic) throws IOException, ParseException
    {

        //By Sajad
//        QueryParser parser = new QueryParser("Body", new StandardAnalyzer());
//        Query query = parser.parse(topic);
//        Similarity sim = new LMJelinekMercerSimilarity(1);
//        iSearcher.setSimilarity(sim);
        //By Arashdn:
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);
        booleanQuery.add(new QueryParser("Body", new StandardAnalyzer()).parse(topic), BooleanClause.Occur.MUST);
        Query query = booleanQuery.build();
        iSearcher = new IndexSearcher(iReader);
        TopDocs hits = iSearcher.search(query, n);
        for (int i = 0; i < hits.scoreDocs.length; i++)
        {
            try
            {
                Document doc = iSearcher.doc(hits.scoreDocs[i].doc);
                documentsList.add(doc);
            }
            catch(AssertionError a)
            {
                System.out.println(a.getMessage());
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    private static LinkedHashMap<String, Double> sortHashMapByValues(
            HashMap<String, Double> passedMap)
    {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);
        Collections.reverse(mapValues);

        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();

        for (double val : mapValues)
        {
            Iterator<String> keyIt = mapKeys.iterator();
            while (keyIt.hasNext())
            {
                String key = keyIt.next();
                Double comp1 = passedMap.get(key);
                Double comp2 = val;

                if (comp1.equals(comp2))
                {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    public void calculate() throws IOException, ParseException
    {
        HashMap<String, Double> experts;
        ArrayList<String> queries;
        double precisionAt1 = 0;
        double precisionAt5 = 0;
        double precisionAt10 = 0;
        double AP = 0;
        double MAP = 0;
        double MP1 = 0;
        double MP5 = 0;
        double MP10 = 0;
        queries = new ArrayList<>();
        queries = TextReader.getQueries(mainTag);
        experts = new HashMap<>();
        int n = 50000;
        int params[] = new int[]{100};
        for (int param : params)
        {
            System.out.println("Using "+param+" topics");
            setup(param);
            TextWriter.writeFileHeader(param);
            for (String query : queries)
            {
                System.out.println("Processing query " + query + "...");
                Statistics.setup(query);
                experts = getExperts(n, query);
                if (experts.size() != 0)
                {
                    Statistics.initExpertList(new ArrayList<>(experts.keySet()));
                    precisionAt1 = Statistics.percisionAt1();
                    precisionAt5 = Statistics.percisionAt5();
                    precisionAt10 = Statistics.percisionAt10();
                    AP = Statistics.AP();
                    MAP += AP;
                    MP1 += precisionAt1;
                    MP5 += precisionAt5;
                    MP10 += precisionAt10;
                    System.out.println("for n = " + n + " Average Precision:" + AP);
                }
                else if (experts.size() == 0)
                {
                    System.out.println("No expert found!!");
                    precisionAt1 = 0;
                    precisionAt5 = 0;
                    precisionAt10 = 0;
                    AP = 0;
                }
                TextWriter.writeToFile(n, query, precisionAt1, precisionAt5, precisionAt10, AP, param);
                precisionAt1 = 0;
                precisionAt5 = 0;
                precisionAt10 = 0;
                AP = 0;
                experts.clear();
            }
            MAP = MAP / (double) 100;
            MP1 = MP1 / (double) 100;
            MP5 = MP5 / (double) 100;
            MP10 = MP10 / (double) 100;
            System.out.println("The MAP is: " + MAP);
            TextWriter.writeToFile(n, "", MP1, MP5, MP10, MAP, param);
            MAP = 0;
            MP1 = 0;
            MP5 = 0;
            MP10 = 0;
        }
    }
}

class Statistics
{

    private static ArrayList<String> experts;
    private static String query;
//    private static String directory;
    private static ArrayList<String> real_rankings;

    public static void initExpertList(ArrayList<String> e) throws IOException
    {
        experts = e;
    }

    public static void setup(String q) throws IOException
    {
        real_rankings = TextReader.getExpertRealRanks(q);
//        TextReader.unsetRealRankedUserList();
        query = q;
    }

    public static double percisionAt1()
    {
        if (real_rankings.contains(experts.get(0)))
        {
            return 1;
        }
        return 0;
    }

    public static double percisionAt5()
    {
        int relevant = 0;
//        int j = Math.min(real_rankings.size(),5);
//        int k = Math.min(5, experts.size());
        for (int i = 0; i < 5; i++)
        {
            if (real_rankings.contains(experts.get(i)))
            {
                relevant += 1;
            }
        }
        return (double) relevant / 5;
    }

    public static double percisionAt10()
    {
        int relevant = 0;
//        int size = real_rankings.size();
//        int j = Math.min(size, 10);
//        int k = Math.min(experts.size(), 10);
        for (int i = 0; i < 10; i++)
        {
            if (real_rankings.contains(experts.get(i)))
            {
                relevant += 1;
            }
        }
        return (double) relevant / 10;
    }

    public static double AP()
    {
        int relevant = 0;
        double AvPrc = 0L;
        for (int i = 0; i < experts.size(); i++)
        {
            if (real_rankings.contains(experts.get(i)))
            {
                relevant += 1;
                AvPrc += (double) relevant / (i + 1);
            }
        }
        return AvPrc / (double) real_rankings.size();
    }
}

class TextWriter
{

    static String csvFile = "./data/TopicModeling/Result/topicModeling";

    public static <T> void writeToFile(int n, String query, double precisionAt1, double precisionAt5, double precisionAt10, double map, int param) throws IOException
    {
        FileWriter writer = new FileWriter(csvFile + param + ".csv", true);
        writeLine(writer, Arrays.asList(String.valueOf(n), query, String.valueOf(map), String.valueOf(precisionAt1),
                String.valueOf(precisionAt5), String.valueOf(precisionAt10)));
        writer.flush();
        writer.close();
    }

    private static void writeLine(Writer w, List<String> values) throws IOException
    {
        StringBuilder sb = new StringBuilder();

        for (String value : values)
        {
            sb.append(value + ",");
        }
        sb.setLength(sb.length() - 1);
        sb.append("\n");
        w.append(sb.toString());
    }

    public static void writeFileHeader(int param) throws IOException
    {
        FileWriter writer = new FileWriter(csvFile + param + ".csv", true);
        writeLine(writer, Arrays.asList("N", "Query", "MAP", "p@1", "p@5", "p@10"));
        writer.flush();
        writer.close();

    }

}