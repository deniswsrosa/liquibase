package liquibase.parser.core.java;

import liquibase.change.AbstractChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.AbstractSqlStatement;
import liquibase.statement.SqlStatement;

import java.lang.reflect.Method;

public class JavaChange extends AbstractChange {

    private Method changeMethod;
    private Object changeLogInstance;

    public JavaChange(Method changeMethod, Object changeLogInstance) {
        this.changeMethod = changeMethod;
        this.changeLogInstance = changeLogInstance;
    }

    @Override
    public String getConfirmationMessage() {
        return "Executed "+changeMethod.getName();
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[0];
    }

    public class JavaChangeStatement extends AbstractSqlStatement {
        public void executeMethod() {
            try {
                changeMethod.invoke(changeLogInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    public class JavaChangeGenerator extends AbstractSqlGenerator<JavaChangeStatement> {

        @Override
        public ValidationErrors validate(JavaChangeStatement statement, Database database, SqlGeneratorChain<JavaChangeStatement> sqlGeneratorChain) {
            return new ValidationErrors();
        }

        @Override
        public Sql[] generateSql(JavaChangeStatement statement, Database database, SqlGeneratorChain<JavaChangeStatement> sqlGeneratorChain) {
            statement.executeMethod();

            return new Sql[0];
        }
    }
}
