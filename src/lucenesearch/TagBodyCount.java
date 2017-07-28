/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucenesearch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author arashdn
 */
public class TagBodyCount
{

    HashSet<Integer> acceptedAnswers;
    HashSet<Integer> allAcceptedAnswers;
    String mainTag;
    String searchTag;
    private HashMap<Integer, ArrayList<String>> tags;

    public TagBodyCount(String mainTag, String searchTag) throws SQLException, IOException
    {
        this.mainTag = mainTag;
        this.searchTag = searchTag;
        loadAnswers();
        loadAllAcceptedAnswers();
        getTags();
    }

    private boolean hasTag(int aid, String tag)
    {
        return tags.get(aid).contains(tag);
    }

    private void getTags() throws FileNotFoundException, IOException
    {
        System.out.println("Loading tags started");
        tags = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("./data/" + mainTag + "_all_tag.txt"));
        String line = "";
        while ((line = br.readLine()) != null)
        {
            String[] tmp = line.split(",");
            int aid = Integer.parseInt(tmp[0]);
            ArrayList<String> tg = tags.get(aid);
            if (tg == null)
            {
                tg = new ArrayList<>();
                tags.put(aid, tg);
            }
            //if(tmp[1] != null && tmp[1] != "" && !tmp[1].equalsIgnoreCase("java"))
            tg.add(tmp[1]);
        }
        System.out.println("Loading tags done!");
    }

    public void loadAnswers() throws SQLException
    {

        acceptedAnswers = new HashSet<>();

        String url = "jdbc:mysql://localhost:3306/sof";
        String username = "root";
        String password = "root";

        System.out.println("Connecting database...");

        Connection conn = DriverManager.getConnection(url, username, password);
        System.out.println("Database connected!");
        Statement stmt = conn.createStatement();
        String query = "select distinct(aid) from " + this.mainTag + "_a_tag," + this.mainTag + "_all_qa where " + this.mainTag + "_all_qa.aid = " + this.mainTag + "_a_tag.p_id and accepted = 1 and tag = '" + this.searchTag + "'";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next())
        {
            int aid = rs.getInt("aid");
            acceptedAnswers.add(aid);
        }
        rs.close();
        stmt.close();
        conn.close();
        System.out.println("Loading Answers done");
    }

    public void loadAllAcceptedAnswers() throws SQLException
    {

        allAcceptedAnswers = new HashSet<>();

        String url = "jdbc:mysql://localhost:3306/sof";
        String username = "root";
        String password = "root";

        System.out.println("Connecting database...");

        Connection conn = DriverManager.getConnection(url, username, password);
        System.out.println("Database connected!");
        Statement stmt = conn.createStatement();
        String query = "select distinct(aid) from " + this.mainTag + "_all_qa where accepted = 1";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next())
        {
            int aid = rs.getInt("aid");
            allAcceptedAnswers.add(aid);
        }
        rs.close();
        stmt.close();
        conn.close();
        System.out.println("Loading All Answers done");
    }

    public void calculateWord(String[] bodyTerms) throws IOException, ParseException
    {
        calculateWord(bodyTerms, 1000000);
    }

    public void calculateWord(String[] bodyTerms, int N) throws IOException, ParseException
    {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(new Searcher().getPostIndexPath())));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        HashSet<Integer> found = new HashSet<>();
        HashSet<Integer> self = new HashSet<>();

        System.out.println("Calculating word itself: " + searchTag);
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(new QueryParser("Body", analyzer).parse(searchTag), BooleanClause.Occur.MUST);
        booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);

        TopDocs results;
        results = searcher.search(booleanQuery.build(), N);

        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");

        int start = 0;
        int end = Math.min(numTotalHits, N);

        int count = 0;
        int skip = 0;

        for (int i = start; i < end; i++)
        {
            Document doc = searcher.doc(hits[i].doc);
            if (doc.get("SId") == null)
            {
                skip++;
                continue;
            }

            int id = Integer.parseInt(doc.get("SId"));
            if (this.acceptedAnswers.contains(id))
            {
                self.add(id);
                count++;
            }
        }

        System.out.println("Total Post Cnt = " + count + "/" + this.acceptedAnswers.size());
        System.out.println("Total skipped Post = " + skip);

        for (String bodyTerm : bodyTerms)
        {
            System.out.println("Query for: " + bodyTerm);
            booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(new QueryParser("Body", analyzer).parse(bodyTerm), BooleanClause.Occur.MUST);
//        booleanQuery.add(new QueryParser("Tags", analyzer).parse(this.searchTag), BooleanClause.Occur.MUST);
            booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);

            results = searcher.search(booleanQuery.build(), N);

            hits = results.scoreDocs;

            numTotalHits = results.totalHits;
            System.out.println(numTotalHits + " total matching documents");

            start = 0;
            end = Math.min(numTotalHits, N);

            count = 0;
            skip = 0;

            for (int i = start; i < end; i++)
            {
                Document doc = searcher.doc(hits[i].doc);
                if (doc.get("SId") == null)
                {
                    skip++;
                    continue;
                }

                int id = Integer.parseInt(doc.get("SId"));
                if (this.acceptedAnswers.contains(id))
                {
                    found.add(id);
                    count++;
                }
            }
            System.out.println("Total Post Cnt = " + count + "/" + this.acceptedAnswers.size());
            System.out.println("Total skipped Post = " + skip);
            System.out.println("-----------------");
        }
        System.out.println("Self Count = " + self.size() + "/" + this.acceptedAnswers.size());
        System.out.println("Final Count = " + found.size() + "/" + this.acceptedAnswers.size());

        HashSet<Integer> intersect = new HashSet<>();
        intersect.addAll(self);
        intersect.retainAll(found);
        HashSet<Integer> q_only = new HashSet<>();
        q_only.addAll(self);
        q_only.removeAll(found);
        System.out.println("Retrieved by normal query only," + q_only.size());
        HashSet<Integer> tr_only = new HashSet<>();
        tr_only.addAll(found);
        tr_only.removeAll(self);
        System.out.println("Retrieved by translations only," + tr_only.size());
        System.out.println("Retrieved by both methods," + intersect.size());
        HashSet<Integer> diff = new HashSet<>();
        diff.addAll(acceptedAnswers);
        diff.removeAll(self);
        diff.removeAll(found);
        System.out.println("Retrieved by no method," + diff.size());
    }

    public void calculateCount(String[] bodyTerms) throws IOException, ParseException
    {
        calculateCount(bodyTerms, 1000000);
    }

    public void calculateCount(String[] bodyTerms, int N) throws IOException, ParseException
    {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(new Searcher().getPostIndexPath())));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        HashSet<Integer> found = new HashSet<>();
        HashSet<Integer> self = new HashSet<>();

        System.out.println("Calculating word itself: " + searchTag);
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(new QueryParser("Body", analyzer).parse(searchTag), BooleanClause.Occur.MUST);
        booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);

        TopDocs results;
        results = searcher.search(booleanQuery.build(), N);

        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");

        int start = 0;
        int end = Math.min(numTotalHits, N);

        int count = 0;
        int skip = 0;

        for (int i = start; i < end; i++)
        {
            Document doc = searcher.doc(hits[i].doc);
            if (doc.get("SId") == null)
            {
                skip++;
                continue;
            }

            int id = Integer.parseInt(doc.get("SId"));
            if (this.acceptedAnswers.contains(id))
            {
                self.add(id);
                count++;
            }
        }

        System.out.println("Total Post Cnt = " + count + "/" + this.acceptedAnswers.size());
        System.out.println("Total skipped Post = " + skip);

        int[] counts = new int[bodyTerms.length];
        int[] accum_counts = new int[bodyTerms.length];
        int cnt = 0;
        for (String bodyTerm : bodyTerms)
        {
            HashSet<Integer> temp = new HashSet<>();
            System.out.println("Query for: " + bodyTerm);
            booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(new QueryParser("Body", analyzer).parse(bodyTerm), BooleanClause.Occur.MUST);
            booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);

            results = searcher.search(booleanQuery.build(), N);

            hits = results.scoreDocs;

            numTotalHits = results.totalHits;
            System.out.println(numTotalHits + " total matching documents");

            start = 0;
            end = Math.min(numTotalHits, N);

            count = 0;
            skip = 0;

            for (int i = start; i < end; i++)
            {
                Document doc = searcher.doc(hits[i].doc);
                if (doc.get("SId") == null)
                {
                    skip++;
                    continue;
                }

                int id = Integer.parseInt(doc.get("SId"));
                if (this.acceptedAnswers.contains(id))
                {
                    temp.add(id);
                }
            }
            HashSet<Integer> temp2 = new HashSet<>();
            temp2.addAll(temp);
            temp.removeAll(found);
            temp.removeAll(self);
            found.addAll(temp2);
            counts[cnt] = temp.size();
            accum_counts[cnt] = cnt == 0 ? temp.size() : accum_counts[cnt - 1] + temp.size();
            cnt++;
            System.out.println("Total Post Cnt = " + count + "/" + this.acceptedAnswers.size());
            System.out.println("Total skipped Post = " + skip);
            System.out.println("-----------------");
        }
        System.out.println("-----Final Count-----");
        System.out.println("Self," + ((double) self.size() / acceptedAnswers.size()) * 100);
        for (int i = 0; i < cnt; i++)
        {
            System.out.println("Tr" + (i + 1) + "," + ((double) counts[i] / acceptedAnswers.size()) * 100);
        }
        System.out.println("-----Final Accum Count-----");
