package lucenesearch;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author arashdn
 */
public class Balog
{
    public ArrayList<Integer> getGoldenList(String goldenFile) throws IOException
    {
        java.nio.file.Path filePath = new java.io.File("data/goldens/"+goldenFile).toPath();
        List<String> stringList = Files.readAllLines(filePath);
        ArrayList<Integer> res = new ArrayList<>();
        boolean isFirst = true;
        for (String s : stringList)
        {
            if(isFirst)
            {
                isFirst = false;
                continue;
            }
            res.add(Integer.parseInt(s.split(",")[0]));
        }
        return res;
    }
    
    protected double getLambda(Double beta , Integer len)
    {
        if(beta == null && len == null)
            return 0.7;
        
        return beta / (double)(beta + len);
        
    }
    
    public double balog1(String bodyTerm) throws IOException, ParseException
    {
        return balog1(bodyTerm, true);
    }
    
    public double balog1(String bodyTerm , boolean printDebug) throws IOException, ParseException
    {
        
        String goldenFile = Utility.getGoldenFileName(bodyTerm);
        
        int hitsPerPage = 500000;

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
        results = searcher.search(q , 5 * hitsPerPage);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        if(printDebug)
            System.out.println(numTotalHits + " Total document found.");
        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage); 

               
        HashMap<Integer, ArrayList<Integer> > users = new HashMap<>();
        int errorUsers = 0;
        for (int i = start; i < end; i++)
        {
            int docID = hits[i].doc;
            Document doc = searcher.doc(docID);
            int uid = 0;
            try
            {
                uid = Integer.parseInt(doc.get("SOwnerUserId"));
                if(users.containsKey(uid))
                {
                    users.get(uid).add(docID);
                }
                else
                {
                    ArrayList<Integer> l = new ArrayList<>();
                    l.add(docID);
                    users.put(uid, l );
                }
            }
            catch (Exception ex)
            {
                //System.out.println("UID: "+doc.get("SOwnerUserId"));
                //System.out.println("Error on doc "+i+" , doc ID: "+doc.get("SId"));
                errorUsers++;
            }
        }
        if(printDebug)
            System.out.println("Users ready , "+errorUsers+" answers with out userId");
        
                
        
        ArrayList<String> queryTokens = new ArrayList<>();
        
        TokenStream tstream  = analyzer.tokenStream(null, new StringReader(bodyTerm));
        tstream.reset();
        while (tstream.incrementToken()) //for term t in query
        {
            queryTokens.add(tstream.getAttribute(CharTermAttribute.class).toString());
        }
        tstream.close();
        if(printDebug)
            System.out.println("Query Len: "+queryTokens.size());
                
        double score;
        HashMap<Integer, Double > userScores = new HashMap<>();
        for (Map.Entry<Integer, ArrayList<Integer>> entry : users.entrySet()) 
        {
            score = -1;
            for(String t : queryTokens)
            {
                double tmp = 0;
                int totalLen = 0;
                int totalTerm = 0;
                for (int uid : entry.getValue())
                {
                    ExtendedDocument ed = new ExtendedDocument(uid, reader);
                    tmp += (  (double)(ed.getTermFrequency("Body").get(t) == null ? 0 : ed.getTermFrequency("Body").get(t))   / (double)ed.getTermsCount("Body")   )*1;//1 is p(d|e)
                    totalLen += ed.getTermsCount("Body");
                    totalTerm += ed.getTermsCount("Body");
                }// for d in De
                LuceneUtils lu = new LuceneUtils(reader);
                double tf_t_col = lu.getTermFrequencyInCollection("Body", t);
                double size_col = lu.getCountOfAllTerms("Body");
                
                double lambda = getLambda((double)totalTerm/users.size(), totalLen);
                
                if(score == -1)
                    score = (1-lambda)*tmp + lambda* (tf_t_col/size_col);
                else
                    score *= (1-lambda)*tmp + lambda* (tf_t_col/size_col);

            }//for t in q
            
            userScores.put(entry.getKey(), score);
        }
        
        ValueComparator bvc = new ValueComparator(userScores);
        TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);
        sorted_map.putAll(userScores);
        
        ArrayList<Integer> lst = new ArrayList<>();

        for (Map.Entry<Integer, Double> entry : sorted_map.entrySet()) 
        {
            if(printDebug)
                System.out.println("{" + entry.getKey() + " } -> "+entry.getValue());
            lst.add(entry.getKey());
        }
        double map = new Evaluator().map(lst, getGoldenList(goldenFile));
        if(printDebug)
            System.out.println("MAP= "+map);
        return map;
                        
    }
    
    public ArrayList<EvalResult> balog1ForAllTags() throws IOException, ParseException
    {
        ArrayList<String> tags = Utility.getTags();
        ArrayList<EvalResult> res = new ArrayList<>();
        double map;
        for (String tag : tags)
        {
            System.out.print(tag+": ");
            map = balog1(tag,false);
            EvalResult er = new EvalResult();
            er.setMap(map);
            er.setTag(tag);
            System.out.println(map);
            res.add(er);
        }
        
        Collections.sort(res);
        double sum = 0;
        for (EvalResult re : res)
        {
            sum += re.getMap();
            System.out.println(re);
        }
        System.out.println("Avg Map: "+(sum/res.size()));
        return res;
    }
    
}
