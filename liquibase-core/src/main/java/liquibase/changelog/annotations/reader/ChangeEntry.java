package liquibase.changelog.annotations.reader;

import java.util.Date;
import java.util.Objects;


public class ChangeEntry {

  private String _id;
  private String changeId;
  private String author;
  private Date timestamp;
  private String comments;
  private String changeLogClass;
  private String changeSetMethodName;
  
  public ChangeEntry(String changeId, String author, Date timestamp, String comments,
                     String changeLogClass, String changeSetMethodName) {

    this.changeId = changeId;
    this._id = "ChangeEntry::"+changeId;
    this.author = author;
    this.timestamp = new Date(timestamp.getTime());
    this.comments = comments;
    this.changeLogClass = changeLogClass;
    this.changeSetMethodName = changeSetMethodName;
  }

  public ChangeEntry(String changeId) {
    this.changeId = changeId;
    this._id = "ChangeEntry::"+changeId;
  }

  @Override
  public String toString() {
    return "[ChangeSet: id=" + this.changeId +
        ", author=" + this.author +
        ", changeLogClass=" + this.changeLogClass +
        ", changeSetMethod=" + this.changeSetMethodName + "]";
  }

  public String _getId() {
    return this._id;
  }

  public String getChangeId() {
    return this.changeId;
  }

  public String getAuthor() {
    return this.author;
  }

  public Date getTimestamp() {
    return this.timestamp;
  }

  public String getChangeLogClass() {
    return this.changeLogClass;
  }

  public String getChangeSetMethodName() {
    return this.changeSetMethodName;
  }

  public String getComments(){return this.comments;}


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ChangeEntry that = (ChangeEntry) o;
    return Objects.equals(_id, that._id) &&
        Objects.equals(changeId, that.changeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id, changeId);
  }
}