//        System.out.println("Self,"+((double)self.size()/acceptedAnswers.size())*100);
//        for (int i = 0; i < cnt; i++)
//        {
//            System.out.println("Tr"+(i+1)+","+((double)accum_counts[i]/acceptedAnswers.size())*100);
//        }
        System.out.println("Cnt,Method,Value");
        for (int i = 0; i < cnt; i++)
        {
            System.out.println((i + 1) + "," + "Translation" + "," + ((double) accum_counts[i] / acceptedAnswers.size()) * 100);
            System.out.println((i + 1) + "," + "self" + "," + ((double) self.size() / acceptedAnswers.size()) * 100);
        }

    }

    public void calculatePR(String[] bodyTerms) throws IOException, ParseException
    {
        calculatePR(bodyTerms, 1000000);
    }

    public void calculatePR(String[] bodyTerms, int N) throws IOException, ParseException
    {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(new Searcher().getPostIndexPath())));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        HashSet<Integer> found = new HashSet<>();
        HashSet<Integer> total = new HashSet<>();

        System.out.println("Calculating word itself: " + searchTag);
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(new QueryParser("Body", analyzer).parse(searchTag), BooleanClause.Occur.MUST);
        booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);

        TopDocs results;
        results = searcher.search(booleanQuery.build(), N);

        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");

        int start = 0;
        int end = Math.min(numTotalHits, N);

        int count_r = 0;
        int count_n = 0;
        int skip = 0;

        for (int i = start; i < end; i++)
        {
            Document doc = searcher.doc(hits[i].doc);
            if (doc.get("SId") == null)
            {
                skip++;
                continue;
            }

            int id = Integer.parseInt(doc.get("SId"));

            if (!hasTag(id, mainTag))
            {
                continue;
            }

            if (this.acceptedAnswers.contains(id))
            {
                found.add(id);
                count_r++;
            }
            else
            {
                count_n++;
            }
            total.add(id);
        }

        System.out.println("Total Post Cnt = " + count_r + "/" + this.acceptedAnswers.size());
        System.out.println("Total skipped Post = " + skip);

        double[] P = new double[bodyTerms.length + 1];
        double[] R = new double[bodyTerms.length + 1];
        int cnt = 0;
        P[cnt] = (double) (count_r) / (count_r + count_n);
        R[cnt] = (double) count_r / (acceptedAnswers.size());
        cnt++;

        for (String bodyTerm : bodyTerms)
        {
            HashSet<Integer> temp = new HashSet<>();
            System.out.println("Query for: " + bodyTerm);
            booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(new QueryParser("Body", analyzer).parse(bodyTerm), BooleanClause.Occur.MUST);
            booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);

            results = searcher.search(booleanQuery.build(), N);

            hits = results.scoreDocs;

            numTotalHits = results.totalHits;
            System.out.println(numTotalHits + " total matching documents");

            start = 0;
            end = Math.min(numTotalHits, N);

            count_r = 0;
            count_n = 0;
            skip = 0;

            for (int i = start; i < end; i++)
            {
                Document doc = searcher.doc(hits[i].doc);
                if (doc.get("SId") == null)
                {
                    skip++;
                    continue;
                }

                int id = Integer.parseInt(doc.get("SId"));

                if (!hasTag(id, searchTag))
                {
                    skip++;
                    continue;
                }

                if (this.acceptedAnswers.contains(id))
                {
                    found.add(id);
                    count_r++;
                }
                else
                {
                    count_n++;
                }
                total.add(id);
            }
            P[cnt] = (double) found.size() / total.size();
            R[cnt] = (double) found.size() / (acceptedAnswers.size());
            cnt++;
            System.out.println("Total Post Cnt = " + count_r + "/"+count_n+"/" + this.acceptedAnswers.size());
            System.out.println("Total skipped Post = " + skip);
            System.out.println("-----------------");
        }
