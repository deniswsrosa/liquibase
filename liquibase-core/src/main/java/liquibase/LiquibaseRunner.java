package liquibase;

import liquibase.changelog.annotations.reader.ChangeEntry;
import liquibase.changelog.annotations.reader.ChangeService;
import liquibase.database.GenericDataStore;
import liquibase.exception.LiquibaseChangeSetException;
import liquibase.exception.LiquibaseConfigurationException;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

public class LiquibaseRunner implements InitializingBean {

    private static final Logger logger = LogService.getLog(LiquibaseRunner.class);

    private boolean enabled = true;
    private String changeLogsScanPackage;
    private ApplicationContext context;
    private Environment springEnvironment;
    private GenericDataStore genericDataStore;


    public LiquibaseRunner(ApplicationContext context) throws ReflectiveOperationException {
        this.context = context;
        this.springEnvironment = context.getEnvironment();
        String url = springEnvironment.getProperty("spring.liquibase.connectionURL");
        String user = springEnvironment.getProperty("spring.liquibase.user");
        String password = springEnvironment.getProperty("spring.liquibase.password");
        String driver = springEnvironment.getProperty("spring.liquibase.driver");

        //TODO: Validate url/user/password/driver
        Class<?> c = Class.forName(driver);
        Constructor<?> cons = c.getConstructor();
        this.genericDataStore = (GenericDataStore) cons.newInstance();
        this.genericDataStore.connect(url, user, password);
    }


    public LiquibaseRunner(String driver, String url, String user, String password) throws ReflectiveOperationException  {
        Class<?> c = Class.forName(driver);
        Constructor<?> cons = c.getConstructor();
        this.genericDataStore = (GenericDataStore) cons.newInstance();
        this.genericDataStore.connect(url, user, password);
    }

    /**
     * For Spring users: executing liquicouch after bean is created in the Spring context
     *
     * @throws Exception exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        execute();
    }

    /**
     * Executing migration
     *
     * @throws LiquibaseException exception
     */
    public void execute() throws LiquibaseException {
        if (!isEnabled()) {
            logger.info("Liquibase is disabled. Exiting.");
            return;
        }

        validateConfig();

        logger.info("Liquibase is starting the data migration sequence..");

        try {
            executeMigration();
        } finally {
            logger.info("The dataStore is being closed.");
            this.genericDataStore.close();
        }

        logger.info("LiquiCouch has finished his job.");
    }

    private void executeMigration() throws LiquibaseException {

        ChangeService service = new ChangeService(changeLogsScanPackage, springEnvironment);

        for (Class<?> changelogClass : service.fetchChangeLogs()) {

            Object changelogInstance = null;
            try {
                changelogInstance = changelogClass.getConstructor().newInstance();

                if (context != null) {
                    context.getAutowireCapableBeanFactory().autowireBean(changelogInstance);
                }

                List<Method> changesetMethods = service.fetchChangeSets(changelogInstance.getClass());

                for (Method changesetMethod : changesetMethods) {
                    ChangeEntry changeEntry = service.createChangeEntry(changesetMethod);

                    try {
                        if (this.genericDataStore.isNewChange(changeEntry)) {
                            executeMethod(changesetMethod, changelogInstance, changeEntry);
                            this.genericDataStore.save(changeEntry);
                            logger.info(changeEntry + " applied");

                        } else if (service.isRunAlwaysChangeSet(changesetMethod)) {
                            executeMethod(changesetMethod, changelogInstance, changeEntry);
                            logger.info(changeEntry + " reapplied");

                        } else {
                            logger.info(changeEntry + " passed over");
                        }
                    } catch (LiquibaseException e) {
                        logger.severe(e.getMessage());
                    }
                }
            } catch (NoSuchMethodException e) {
                throw new LiquibaseException(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new LiquibaseException(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                throw new LiquibaseException(targetException.getMessage(), e);
            } catch (InstantiationException e) {
                throw new LiquibaseException(e.getMessage(), e);
            }
        }
    }

    private void executeMethod(Method changesetMethod, Object changelogInstance, ChangeEntry entry) throws LiquibaseException {
        try {
            executeChangeSetMethod(changesetMethod, changelogInstance);
            return;

        } catch (IllegalAccessException e) {
            throw new LiquibaseException(e.getMessage(), e);

        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            throw new LiquibaseException(targetException.getMessage(), e);
        }

    }

    private Object executeChangeSetMethod(Method changeSetMethod, Object changeLogInstance)
            throws IllegalAccessException, InvocationTargetException, LiquibaseChangeSetException {
        if (changeSetMethod.getParameterTypes().length == 1
                && changeSetMethod.getParameterTypes()[0].equals(GenericDataStore.class)) {
            logger.debug("method with bucket argument");

            return changeSetMethod.invoke(changeLogInstance, this.genericDataStore);
        } else if (changeSetMethod.getParameterTypes().length == 0) {
            logger.debug("method with no params");
            return changeSetMethod.invoke(changeLogInstance);
        } else {
            throw new LiquibaseChangeSetException("ChangeSet method " + changeSetMethod.getName() +
                    " has wrong arguments list. Please see docs for more info!");
        }
    }

    private void validateConfig() throws LiquibaseConfigurationException {
        if (!hasText(changeLogsScanPackage)) {
            throw new LiquibaseConfigurationException("Scan package for changelogs is not set: use appropriate setter");
        }
    }


    /**
     * Package name where @ChangeLog-annotated classes are kept.
     *
     * @param changeLogsScanPackage package where your changelogs are
     * @return LiquiCouch object for fluent interface
     */
    public LiquibaseRunner setChangeLogsScanPackage(String changeLogsScanPackage) {
        this.changeLogsScanPackage = changeLogsScanPackage;
        return this;
    }

    /**
     * @return true if LiquiCouch runner is enabled and able to run, otherwise false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Feature which enables/disables LiquiCouch runner execution
     *
     * @param enabled LiquiCouch will run only if this option is set to true
     * @return LiquiCouch object for fluent interface
     */
    public LiquibaseRunner setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Set Environment object for Spring Profiles (@Profile) integration
     *
     * @param environment org.springframework.core.env.Environment object to inject
     * @return LiquiCouch object for fluent interface
     */
    public LiquibaseRunner setSpringEnvironment(Environment environment) {
        this.springEnvironment = environment;
        return this;
    }

    /**
     * Should only be used for testing purposes
     */
    public void setDAO(GenericDataStore genericDataStore){
        this.genericDataStore = genericDataStore;
    }

}
