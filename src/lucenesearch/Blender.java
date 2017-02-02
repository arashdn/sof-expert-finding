package lucenesearch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author arashdn
 */
public class Blender
{
    private HashMap<Integer,ArrayList<String>> tags;
    private String filePath;

    public Blender(String path) throws IOException
    {
        filePath = path;
        getTags();
    }
    
    
    
    private void getTags() throws FileNotFoundException, IOException
    {
        tags = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("./data/java_a_tag.txt"));
        String line = "";
        while((line = br.readLine()) != null)
        {
            String [] tmp = line.split(",");
            int aid = Integer.parseInt(tmp[0]);
            ArrayList<String> tg = tags.get(aid);
            if(tg == null)
            {
                tg = new ArrayList<>();
                tags.put(aid, tg);
            }
            //if(tmp[1] != null && tmp[1] != "" && !tmp[1].equalsIgnoreCase("java"))
            tg.add(tmp[1]);
        }
    }
    
    public void blendAnd(int countWords) throws IOException, ParseException
    {
        java.nio.file.Path filePath = new java.io.File(this.filePath).toPath();
        List<String> stringList = Files.readAllLines(filePath);
        
        HashMap<String,ArrayList<ProbTranslate>> tags = new HashMap<>();
        for (String s : stringList)
        {
            String[] tgs = s.split("~");
            ArrayList<ProbTranslate> e = new ArrayList<>();
            if(tgs.length > 1 && tgs[1] != null && tgs[1] != "")
            {
                String [] trs = tgs[1].split(",");
                
                for (int i = 0; i<countWords ; i++)
                {
                    String [] t = trs[i].split(":");
                    e.add(new ProbTranslate(t[0], 1));
//                    e.add(new ProbTranslate(t[0], Double.parseDouble(t[1])));
                }
            }
            else
            {
                e.add(new ProbTranslate(tgs[0], 1));
            }
            tags.put(tgs[0], e );
        }
        Iterator it = tags.entrySet().iterator();
        HashMap<Integer, Double > userScores = null;
        HashMap<Integer, Double > totalUserScores = new HashMap<>();
        while (it.hasNext()) 
        {
            totalUserScores = new HashMap<>();
            Map.Entry pair = (Map.Entry)it.next();
            String tag = pair.getKey().toString();
            ArrayList<ProbTranslate> trans = (ArrayList<ProbTranslate>) pair.getValue();

            
            for (ProbTranslate tran : trans)
            {
                //System.out.println(tag+" -> "+tran.getWord()+": ");
                userScores = null;
                userScores = getTransaltionScore(10000, tran.getWord());
                
                Iterator it2 = userScores.entrySet().iterator();
                while (it2.hasNext())
                {
                    Map.Entry pair2 = (Map.Entry) it2.next();
                    if (totalUserScores.containsKey(pair2.getKey()))
                    {
                        double oldScore = userScores.get(pair2.getKey());
                        totalUserScores.replace(Integer.parseInt(pair2.getKey().toString()), tran.getProb()*Double.parseDouble(pair2.getValue().toString()) + oldScore);
                    }
                    else
                    {
                        totalUserScores.put(Integer.parseInt(pair2.getKey().toString()), tran.getProb()*Double.parseDouble(pair2.getValue().toString()));
                    }
                    it2.remove(); // avoids a ConcurrentModificationException
                }

            }
            ValueComparator3 bvc = new ValueComparator3(totalUserScores);
            TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);
            sorted_map.putAll(totalUserScores);
            ArrayList<Integer> lst = new ArrayList<>();

            for (Map.Entry<Integer, Double> entry : sorted_map.entrySet()) 
            {
                lst.add(entry.getKey());
            }
            Evaluator ev = new Evaluator();
            Balog balog = new Balog();
            double map = ev.map(lst, balog.getGoldenList( Utility.getGoldenFileName(tag)));
            System.out.println(tag+","+map);
//        double p1 = ev.precisionAtK(lst, getGoldenList(goldenFile),1);
//        double p5 = ev.precisionAtK(lst, getGoldenList(goldenFile),5);
//        double p10 = ev.precisionAtK(lst, getGoldenList(goldenFile),10);
        
            
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
    
    public HashMap<Integer, Double> getTransaltionScore(Integer N,String bodyTerm) throws IOException, ParseException
    {
        HashMap<Integer, Double> userScores = new HashMap<>();
        if (N == null)
        {
            N = 10000;
        }
        int hitsPerPage = N;
        String index = new Searcher().getPostIndexPath();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        //        float smoothing = 0.7f;
//        LMJelinekMercerSimilarity sim = new LMJelinekMercerSimilarity(smoothing);
//        searcher.setSimilarity(sim);

        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);
        booleanQuery.add(new QueryParser("Body", analyzer).parse(bodyTerm), BooleanClause.Occur.MUST);
        Query q = booleanQuery.build();
        TopDocs results;
        results = searcher.search(q, 5 * hitsPerPage);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);
        int errorUsers = 0;
        for (int i = start; i < end; i++)
        {
            int docID = hits[i].doc;
            int uid = -1;
            Document doc = searcher.doc(docID);
            Post p = new Post(doc);
            try
            {
                uid = Integer.parseInt(doc.get("SOwnerUserId"));
            }
            catch (Exception ex)
            {
                errorUsers++;
                continue;
            }
            if (userScores.containsKey(uid))
            {
                double oldScore = userScores.get(uid);
                userScores.replace(uid, 1 + oldScore);
            }
            else
            {
                userScores.put(uid, 1.0);
            }
        }
        return userScores;
    }


    public void blendOr(int countWords) throws IOException, ParseException
    {
        java.nio.file.Path filePath = new java.io.File(this.filePath).toPath();
        List<String> stringList = Files.readAllLines(filePath);
        
        HashMap<String,ArrayList<ProbTranslate>> tags = new HashMap<>();
        for (String s : stringList)
        {
            String[] tgs = s.split("~");
            ArrayList<ProbTranslate> e = new ArrayList<>();
            if(tgs.length > 1 && tgs[1] != null && tgs[1] != "")
            {
                String [] trs = tgs[1].split(",");
                
                for (int i = 0; i<countWords ; i++)
                {
                    String [] t = trs[i].split(":");
                    e.add(new ProbTranslate(t[0], 1));
//                    e.add(new ProbTranslate(t[0], Double.parseDouble(t[1])));
                }
            }
            else
            {
                e.add(new ProbTranslate(tgs[0], 1));
            }
            tags.put(tgs[0], e );
        }
        Iterator it = tags.entrySet().iterator();
        HashMap<Integer, Double > userScores = null;
        HashMap<Integer, Double > totalUserScores = new HashMap<>();
        while (it.hasNext()) 
        {
            totalUserScores = new HashMap<>();
            Map.Entry pair = (Map.Entry)it.next();
            String tag = pair.getKey().toString();
            ArrayList<ProbTranslate> trans = (ArrayList<ProbTranslate>) pair.getValue();

            totalUserScores = getTransaltionScoreOr(10000, trans);
            
            ValueComparator3 bvc = new ValueComparator3(totalUserScores);
            TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);
            sorted_map.putAll(totalUserScores);
            ArrayList<Integer> lst = new ArrayList<>();

            for (Map.Entry<Integer, Double> entry : sorted_map.entrySet()) 
            {
                lst.add(entry.getKey());
            }
            Evaluator ev = new Evaluator();
            Balog balog = new Balog();
            double map = ev.map(lst, balog.getGoldenList( Utility.getGoldenFileName(tag)));
            System.out.println(tag+","+map);
//        double p1 = ev.precisionAtK(lst, getGoldenList(goldenFile),1);
//        double p5 = ev.precisionAtK(lst, getGoldenList(goldenFile),5);
//        double p10 = ev.precisionAtK(lst, getGoldenList(goldenFile),10);
        
            
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public HashMap<Integer, Double> getTransaltionScoreOr(Integer N,ArrayList<ProbTranslate> trans) throws IOException, ParseException
    {
        HashMap<Integer, Double> userScores = new HashMap<>();
        if (N == null)
        {
            N = 10000;
        }
        int hitsPerPage = N;
        String index = new Searcher().getPostIndexPath();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        //        float smoothing = 0.7f;
//        LMJelinekMercerSimilarity sim = new LMJelinekMercerSimilarity(smoothing);
//        searcher.setSimilarity(sim);

        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);
        
        for (ProbTranslate tran : trans)
        {
            booleanQuery.add(new QueryParser("Body", analyzer).parse(tran.getWord()), BooleanClause.Occur.SHOULD);

        }
        Query q = booleanQuery.build();
        TopDocs results;
        results = searcher.search(q, 5 * hitsPerPage);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);
        int errorUsers = 0;
        for (int i = start; i < end; i++)
        {
            int docID = hits[i].doc;
            int uid = -1;
            Document doc = searcher.doc(docID);
            Post p = new Post(doc);
            try
            {
                uid = Integer.parseInt(doc.get("SOwnerUserId"));
            }
            catch (Exception ex)
            {
                errorUsers++;
                continue;
            }
            if (userScores.containsKey(uid))
            {
                double oldScore = userScores.get(uid);
                userScores.replace(uid, 1 + oldScore);
            }
            else
            {
                userScores.put(uid, 1.0);
            }
        }
        return userScores;
    }
    
    public void blendBalog(int countWords) throws IOException, ParseException
    {
        java.nio.file.Path filePath = new java.io.File(this.filePath).toPath();
        List<String> stringList = Files.readAllLines(filePath);
        
        HashMap<String,ArrayList<ProbTranslate>> tags = new HashMap<>();
        for (String s : stringList)
        {
            String[] tgs = s.split("~");
            ArrayList<ProbTranslate> e = new ArrayList<>();
            if(tgs.length > 1 && tgs[1] != null && tgs[1] != "")
            {
                String [] trs = tgs[1].split(",");
                
                for (int i = 0; i<countWords ; i++)
                {
                    String [] t = trs[i].split(":");
                    e.add(new ProbTranslate(t[0], 1));
//                    e.add(new ProbTranslate(t[0], Double.parseDouble(t[1])));
                }
            }
            else
            {
                e.add(new ProbTranslate(tgs[0], 1));
            }
            tags.put(tgs[0], e );
        }
        Balog balog = new Balog();
        Iterator it = tags.entrySet().iterator();
        HashMap<Integer, Double > userScores = null;
        HashMap<Integer, Double > totalUserScores = new HashMap<>();
        while (it.hasNext()) 
        {
            totalUserScores = new HashMap<>();
            Map.Entry pair = (Map.Entry)it.next();
            String tag = pair.getKey().toString();
            ArrayList<ProbTranslate> trans = (ArrayList<ProbTranslate>) pair.getValue();

            
            for (ProbTranslate tran : trans)
            {
                //System.out.println(tag+" -> "+tran.getWord()+": ");
                userScores = null;
                userScores = balog.calculateBalog2(50000, tran.getWord(), false, null, 0.5);
                
                Iterator it2 = userScores.entrySet().iterator();
                while (it2.hasNext())
                {
                    Map.Entry pair2 = (Map.Entry) it2.next();
                    if (totalUserScores.containsKey(pair2.getKey()))
                    {
                        double oldScore = userScores.get(pair2.getKey());
                        totalUserScores.replace(Integer.parseInt(pair2.getKey().toString()), tran.getProb()*Double.parseDouble(pair2.getValue().toString()) + oldScore);
                    }
                    else
                    {
                        totalUserScores.put(Integer.parseInt(pair2.getKey().toString()), tran.getProb()*Double.parseDouble(pair2.getValue().toString()));
                    }
                    it2.remove(); // avoids a ConcurrentModificationException
                }

            }
            ValueComparator3 bvc = new ValueComparator3(totalUserScores);
            TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);
            sorted_map.putAll(totalUserScores);
            ArrayList<Integer> lst = new ArrayList<>();

            for (Map.Entry<Integer, Double> entry : sorted_map.entrySet()) 
            {
                lst.add(entry.getKey());
            }
            Evaluator ev = new Evaluator();
            double map = ev.map(lst, balog.getGoldenList( Utility.getGoldenFileName(tag)));
            System.out.println(tag+","+map);
//        double p1 = ev.precisionAtK(lst, getGoldenList(goldenFile),1);
//        double p5 = ev.precisionAtK(lst, getGoldenList(goldenFile),5);
//        double p10 = ev.precisionAtK(lst, getGoldenList(goldenFile),10);
        
            
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}