//        System.out.println("-----Final Count-----");
//        System.out.println("Self,"+((double)self.size()/acceptedAnswers.size())*100);
//        for (int i = 0; i < cnt; i++)
//        {
//            System.out.println("Tr"+(i+1)+","+((double)counts[i]/acceptedAnswers.size())*100);
//        }
        System.out.println("-----Final Accum Count-----");
//        System.out.println("Self,"+((double)self.size()/acceptedAnswers.size())*100);
//        for (int i = 0; i < cnt; i++)
//        {
//            System.out.println("Tr"+(i+1)+","+((double)accum_counts[i]/acceptedAnswers.size())*100);
//        }
        System.out.println("Cnt,Method,Value");
        for (int i = 0; i < cnt; i++)
        {
            System.out.println((i) + "," + "Precision" + "," + P[i] * 100);
            System.out.println((i) + "," + "Recall" + "," + R[i] * 100);
        }

    }
    
    
    public void calculateVenn(String[] bodyTerms) throws IOException, ParseException
    {
        calculateVenn(bodyTerms, 1000000);
    }

    public void calculateVenn(String[] bodyTerms, int N) throws IOException, ParseException
    {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(new Searcher().getPostIndexPath())));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        ArrayList<HashSet<Integer>> sets = new ArrayList<>();
