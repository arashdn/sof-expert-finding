package lucenesearch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lucenesearch.LuceneTools.LuceneUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author arashdn
 */
// <editor-fold desc="WordProb class">    
class WordProb implements Comparable<WordProb>
{

    String word;
    double finalProb;

    public WordProb(String word, double finalProb)
    {
        this.word = word;
        this.finalProb = finalProb;
    }

    public String getWord()
    {
        return word;
    }

    public void setWord(String word)
    {
        this.word = word;
    }

    public double getFinalProb()
    {
        return finalProb;
    }

    public void setFinalProb(double finalProb)
    {
        this.finalProb = finalProb;
    }

    @Override
    public int compareTo(WordProb o)
    {
        return Double.compare(o.finalProb,finalProb);
    }

    @Override
    public String toString()
    {
        return "WordProb{" + "word=" + word + ", finalProb=" + finalProb + '}';
    }
    
    

}
// </editor-fold>    

// <editor-fold desc="Tag class">     
class Tag
{

    private String tagName;
    private HashMap<String, Double> words;

    public Tag(String tagName)
    {
        this.tagName = tagName;
        this.words = new HashMap<>();
    }

    public void addWord(String w, Double prob)
    {
        words.put(w, prob);
    }

    public double getWordProb(String Word)
    {
        return words.get(Word);
    }

    public String getTagName()
    {
        return tagName;
    }

    public void setTagName(String tagName)
    {
        this.tagName = tagName;
    }

    public HashMap<String, Double> getWords()
    {
        return words;
    }

    public void setWords(HashMap<String, Double> words)
    {
        this.words = words;
    }

}
// </editor-fold>

public class DeepTranslationFixer
{

    int numberOfTranslations = 10;
    private Tag[] tagWord;
    private ArrayList<String> words;
    private HashMap<String, ArrayList<Double>> wordPriors;
    private HashMap<Integer, HashMap<String, ArrayList<WordProb>>> finalProbs;
    private HashMap<String, Double> normalizedTermFreq = null;
    private HashMap<String, Double> normalizedTFIDF = null;
    private String index = new Searcher().getPostIndexPath();
    

    private void readFile() throws FileNotFoundException, IOException
    {
        BufferedReader br = new BufferedReader(new FileReader("./data/deep_result.txt"));
        String line = br.readLine();
        String[] tags = line.split("\t");

        tagWord = new Tag[tags.length - 1];
        words = new ArrayList<>();

        for (int i = 1; i < tags.length; i++)
        {
            tagWord[i - 1] = new Tag(tags[i]);
        }

        while ((line = br.readLine()) != null)
        {
            String[] temp = line.split("\t");
            String word = temp[0];
            words.add(word);
            for (int i = 1; i < temp.length; i++)
            {
                tagWord[i - 1].addWord(word, Double.parseDouble(temp[i]));
            }
        }

        br.close();

        double sum = 0;
        for (Tag tg : tagWord)
        {
            sum += tg.getWordProb("filewriter");
        }
        assert Math.abs(sum - 1) < 0.001;

        System.out.println("File Reading Done!");
    }

    private void fillProbs() throws IOException
    {
        wordPriors = new HashMap<>();
        ArrayList<Double> res = new ArrayList<>();
        readFile();
        for (String word : words)
        {
            res = new ArrayList<>();
            res.add(getNaiveProb(word));
            res.add(getTFProb(word));
            res.add(getTFIDFProb(word));
            wordPriors.put(word, res);
        }
    }

    private void getFinalProbs() throws IOException
    {
        fillProbs();
        finalProbs = new HashMap<>();

        for (Tag tag : tagWord)
        {
            HashMap<String, Double> p_w_tag = tag.getWords();
            for (String word : p_w_tag.keySet())
            {
                ArrayList<Double> values = wordPriors.get(word);
                for (int i = 0; i < values.size(); i++)
                {
                    WordProb wp = new WordProb(word, values.get(i) * p_w_tag.get(word));
                    HashMap<String, ArrayList<WordProb>> temp = finalProbs.get(i);
                    if (temp == null)
                    {
                        temp = new HashMap<>();
                        finalProbs.put(i, temp);
                    }
                    ArrayList<WordProb> temp2 = temp.get(tag.getTagName());
                    if (temp2 == null)
                    {
                        temp2 = new ArrayList<>();
                        temp.put(tag.getTagName(), temp2);
                    }
                    temp2.add(wp);
//                    System.out.println("Tag: "+tag.getTagName()+" , Array= "+temp2);
                }
            }
        }
    }

    public void saveResult() throws IOException
    {
        getFinalProbs();
        for (Integer methodNumber : finalProbs.keySet())
        {
            StringBuilder sb = new StringBuilder();

            HashMap<String, ArrayList<WordProb>> temp = finalProbs.get(methodNumber);
            for (String tag : temp.keySet())
            {
                sb.append(tag).append("~");
                ArrayList<WordProb> res = temp.get(tag);
                Collections.sort(res);
                for(int i = 0 ; i<numberOfTranslations; i++)
                {
                    sb.append(res.get(i).getWord()).append(",");
                }
                sb.setLength(sb.length() - 1);
                sb.append("\n");
            }
            PrintWriter pw = new PrintWriter("./data/final_deep_trans_method_"+methodNumber+".txt");
            pw.print(sb.toString());
            pw.close();
        }
        System.out.println("Done!");
    }

    private Double getNaiveProb(String word)
    {
        return 1.0;
    }
    private Double getTFProb(String word) throws IOException
    {
        if(normalizedTermFreq == null)
        {
            initNormalizedTermFreq();
        }
        double res =  normalizedTermFreq.get(word);
        if(res > 0)
            return res;
        throw new ArithmeticException("Zero TF");
    }
    
    private Double getTFIDFProb(String word) throws IOException
    {

        if(normalizedTFIDF == null)
        {
            initNormalizedTFIDF();
        }
        double res =  normalizedTFIDF.get(word);
        if(res > 0)
            return res;
        throw new ArithmeticException("Zero TF-IDF");
        
        
    }
    
    private void initNormalizedTFIDF() throws IOException
    {
        LuceneUtils lu = new LuceneUtils(DirectoryReader.open(FSDirectory.open(Paths.get(index))));
        normalizedTFIDF = new HashMap<>();
        for (String word : words)
        {
            double temp = lu.getTFIDFScoreInCollection("Body", word);
            normalizedTFIDF.put(word, temp);
        }
    }

    private void initNormalizedTermFreq() throws IOException
    {
        LuceneUtils lu = new LuceneUtils(DirectoryReader.open(FSDirectory.open(Paths.get(index))));
        normalizedTermFreq = new HashMap<>();
        double sum = 0;
        for (String word : words)
        {
            double temp = lu.getTermFrequencyInCollection("Body", word);
            sum += temp;
            normalizedTermFreq.put(word, temp);
        }
        //normalize:
        for (String word : normalizedTermFreq.keySet())
        {
            double temp = normalizedTermFreq.get(word)/sum;
            normalizedTermFreq.replace(word, temp);
        }
    }
}
