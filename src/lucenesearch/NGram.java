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
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import lucenesearch.LuceneTools.ExtendedDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author arashdn
 */
public class NGram
{

    private HashMap<Integer,ArrayList<String>> tags;
    private String mainTag;
    public NGram(String mainTag) throws FileNotFoundException, IOException
    {
        init(mainTag);
    }
    
    private void init(String mainTag) throws FileNotFoundException, IOException
    {
        this.mainTag = mainTag;
        tags = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("./data/"+mainTag+"_a_tag.txt"));
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
    
    public void getNGram(int n) throws IOException, ParseException
    {
        getNGram(n , 10000000);
    }
    
    public void getNGram(int n , int hitPP) throws IOException, ParseException
    {
        int hitsPerPage = hitPP;
        
        String index = new Searcher().getPostIndexPath();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        
        //booleanQuery.add(new QueryParser("Body", analyzer).parse(""), BooleanClause.Occur.MUST);
        booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);
        
        TopDocs results;

        results = searcher.search(booleanQuery.build(), hitsPerPage);
        
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");

        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);
        
        PrintWriter pw = new PrintWriter("./data/grams/"+n+"gram.csv");

        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
        {
            Document doc = searcher.doc(hits[i].doc);
            ArrayList<String[]> tmp = getNGrams(doc,new ExtendedDocument(hits[i].doc, reader),n);
            for (String[] ngrams : tmp)
            {
                sb = new StringBuilder();
                sb.append(doc.get("SId"));
                sb.append(",");
                sb.append(toTabbedStr(ngrams));
                sb.append(",");
                ArrayList<String> tagg = tags.get(Integer.parseInt(doc.get("SId")));
                sb.append(implodeTabbed(tagg));
                sb.append("\n");
                if(tagg.size()>1)
                    pw.print(sb.toString());
            }
            
        }
        pw.close();
    }
    
    private String toTabbedStr(String[] ngrams)
    {
        StringBuilder sb = new StringBuilder();
        int n = ngrams.length;
        for (int i = 0; i < n; i++)
        {
            sb.append(ngrams[i]);
            sb.append("\t");
        }
        return sb.toString().trim();
    }
    
    private ArrayList<String[]> getNGrams(Document originalDoc,ExtendedDocument doc, int n) throws IOException
    {
        if(n == 1)
        {
            return get1Gram(originalDoc,doc);
        }
        else if(n == 2)
        {
            return get2Gram(doc);
        }
        else if(n == 4)
        {
            return get4Gram(doc);
        }
        else
            return null;
    }

    private ArrayList<String[]> get1Gram(Document originalDoc,ExtendedDocument doc) throws IOException
    {
        ArrayList<String[]> res = new ArrayList<>();
        
//        Terms t = doc.getTermVector("Body");
//        TermsEnum itr = t.iterator();
//        BytesRef term;
//        while ((term = itr.next()) != null)
//        {
//            String termText = term.utf8ToString();
//            String[] tmp= new String[1];
//            tmp[0] = termText;
//            res.add(tmp);
//        }
        
        
        ArrayList<String> t = new lucenesearch.LuceneTools.LuceneUtils(null).getAnalyzedRemoveHtml(originalDoc.get("Body"));
        for (String termText : t)
        {
            String[] tmp= new String[1];
            tmp[0] = termText;
            res.add(tmp);
        }
        
        return res;
    }

    private ArrayList<String[]> get2Gram(ExtendedDocument doc) throws IOException
    {
        ArrayList<String[]> res = new ArrayList<>();
        
        
        Terms t = doc.getTermVector("Body");
        TermsEnum itr = t.iterator();
        BytesRef term;
        ArrayList<String> terms = new ArrayList<>();
        while ((term = itr.next()) != null)
        {
            String termText = term.utf8ToString();
            terms.add(termText);
        }
                        
        int n = terms.size();
        
        for (int i = 1; i < n; i++)
        {
            String [] temp = new String[2];
            temp[0] = terms.get(i-1);
            temp[1] = terms.get(i);
            res.add(temp);
        }
        
        return res;
    }

    private ArrayList<String[]> get4Gram(ExtendedDocument doc) throws IOException
    {
        ArrayList<String[]> res = new ArrayList<>();
        
        
        Terms t = doc.getTermVector("Body");
        TermsEnum itr = t.iterator();
        BytesRef term;
        ArrayList<String> terms = new ArrayList<>();
        while ((term = itr.next()) != null)
        {
            String termText = term.utf8ToString();
            terms.add(termText);
        }
                        
        int n = terms.size();
        
        for (int i = 3; i < n; i++)
        {
            String [] temp = new String[4];
            temp[0] = terms.get(i-3);
            temp[1] = terms.get(i-2);
            temp[2] = terms.get(i-1);
            temp[3] = terms.get(i);
            res.add(temp);
        }
        
        return res;
    }


    private String implodeTabbed(ArrayList<String> tags)
    {
        StringBuilder sb = new StringBuilder();
        for (String tag : tags)
        {
            if(!tag.equalsIgnoreCase(mainTag))
                sb.append(tag).append("\t");
        }
        String res = sb.toString();
        if(res.length() == 0)
            res = "#";
        return res.substring(0, res.length()-1);
    }


}