//        HashSet<?>[] sets = new HashSet<?>[bodyTerms.length + 1];

        System.out.println("Calculating word itself: " + searchTag);
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(new QueryParser("Body", analyzer).parse(searchTag), BooleanClause.Occur.MUST);
        booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);

        TopDocs results;
        results = searcher.search(booleanQuery.build(), N);

        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");

        int start = 0;
        int end = Math.min(numTotalHits, N);

        int count = 0;
        int skip = 0;
        
        sets.add(0,acceptedAnswers);
        
        HashSet<Integer> temp = new HashSet<Integer>();
        sets.add(1, new HashSet<>());

        for (int i = start; i < end; i++)
        {
            Document doc = searcher.doc(hits[i].doc);
            if (doc.get("SId") == null)
            {
                skip++;
                continue;
            }

            int id = Integer.parseInt(doc.get("SId"));
            if (this.acceptedAnswers.contains(id))
            {
                sets.get(1).add(id);
            }
        }
        

        System.out.println("Total Post Cnt = " + count + "/" + this.acceptedAnswers.size());
        System.out.println("Total skipped Post = " + skip);

        int[] counts = new int[bodyTerms.length];
        int[] accum_counts = new int[bodyTerms.length];
        int cnt = 0;
        int arrayIndex = 2;
        for (String bodyTerm : bodyTerms)
        {
            sets.add(arrayIndex, new HashSet<>());
            System.out.println("Query for: " + bodyTerm);
            booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(new QueryParser("Body", analyzer).parse(bodyTerm), BooleanClause.Occur.MUST);
            booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);

            results = searcher.search(booleanQuery.build(), N);

            hits = results.scoreDocs;

            numTotalHits = results.totalHits;
            System.out.println(numTotalHits + " total matching documents");

            start = 0;
            end = Math.min(numTotalHits, N);

            count = 0;
            skip = 0;

            for (int i = start; i < end; i++)
            {
                Document doc = searcher.doc(hits[i].doc);
                if (doc.get("SId") == null)
                {
                    skip++;
                    continue;
                }

                int id = Integer.parseInt(doc.get("SId"));
                if (this.acceptedAnswers.contains(id))
                {
                    sets.get(arrayIndex).add(id);
                }
            }
            arrayIndex++;
            counts[cnt] = temp.size();
            accum_counts[cnt] = cnt == 0 ? temp.size() : accum_counts[cnt - 1] + temp.size();
            cnt++;
