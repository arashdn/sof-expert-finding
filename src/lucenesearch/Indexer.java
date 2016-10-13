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
import java.util.stream.Stream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LegacyLongField;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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
class Post
{

    private long id;
    private int postTypeId;
    private long parentId;
    private Date creationDate;
    private int score;
    private int viewCount;
    private String body;
    private long ownerUserId;
    private Date lastActivityDate;
    private String title;
    private String tags;
    private int answerCount;
    private int commentCount;

    // <editor-fold desc="Setters and getters" defaultstate="collapsed">
    public int getCommentCount()
    {
        return commentCount;
    }

    public void setCommentCount(int commentCount)
    {
        this.commentCount = commentCount;
    }

    public int getAnswerCount()
    {
        return answerCount;
    }

    public void setAnswerCount(int answerCount)
    {
        this.answerCount = answerCount;
    }

    public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Date getLastActivityDate()
    {
        return lastActivityDate;
    }

    public void setLastActivityDate(Date lastActivityDate)
    {
        this.lastActivityDate = lastActivityDate;
    }

    public void setLastActivityDate(String lastActivityDate) throws ParseException
    {
        String d = lastActivityDate.replaceAll("T", " ");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        this.setLastActivityDate(dateFormat.parse(d));
    }

    public long getOwnerUserId()
    {
        return ownerUserId;
    }

    public void setOwnerUserId(long ownerUserId)
    {
        this.ownerUserId = ownerUserId;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public int getViewCount()
    {
        return viewCount;
    }

    public void setViewCount(int viewCount)
    {
        this.viewCount = viewCount;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date CreationDate)
    {
        this.creationDate = CreationDate;
    }

    public void setCreationDate(String CreationDate) throws ParseException
    {
        String d = CreationDate.replaceAll("T", " ");
        final String NEW_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        this.setCreationDate(dateFormat.parse(d));
    }

    public long getParentId()
    {
        return parentId;
    }

    public void setParentId(long parentId)
    {
        this.parentId = parentId;
    }

    public int getPostTypeId()
    {
        return postTypeId;
    }

    public void setPostTypeId(int PostTypeId)
    {
        this.postTypeId = PostTypeId;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long Id)
    {
        this.id = Id;
    }
// </editor-fold>

    public Post(NamedNodeMap nnm) throws ParseException
    {
        if (nnm.getNamedItem("Id") != null)
        {
            this.setId(Long.parseLong(nnm.getNamedItem("Id").getFirstChild().getTextContent()));
        }
        if (nnm.getNamedItem("PostTypeId") != null)
        {
            this.setPostTypeId(Integer.parseInt(nnm.getNamedItem("PostTypeId").getFirstChild().getTextContent()));
        }
        if (nnm.getNamedItem("ParentId") != null)
        {
            this.setParentId(Long.parseLong(nnm.getNamedItem("ParentId").getFirstChild().getTextContent()));
        }
        if (nnm.getNamedItem("CreationDate") != null)
        {
            this.setCreationDate(nnm.getNamedItem("CreationDate").getFirstChild().getTextContent());
        }
        if (nnm.getNamedItem("Score") != null)
        {
            this.setScore(Integer.parseInt(nnm.getNamedItem("Score").getFirstChild().getTextContent()));
        }
        if (nnm.getNamedItem("ViewCount") != null)
        {
            this.setViewCount(Integer.parseInt(nnm.getNamedItem("ViewCount").getFirstChild().getTextContent()));
        }
        if (nnm.getNamedItem("Body") != null)
        {
            this.setBody(nnm.getNamedItem("Body").getFirstChild().getTextContent());
        }
        if (nnm.getNamedItem("OwnerUserId") != null)
        {
            this.setOwnerUserId(Long.parseLong(nnm.getNamedItem("OwnerUserId").getFirstChild().getTextContent()));
        }
        if (nnm.getNamedItem("LastActivityDate") != null)
        {
            this.setLastActivityDate(nnm.getNamedItem("LastActivityDate").getFirstChild().getTextContent());
        }
        if (nnm.getNamedItem("Title") != null)
        {
            this.setTitle(nnm.getNamedItem("Title").getFirstChild().getTextContent());
        }
        if (nnm.getNamedItem("Tags") != null)
        {
            this.setTags(nnm.getNamedItem("Tags").getFirstChild().getTextContent());
        }
        if (nnm.getNamedItem("AnswerCount") != null)
        {
            this.setAnswerCount(Integer.parseInt(nnm.getNamedItem("AnswerCount").getFirstChild().getTextContent()));
        }
        if (nnm.getNamedItem("CommentCount") != null)
        {
            this.setCommentCount(Integer.parseInt(nnm.getNamedItem("CommentCount").getFirstChild().getTextContent()));
        }

    }
}

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
        //writer.close();


        //Read File Line By Line
        long i = 1;
        while ((strLine = br.readLine()) != null)
        {
            if(strLine.contains("</posts>"))
            {
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
            }

        }

        //Close the input stream
        br.close();
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    }

    public void indexPost(IndexWriter writer, Post p) throws IOException
    {
        // make a new, empty document
        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();

        doc.add(new LongPoint("Id", p.getId()));
        doc.add(new IntPoint("PostTypeId", p.getPostTypeId()));
        doc.add(new LongPoint("ParentId", p.getParentId()));
        doc.add(new LongPoint("CreationDate", p.getCreationDate().getTime()));
        doc.add(new IntPoint("Score", p.getScore()));
        doc.add(new IntPoint("ViewCount", p.getViewCount()));
        
        if(p.getBody() != null)
            doc.add(new TextField("Body", p.getBody(), Field.Store.YES));
        
        doc.add(new LongPoint("OwnerUserId", p.getOwnerUserId()));
        doc.add(new LongPoint("LastActivityDate", p.getLastActivityDate().getTime()));
        
        if(p.getTitle()!= null)
            doc.add(new TextField("Title", p.getTitle(), Field.Store.YES));
        
        if(p.getTags() != null)
            doc.add(new TextField("Tags", p.getTags(), Field.Store.YES));
        
        doc.add(new IntPoint("AnswerCount", p.getAnswerCount()));
        doc.add(new IntPoint("CommentCount", p.getCommentCount()));



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
}
