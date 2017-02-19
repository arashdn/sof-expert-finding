package lucenesearch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
            return 0.5;
        
        return beta / (double)(beta + len);
        
    }
    
    public EvalResult balog1(String bodyTerm) throws IOException, ParseException
    {
        return balog1(bodyTerm, true, null);
    }
    
    public EvalResult balog1(String bodyTerm , boolean printDebug) throws IOException, ParseException
    {
        return balog1(bodyTerm, printDebug, null);
    }
    public EvalResult balog1(String bodyTerm , boolean printDebug , String goldenName) throws IOException, ParseException
    {
        return balog1(bodyTerm, printDebug, goldenName,null);
    }
    
    public EvalResult balog1(String bodyTerm , boolean printDebug , String goldenName  ,Integer N) throws IOException, ParseException
    {
        return balog1(bodyTerm, printDebug, goldenName,N,null);
    }
    
    public EvalResult balog1(String bodyTerm , boolean printDebug , String goldenName  ,Integer N , Double Beta) throws IOException, ParseException
    {
        return balog1(bodyTerm, printDebug, goldenName,N,Beta,null);
    }
    
    public EvalResult balog1(String bodyTerm , boolean printDebug , String goldenName , Integer N , Double Beta,Double Lambda) throws IOException, ParseException
    {
        String goldenFile;
        if(goldenName == null)
            goldenFile = Utility.getGoldenFileName(bodyTerm);
        else
            goldenFile = Utility.getGoldenFileName(goldenName);
       
        if(N == null)
            N = 500000;
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
                
                double lambda;
                double beta;
                if(Beta == null)
                    beta = (double)totalTerm/users.size();
                else
                    beta = Beta;
                if(Lambda == null)
                    lambda = getLambda(beta, totalLen);
                else
                    lambda = Lambda;
                
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
        Evaluator ev = new Evaluator();
        double map = ev.map(lst, getGoldenList(goldenFile));
        double p1 = ev.precisionAtK(lst, getGoldenList(goldenFile),1);
        double p5 = ev.precisionAtK(lst, getGoldenList(goldenFile),5);
        double p10 = ev.precisionAtK(lst, getGoldenList(goldenFile),10);
        if(printDebug)
            System.out.println("MAP= "+map);
        return new EvalResult(bodyTerm, map, p1, p5, p10);
                        
    }
    
    public ArrayList<EvalResult> balog1ForAllTags() throws IOException, ParseException
    {
        System.out.println("tag:map,p@1,p@5,p@10");
        ArrayList<String> tags = Utility.getTags();
        ArrayList<EvalResult> res = new ArrayList<>();
        double map;
        for (String tag : tags)
        {
            System.out.print(tag+": ");
            EvalResult er = balog1(tag,false,null,10000,null,0.5);
            System.out.println(er.getMap()+","+er.getP1()+","+er.getP5()+","+er.getP10());
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
    
    
    public EvalResult balog2(String bodyTerm) throws IOException, ParseException
    {
        return balog2(bodyTerm, true, null);
    }
    
    public EvalResult balog2(String bodyTerm , boolean printDebug) throws IOException, ParseException
    {
        return balog2(bodyTerm, printDebug, null);
    }
    
    public EvalResult balog2(String bodyTerm , boolean printDebug , String goldenName  ,Integer N) throws IOException, ParseException
    {
        return balog2(bodyTerm, printDebug, goldenName,N,null);
    }
    
    public EvalResult balog2(String bodyTerm , boolean printDebug , String goldenName  ,Integer N , Double Beta) throws IOException, ParseException
    {
        return balog2(bodyTerm, printDebug, goldenName,N,Beta,null);
    }
    

    
    public EvalResult balog2(String bodyTerm , boolean printDebug , String goldenName) throws IOException, ParseException
    {
        return balog2(bodyTerm, printDebug, goldenName,null);
    }
    
    public EvalResult balog2(String bodyTerm , boolean printDebug , String goldenName , Integer N , Double Beta,Double Lambda) throws IOException, ParseException
    {
        String goldenFile;
        if(goldenName == null)
            goldenFile = Utility.getGoldenFileName(bodyTerm);
        else
            goldenFile = Utility.getGoldenFileName(goldenName);
        
        HashMap<Integer, Double > userScores = calculateBalog2(N, bodyTerm, printDebug, Beta, Lambda);
        
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
        Evaluator ev = new Evaluator();
        double map = ev.map(lst, getGoldenList(goldenFile));
        double p1 = ev.precisionAtK(lst, getGoldenList(goldenFile),1);
        double p5 = ev.precisionAtK(lst, getGoldenList(goldenFile),5);
        double p10 = ev.precisionAtK(lst, getGoldenList(goldenFile),10);
        if(printDebug)
            System.out.println("MAP= "+map);
        return new EvalResult(bodyTerm, map, p1, p5, p10);
                        
    }

    public HashMap<Integer, Double> calculateBalog2(Integer N, String bodyTerm, boolean printDebug, Double Beta, Double Lambda) throws ParseException, IOException
    {
        return calculateBalog2(N, bodyTerm, printDebug, Beta, Lambda, false, null , "");
    }
    public HashMap<Integer, Double> calculateBalog2(Integer N, String bodyTerm, boolean printDebug, Double Beta, Double Lambda , boolean taggedAnswerOnly, HashMap<Integer,ArrayList<String>> tags, String originalTag) throws ParseException, IOException
    {
        HashMap<Integer, Double> userScores;
        if (N == null)
        {
            N = 500000;
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
        if (printDebug)
        {
            System.out.println(numTotalHits + " Total document found.");
        }
        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);
        ArrayList<String> queryTokens = new ArrayList<>();
        TokenStream tstream = analyzer.tokenStream(null, new StringReader(bodyTerm));
        tstream.reset();
        while (tstream.incrementToken()) //for term t in query
        {
            queryTokens.add(tstream.getAttribute(CharTermAttribute.class).toString());
        }
        tstream.close();
        if (printDebug)
        {
            System.out.println("Query Len: " + queryTokens.size() + " " + queryTokens);
        }
        HashMap<String, Double> collectionProbForTerms = new HashMap<>();
        LuceneUtils lu = new LuceneUtils(reader);
        for (String t : queryTokens)
        {
            double tf_t_col = lu.getTermFrequencyInCollection("Body", t);
            double size_col = lu.getCountOfAllTerms("Body");
            double p_t_col = (tf_t_col / size_col);
            collectionProbForTerms.put(t, p_t_col);
        }//for t in q
        long docsLen = 0;
        int docCount = 0;
        for (int i = start; i < end; i++) // calculate docLen AVg
        {
            int docID = hits[i].doc;
            int uid = -1;
            Document doc = searcher.doc(docID);

            ExtendedDocument ed = new ExtendedDocument(docID, reader);
            docCount++;
            docsLen += ed.getTermsCount("Body");
        }
        double beta;
        if (Beta == null)
        {
            beta = (double) docsLen / docCount;
        }
        else
        {
            beta = Beta;
        }
        if (printDebug)
        {
            System.out.println("Avg DocsLen: " + (double) docsLen / docCount);
        }
        double score;
        int errorUsers = 0;
        userScores = new HashMap<>();
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
            
            if(taggedAnswerOnly)
            {
                ArrayList<String> tg = tags.get(Integer.parseInt(doc.get("SId")));
                if(!tg.contains(originalTag))
                    continue;
            }

            ExtendedDocument ed = new ExtendedDocument(docID, reader);
            double lambda;
            if (Lambda == null)
            {
                lambda = getLambda(beta, ed.getTermsCount("Body"));
            }
            else
            {
                lambda = Lambda;
            }

            score = -1;
            for (String t : queryTokens)
            {
                double p_t_d = (double) (ed.getTermFrequency("Body").get(t) == null ? 0 : ed.getTermFrequency("Body").get(t)) / (double) ed.getTermsCount("Body");
                double p_t_col = collectionProbForTerms.get(t);
                if (score == -1)
                {
                    score = ((1 - lambda) * p_t_d) + (lambda * p_t_col);
                }
                else
                {
                    score *= ((1 - lambda) * p_t_d) + (lambda * p_t_col);
                }
            }//for t in q

            double p_d_e = 1;
            double totScore = score * p_d_e;
            if (userScores.containsKey(uid))
            {
                double oldScore = userScores.get(uid);
                userScores.replace(uid, totScore + oldScore);
            }
            else
            {
                userScores.put(uid, totScore);
            }
        }
        if (printDebug)
        {
            System.out.println("Result ready , " + errorUsers + " answers with out userId");
        }
        return userScores;
    }
    
    public ArrayList<EvalResult> balog2ForAllTags() throws IOException, ParseException
    {
        System.out.println("tag:map,p@1,p@5,p@10");
        ArrayList<String> tags = Utility.getTags();
        ArrayList<EvalResult> res = new ArrayList<>();
        double map;
        for (String tag : tags)
        {
            System.out.print(tag+":");
            EvalResult er = balog2(tag,false,null,10000,null,0.5);
            System.out.println(er.getMap()+","+er.getP1()+","+er.getP5()+","+er.getP10());
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
    
    public void balog2ForAllTagsCsv() throws IOException, ParseException
    {
        ArrayList<String> tags = Utility.getTags();
        ArrayList<EvalResult> res = new ArrayList<>();
        
        Integer N = 10000;
        Double Beta = null;
        Double Lambda = 1.0;
        
        PrintWriter out = new PrintWriter("data/res_balog2/N"+N+"_B"+Beta+"_L"+Lambda+".csv");
        out.println("N,B,L,Query,Map,p@1,p@5,p@10");
        
        double sumMap = 0.0;
        double sumP1 = 0.0;
        double sumP5 = 0.0;
        double sumP10 = 0.0;
        
        for (String tag : tags)
        {
            System.out.print(tag+": ");
            EvalResult er = balog2(tag,false,null,N,Beta,Lambda);
            System.out.println(er.getMap());
            sumMap += er.getMap();
            sumP1 += er.getP1();
            sumP5 += er.getP5();
            sumP10 += er.getP10();
            out.println(N+","+Beta+","+Lambda+","+tag+","+er.getMap()+","+er.getP1()+","+er.getP5()+","+er.getP10()+",");
            res.add(er);
        }
        out.close();
        
        Collections.sort(res);
        for (EvalResult re : res)
        {
            System.out.println(re);
        }
        System.out.println("Avg Map: "+(sumMap/res.size()));
        System.out.println("Avg P1: "+(sumP1/res.size()));
        System.out.println("Avg P5: "+(sumP5/res.size()));
        System.out.println("Avg P10: "+(sumP10/res.size()));
    }
    
    public void balog1ForAllTagsCsv() throws IOException, ParseException
    {
        ArrayList<String> tags = Utility.getTags();
        ArrayList<EvalResult> res = new ArrayList<>();
        
        Integer N = 10000;
        Double Beta = null;
        Double Lambda = 1.0;
        
        PrintWriter out = new PrintWriter("data/res_balog1/N"+N+"_B"+Beta+"_L"+Lambda+".csv");
        out.println("N,B,L,Query,Map,p@1,p@5,p@10");
        
        double sumMap = 0.0;
        double sumP1 = 0.0;
        double sumP5 = 0.0;
        double sumP10 = 0.0;
        
        for (String tag : tags)
        {
            System.out.print(tag+": ");
            EvalResult er = balog1(tag,false,null,N,Beta,Lambda);
            System.out.println(er.getMap());
            sumMap += er.getMap();
            sumP1 += er.getP1();
            sumP5 += er.getP5();
            sumP10 += er.getP10();
            out.println(N+","+Beta+","+Lambda+","+tag+","+er.getMap()+","+er.getP1()+","+er.getP5()+","+er.getP10()+",");
            res.add(er);
        }
        out.close();
        
        Collections.sort(res);
        for (EvalResult re : res)
        {
            System.out.println(re);
        }
        System.out.println("Avg Map: "+(sumMap/res.size()));
        System.out.println("Avg P1: "+(sumP1/res.size()));
        System.out.println("Avg P5: "+(sumP5/res.size()));
        System.out.println("Avg P10: "+(sumP10/res.size()));
    }
    
    
}
