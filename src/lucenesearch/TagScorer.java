/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucenesearch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lucenesearch.LuceneTools.ExtendedDocument;
import lucenesearch.LuceneTools.LuceneUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
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
public class TagScorer
{
    private HashMap<Integer,ArrayList<String>> tags;

    public TagScorer() throws IOException
    {
        this.getTags();
    }
    
    
    
    private void getTags() throws FileNotFoundException, IOException
    {
        System.out.println("Loading tags started");
        tags = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("./data/java_all_tag.txt"));
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
        System.out.println("Loading tags done!");
    }
    
    public void caculate() throws IOException
    {
        
        HashMap<String,HashMap<Integer,Double>> tagUserScore = new HashMap<>();
            
        
        int hitsPerPage = 2000000;
        String index = new Searcher().getPostIndexPath();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);

        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);
        Query q = booleanQuery.build();
        TopDocs results;
        results = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " Total answers found.");
        
        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);
        
        int errorUsers = 0;
        for (int i = start; i < end; i++)
        {
            System.out.println("processing answer "+i+"/"+end);
            int docID = hits[i].doc;
            int uid = -1;
            Document doc = searcher.doc(docID);

            Post p = new Post(doc);
            try
            {
                uid = Integer.parseInt(doc.get("SOwnerUserId"));
                ArrayList<String> tgs = tags.get(p.getId());
                for (String tg : tgs)
                {
                    if(!tagUserScore.containsKey(tg))
                    {
                        tagUserScore.put(tg, new HashMap<>());
                    }
                    HashMap<Integer,Double> temp = tagUserScore.get(tg);
                    if(!temp.containsKey(uid))
                    {
                        temp.put(uid, 1.0);
                    }
                    else
                    {
                        temp.replace(uid, 1 + temp.get(uid));
                    }
                }
            }
            catch (Exception ex)
            {
                errorUsers++;
                continue;
            }
        }
        
        
        System.out.println("tag:map,p@1,p@5,p@10");
        for (Map.Entry<String, HashMap<Integer, Double>> entryM : tagUserScore.entrySet())
        {
            String tag = entryM.getKey();
            HashMap<Integer, Double> userScores = entryM.getValue();
            
            String goldenFile = Utility.getGoldenFileName(tag);
        
            ValueComparator bvc = new ValueComparator(userScores);
            TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);
            sorted_map.putAll(userScores);

            ArrayList<Integer> lst = new ArrayList<>();

            for (Map.Entry<Integer, Double> entry : sorted_map.entrySet()) 
            {
                lst.add(entry.getKey());
            }
            Evaluator ev = new Evaluator();
            Balog b = new Balog();
            double map = ev.map(lst, b.getGoldenList(goldenFile));
            double p1 = ev.precisionAtK(lst, b.getGoldenList(goldenFile),1);
            double p5 = ev.precisionAtK(lst, b.getGoldenList(goldenFile),5);
            double p10 = ev.precisionAtK(lst, b.getGoldenList(goldenFile),10);
            EvalResult er = new EvalResult(tag, map, p1, p5, p10);
            System.out.println(er.getMap()+","+er.getP1()+","+er.getP5()+","+er.getP10());
        
        }
        
    }
}
