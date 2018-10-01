package liquibase.database;

import liquibase.changelog.annotations.reader.ChangeEntry;

public interface GenericDataStore<T> {


    void connect(String url, String user, String password);

    ChangeEntry save(ChangeEntry changeEntry);

    boolean isNewChange(ChangeEntry changeEntry);

    void close();

    T getInternalStore();

}
