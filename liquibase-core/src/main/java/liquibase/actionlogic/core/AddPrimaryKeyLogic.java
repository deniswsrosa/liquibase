package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddPrimaryKeyAction;
import liquibase.action.core.AlterTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;

public class AddPrimaryKeyLogic extends AbstractSqlBuilderLogic {

    public static enum Clauses {
        constraintName, columnNames, tablespace,
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddPrimaryKeyAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);
        errors.checkForRequiredField(AddPrimaryKeyAction.Attr.columnNames, action);
        errors.checkForRequiredField(AddPrimaryKeyAction.Attr.tableName, action);
        if (action.get(AddPrimaryKeyAction.Attr.clustered, false)) {
            errors.addUnsupportedError("Adding a clustered primary key", scope.get(Scope.Attr.database, Database.class).getShortName());
        }

        return errors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(
                new AlterTableAction(scope.get(AddPrimaryKeyAction.Attr.tableName, ObjectName.class),
                        generateSql(action, scope)));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        StringClauses clauses = new StringClauses();

        clauses.append("ADD CONSTRAINT");

        //TODO: Informix logic from AddPrimaryKeyGeneratorInformix:
//        // Using auto-generated names of the form <constraint_type><tabid>_<constraintid> can cause collisions
//        // See here: http://www-01.ibm.com/support/docview.wss?uid=swg21156047
//        String constraintName = statement.getConstraintName();
//        if (constraintName != null && !constraintName.matches("[urcn][0-9]+_[0-9]+")) {
//            sql.append(" CONSTRAINT ");
//            sql.append(database.escapeConstraintName(constraintName));
//        }
//
        clauses.append(Clauses.constraintName, database.escapeConstraintName(action.get(AddPrimaryKeyAction.Attr.constraintName, String.class)));
        clauses.append("PRIMARY KEY");
        clauses.append(Clauses.columnNames, "(" + database.escapeColumnNameList(action.get(AddPrimaryKeyAction.Attr.columnNames, String.class)) + ")");

        String tablespace = action.get(AddPrimaryKeyAction.Attr.tablespace, String.class);
        if (tablespace != null && database.supportsTablespaces()) {
            clauses.append(Clauses.tablespace, "USING INDEX TABLESPACE " + tablespace);
        }

        return clauses;
    }
}
