package lucenesearch;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import lucenesearch.LuceneTools.LuceneUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LegacyIntField;
import org.apache.lucene.document.LegacyLongField;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * @author arashdn
 */

public class Indexer
{
    
    public void indexPosts(String path) throws FileNotFoundException, IOException, SAXException, ParseException
    {
        FileInputStream fstream = new FileInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        //skip first two lines which contains xml definations
        br.readLine();
        br.readLine();

        String strLine;

        boolean create = true;
        Date start = new Date();
        Directory dir = FSDirectory.open(Paths.get("./data/index"));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        if (create)
        {
            // Create a new index in the directory, removing any
            // previously indexed documents:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        }
        else
        {
            // Add new documents to an existing index:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }

        // Optional: for better indexing performance, if you
        // are indexing many documents, increase the RAM
        // buffer.  But if you do this, increase the max heap
        // size to the JVM (eg add -Xmx512m or -Xmx1g):
        //
        iwc.setRAMBufferSizeMB(1024.0);
        IndexWriter writer = new IndexWriter(dir, iwc);


        //Read File Line By Line
        long i = 1;
        while ((strLine = br.readLine()) != null)
        {
            if(strLine.contains("</posts>"))
            {
                writer.close();
                System.out.println("Completed on: "+i);
            }
            else
            {
                InputSource is = new InputSource(new StringReader(strLine));
                DOMParser dp = new DOMParser();
                dp.parse(is);
                Document doc = dp.getDocument();
                NodeList nl = doc.getElementsByTagName("row");
                Node n = nl.item(0);
                NamedNodeMap nnm = n.getAttributes();
                Post p = new Post(nnm);
                indexPost(writer, p);
                System.out.println("Indexing row " + (i++));
//                if(i>1000000)
//                    break;
            }

        }

        //Close the input stream
        br.close();
        if(writer.isOpen())
            writer.close();
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    }

    public void indexPost(IndexWriter writer, Post p) throws IOException
    {
        // make a new, empty document
        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
        
       //https://gist.github.com/mocobeta/0d2feeb59295bfad157ed06e36fd626ahttps://gist.github.com/mocobeta/0d2feeb59295bfad157ed06e36fd626a
//        FieldType summaryType = new FieldType();
//        summaryType.setIndexOptions(IndexOptions.DOCS);
//        summaryType.setStored(true);
//        summaryType.setTokenized(true);
//        summaryType.setStoreTermVectors(true);
//        summaryType.setStoreTermVectorPositions(true);
//        summaryType.setStoreTermVectorOffsets(true);
//        summaryType.setStoreTermVectorPayloads(true);
       
        doc.add(new IntPoint("Id", p.getId()));
        doc.add(new StoredField("SId",p.getId()));
        
        doc.add(new IntPoint("PostTypeId", p.getPostTypeId()));
        doc.add(new StoredField("SPostTypeId",p.getPostTypeId()));
        
        if(p.getParentId() != 0)
        {
            doc.add(new IntPoint("ParentId", p.getParentId()));
            doc.add(new StoredField("SParentId",p.getParentId()));
        }
        
        if(p.getAcceptedAnswerId()!= 0)
        {
            doc.add(new IntPoint("AcceptedAnswerId", p.getAcceptedAnswerId()));
            doc.add(new StoredField("SAcceptedAnswerId",p.getAcceptedAnswerId()));
        }
        
        doc.add(new LongPoint("CreationDate", p.getCreationDate().getTime()));
        doc.add(new StoredField("SCreationDate",p.getCreationDate().getTime()));

        doc.add(new IntPoint("Score", p.getScore()));
        doc.add(new StoredField("SScore",p.getScore()));
        doc.add(new SortedNumericDocValuesField("SortScore",p.getScore()));
        
        
        doc.add(new IntPoint("ViewCount", p.getViewCount()));
        doc.add(new StoredField("SViewCount",p.getViewCount()));
        doc.add(new SortedNumericDocValuesField("SortViewCount",p.getViewCount()));

        
        
        if(p.getBody() != null)
        {
            doc.add(new Field("Body", p.getBody(), LuceneUtils.getVectorField()));
            doc.add(new Field("Code", p.getCode(), LuceneUtils.getVectorField()));
        }
                
        
        if(p.getOwnerUserId()!= 0)
        {
            doc.add(new IntPoint("OwnerUserId", p.getOwnerUserId()));
            doc.add(new StoredField("SOwnerUserId",p.getOwnerUserId()));
        }
        
        if(p.getLastEditorUserId()!= 0)
        {
            doc.add(new IntPoint("LastEditorUserId", p.getLastEditorUserId()));
            doc.add(new StoredField("SLastEditorUserId",p.getLastEditorUserId()));
        }
        
        if(p.getLastEditorDisplayName() != null)
            doc.add(new TextField("LastEditorDisplayName", p.getLastEditorDisplayName(), Field.Store.YES));
        
        if(p.getLastEditDate() != null)
        {
            doc.add(new LongPoint("LastEditDate", p.getLastEditDate().getTime()));
            doc.add(new StoredField("SLastEditDate",p.getLastEditDate().getTime()));
        }
        
       if(p.getLastActivityDate()!= null)
       {
            doc.add(new LongPoint("LastActivityDate", p.getLastActivityDate().getTime()));
            doc.add(new StoredField("SLastActivityDate",p.getLastActivityDate().getTime()));
       }
        
        if(p.getTitle()!= null)
            doc.add(new TextField("Title", p.getTitle(), Field.Store.YES));
        
        if(p.getTags() != null)
        {
            for (String tag : p.getTags())
            {
                doc.add(new StringField("Tags", tag, Field.Store.YES));
            }
        }
        
        doc.add(new IntPoint("AnswerCount", p.getAnswerCount()));
        doc.add(new StoredField("SAnswerCount",p.getAnswerCount()));

        doc.add(new IntPoint("CommentCount", p.getCommentCount()));
        doc.add(new StoredField("SCommentCount",p.getCommentCount()));
        doc.add(new SortedNumericDocValuesField("SortCommentCount",p.getCommentCount()));

        doc.add(new IntPoint("FavoriteCount", p.getFavoriteCount()));
        doc.add(new StoredField("SFavoriteCount",p.getFavoriteCount()));
        doc.add(new SortedNumericDocValuesField("SortFavoriteCount",p.getFavoriteCount()));

        if(p.getCommunityOwnedDate()!= null)
        {
           doc.add(new LongPoint("CommunityOwnedDate", p.getCommunityOwnedDate().getTime()));
           doc.add(new StoredField("SCommunityOwnedDate",p.getCommunityOwnedDate().getTime()));
        }


        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE)
        {
            // New index, so we just add the document (no old document can be there):
            //System.out.println("adding " + p.getId());
            writer.addDocument(doc);
        }
        else
        {
            // Existing index (an old copy of this document may have been indexed) so 
            // we use updateDocument instead to replace the old one matching the exact 
            // path, if present:
//           System.out.println("updating " + p.getId());
//           writer.updateDocument(new Term("path", file.toString()), doc);
            throw new NotImplementedException();
        }
    }
    
    public void indexJavaPosts(String path) throws FileNotFoundException, IOException, SAXException, ParseException
    {
        indexJavaPosts(path, null);
    }
    public void indexJavaPosts(String path, Double p) throws FileNotFoundException, IOException, SAXException, ParseException
    {
        int numberOfskip = 0;
        Random rand = new Random();
        FileInputStream fstream = new FileInputStream("data/Q_A.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        
        String line;
        int ii = 0;
        Set<Integer> set = new TreeSet<>();
        
        line = br.readLine();//skip first line
        while ((line = br.readLine()) != null)
        {
            String[] split = line.split(",");
            if(split.length == 1)
                continue;
            int q = Integer.parseInt(split[0].trim());
            int a = Integer.parseInt(split[1].trim());
            set.add(a);
            set.add(q);
           
            //System.out.println((ii++)+"");
        }
        System.out.println("Len: "+set.size());
        
        
        fstream = new FileInputStream(path);
        br = new BufferedReader(new InputStreamReader(fstream));

        //skip first two lines which contains xml definations
        br.readLine();
        br.readLine();

        String strLine;

        boolean create = true;
        Date start = new Date();
        Directory dir = FSDirectory.open(Paths.get("./data/index_java"));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        if (create)
        {
            // Create a new index in the directory, removing any
            // previously indexed documents:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        }
        else
        {
            // Add new documents to an existing index:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }

        // Optional: for better indexing performance, if you
        // are indexing many documents, increase the RAM
        // buffer.  But if you do this, increase the max heap
        // size to the JVM (eg add -Xmx512m or -Xmx1g):
        //
        iwc.setRAMBufferSizeMB(1024.0);
        IndexWriter writer = new IndexWriter(dir, iwc);


        //Read File Line By Line
        long i = 1;
        int indexed = 0;
        while ((strLine = br.readLine()) != null)
        {
            if(strLine.contains("</posts>"))
            {
                writer.close();
                System.out.println("Completed on: "+i);
            }
            else
            {
                InputSource is = new InputSource(new StringReader(strLine));
                DOMParser dp = new DOMParser();
                dp.parse(is);
                Document doc = dp.getDocument();
                NodeList nl = doc.getElementsByTagName("row");
                Node n = nl.item(0);
                NamedNodeMap nnm = n.getAttributes();
                if (nnm.getNamedItem("Id") != null && set.contains(Integer.parseInt(nnm.getNamedItem("Id").getFirstChild().getTextContent())))
                {
                    if(p != null)
                    {
                        if(rand.nextDouble() > p)//p is indexing prob here rans is jump prob so < will be >
                        {
                            numberOfskip++;
                            System.err.println("num skip: "+numberOfskip);
                            continue;
                        }
                    }
                    Post post = new Post(nnm);
                    indexPost(writer, post);
                    System.out.println("row " + i+" indexed "+(++indexed));
                }
                i++;
//                if(i>1000000)
//                    break;
            }

        }

        //Close the input stream
        br.close();
        if(writer.isOpen())
            writer.close();
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    }


    public void indexTagPosts(String path, String tag) throws FileNotFoundException, IOException, SAXException, ParseException
    {
        indexTagPosts(path,tag, null);
    }
    public void indexTagPosts(String path, String tag, Double p) throws FileNotFoundException, IOException, SAXException, ParseException
    {
        int numberOfskip = 0;
        Random rand = new Random();
        FileInputStream fstream = new FileInputStream("data/"+tag+"_Q_A.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        
        String line;
        int ii = 0;
        Set<Integer> set = new TreeSet<>();
        
        line = br.readLine();//skip first line
        while ((line = br.readLine()) != null)
        {
            String[] split = line.split(",");
            if(split.length == 1)
                continue;
            int q = Integer.parseInt(split[0].trim());
            int a = Integer.parseInt(split[1].trim());
            set.add(a);
            set.add(q);
           
            //System.out.println((ii++)+"");
        }
        System.out.println("Len: "+set.size());
        
        
        fstream = new FileInputStream(path);
        br = new BufferedReader(new InputStreamReader(fstream));

        //skip first two lines which contains xml definations
        br.readLine();
        br.readLine();

        String strLine;

        boolean create = true;
        Date start = new Date();
        Directory dir = FSDirectory.open(Paths.get("./data/index_"+tag));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        if (create)
        {
            // Create a new index in the directory, removing any
            // previously indexed documents:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        }
        else
        {
            // Add new documents to an existing index:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }

        // Optional: for better indexing performance, if you
        // are indexing many documents, increase the RAM
        // buffer.  But if you do this, increase the max heap
        // size to the JVM (eg add -Xmx512m or -Xmx1g):
        //
        iwc.setRAMBufferSizeMB(1024.0);
        IndexWriter writer = new IndexWriter(dir, iwc);


        //Read File Line By Line
        long i = 1;
        int indexed = 0;
        while ((strLine = br.readLine()) != null)
        {
            if(strLine.contains("</posts>"))
            {
                writer.close();
                System.out.println("Completed on: "+i);
            }
            else
            {
                InputSource is = new InputSource(new StringReader(strLine));
                DOMParser dp = new DOMParser();
                dp.parse(is);
                Document doc = dp.getDocument();
                NodeList nl = doc.getElementsByTagName("row");
                Node n = nl.item(0);
                NamedNodeMap nnm = n.getAttributes();
                if (nnm.getNamedItem("Id") != null && set.contains(Integer.parseInt(nnm.getNamedItem("Id").getFirstChild().getTextContent())))
                {
                    if(p != null)
                    {
                        if(rand.nextDouble() > p)//p is indexing prob here rans is jump prob so < will be >
                        {
                            numberOfskip++;
                            System.err.println("num skip: "+numberOfskip);
                            continue;
                        }
                    }
                    Post post = new Post(nnm);
                    indexPost(writer, post);
                    System.out.println("row " + i+" indexed "+(++indexed));
                }
                i++;
//                if(i>1000000)
//                    break;
            }

        }

        //Close the input stream
        br.close();
        if(writer.isOpen())
            writer.close();
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    }

}
