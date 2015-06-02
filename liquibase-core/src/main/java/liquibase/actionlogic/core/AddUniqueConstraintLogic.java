package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddUniqueConstraintAction;
import liquibase.action.core.AlterTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;

public class AddUniqueConstraintLogic extends AbstractSqlBuilderLogic {

    public static enum Clauses {
        constraintName,
        tablespace
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddUniqueConstraintAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);
        validationErrors.checkForRequiredField(AddUniqueConstraintAction.Attr.tableName, action);
        validationErrors.checkForRequiredField(AddUniqueConstraintAction.Attr.columnNames, action);
        return validationErrors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterTableAction(
                action.get(AddUniqueConstraintAction.Attr.tableName, ObjectName.class),
                generateSql(action, scope)
                ));
    }

    protected StringClauses generateSql(Action action, Scope scope) {
        String constraintName = action.get(AddUniqueConstraintAction.Attr.constraintName, String.class);
        Database database = scope.get(Scope.Attr.database, Database.class);

		StringClauses clauses = new StringClauses();
        clauses.append("ADD CONSTRAINT");
        if (constraintName != null) {
            clauses.append(Clauses.constraintName, database.escapeConstraintName(constraintName));
        }
        clauses.append("UNIQUE");
        clauses.append("("+database.escapeColumnNameList(action.get(AddUniqueConstraintAction.Attr.columnNames, String.class))+"");

        if (database.supportsInitiallyDeferrableColumns()) {
            if (action.get(AddUniqueConstraintAction.Attr.deferrable, false)) {
                clauses.append("DEFERRABLE");
            }

            if (action.get(AddUniqueConstraintAction.Attr.initiallyDeferred, false)) {
                clauses.append("INITIALLY DEFERRED");
            }
        }

        if (action.get(AddUniqueConstraintAction.Attr.disabled, false)) {
            clauses.append("DISABLE");
        }

        String tablespace = action.get(AddUniqueConstraintAction.Attr.tablespace, String.class);

        if (tablespace != null && database.supportsTablespaces()) {
            clauses.append(Clauses.tablespace, "USING INDEX TABLESPACE " + tablespace);
        }

        return clauses;

    }
}
