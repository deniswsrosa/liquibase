package liquibase.changelog.annotations.reader;


import liquibase.parser.core.java.ChangeLog;
import liquibase.parser.core.java.ChangeSet;

import java.io.Serializable;
import java.util.Comparator;

import static org.springframework.util.StringUtils.hasText;

/**
 * Sort ChangeSets by 'order'
 *
 */
public class ChangeSetComparator implements Comparator<ChangeSet>, Serializable {
  @Override
  public int compare(ChangeSet o1, ChangeSet o2) {

    Integer val1 = o1.order();
    Integer val2 = o2.order();

    return val1.compareTo(val2);
  }
}
