package lucenesearch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author arashdn
 */
public class Post
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