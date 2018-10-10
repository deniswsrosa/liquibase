package liquibase.parser.core.java;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogParser;
import liquibase.resource.ResourceAccessor;

import java.lang.reflect.Method;

public class JavaChangeLogParser implements ChangeLogParser {

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        try {
            Class changeLogClass = Class.forName(physicalChangeLogLocation.replace("/", "."));
            Object changeLogInstance = changeLogClass.newInstance();

            DatabaseChangeLog changelog = new DatabaseChangeLog(physicalChangeLogLocation);

            for (Method method : changeLogClass.getMethods()) {
                ChangeSet changeSetAnnotation = method.getAnnotation(ChangeSet.class);
                if (changeSetAnnotation != null && !changeSetAnnotation.ignore()) {
                    String id = changeSetAnnotation.id();
                    String author = changeSetAnnotation.author();
                    boolean runAlways = changeSetAnnotation.runAlways();

                    liquibase.changelog.ChangeSet changeSet = new liquibase.changelog.ChangeSet(id, author, runAlways, false, physicalChangeLogLocation, null, null, changelog);

                    if (method.getReturnType().equals(void.class)) {
                        changeSet.addChange(new JavaChange(method, changeLogInstance));
                    } else if (Change.class.isAssignableFrom(method.getReturnType())) {
                        changeSet.addChange((Change) method.invoke(changeLogInstance));
                    } else if (liquibase.changelog.ChangeSet.class.isAssignableFrom(method.getReturnType())) {
                        changeSet = (liquibase.changelog.ChangeSet) method.invoke(changeLogInstance);
                    } else {
                        throw new ChangeLogParseException("Unknown return type for " + method + ": " + method.getReturnType().getName());
                    }

                    changelog.addChangeSet(changeSet);
                }
            }

            return changelog;
        } catch (Throwable e) {
            throw new ChangeLogParseException(e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        return false;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