//            System.out.println("Total Post Cnt = " + count + "/" + this.acceptedAnswers.size());
//            System.out.println("Total skipped Post = " + skip);
            System.out.println("-----------------");
        }
        
        System.out.println("-------------------\nFinal Res\n-------------\n");
        int pow = 1;
        for (int i = 0; i < bodyTerms.length+1; i++)
            pow *= 2;

        HashSet<Integer> temp2 = new HashSet<>();
        for (HashSet<Integer> hs : sets)
        {
            temp2.addAll(hs);
        }
        int size = temp2.size();
        for (int i = 1; i <= pow - 1; i++)
        {
            ArrayList<Integer> numbers = new ArrayList<>();
//                int rem = 2;
            int dig = 2;
            int n =  i;
            while(n != 0)
            {
                if(n % 2 == 1)
                {
                    numbers.add(dig);
                }     
                n /= 2;
                dig++;
            }
//            System.out.println(numbers);
            temp = new HashSet<>();
            temp.addAll(sets.get(numbers.get(0)-2));
            for (Integer number : numbers)
            {
                temp.retainAll(sets.get(number-2)); //-1 to include self translation and accepted
            }
            String s = "";
            if(numbers.size() == 1)
                s = "area";
            else
                s="n";
            for (Integer number : numbers)
            {
                s = s+ (number-1);
            }
//            s += "="+((double)temp.size() / acceptedAnswers.size())+",";
            s += "="+(temp.size())+",";
            System.out.println(s);
        }
        String s = "category = c(\"All\",\""+this.searchTag+"\",";
        for (String t : bodyTerms)
        {
            s = s+"\"" + t + "\",";
        }
        s += "),";
        System.out.println(s);
        
//        System.out.println("-----Final Count-----");
//        System.out.println("Self," + ((double) self.size() / acceptedAnswers.size()) * 100);
//        for (int i = 0; i < cnt; i++)
//        {
//            System.out.println("Tr" + (i + 1) + "," + ((double) counts[i] / acceptedAnswers.size()) * 100);
//        }
//        System.out.println("-----Final Accum Count-----");
//        System.out.println("Self,"+((double)self.size()/acceptedAnswers.size())*100);
//        for (int i = 0; i < cnt; i++)
//        {
//            System.out.println("Tr"+(i+1)+","+((double)accum_counts[i]/acceptedAnswers.size())*100);
//        }
//        System.out.println("Cnt,Method,Value");
//        for (int i = 0; i < cnt; i++)
//        {
//            System.out.println((i + 1) + "," + "Translation" + "," + ((double) accum_counts[i] / acceptedAnswers.size()) * 100);
//            System.out.println((i + 1) + "," + "self" + "," + ((double) self.size() / acceptedAnswers.size()) * 100);
//        }

    }
}
