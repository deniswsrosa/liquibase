package com.example.liquibase.parser.java;

import liquibase.change.core.RenameTableChange;
import liquibase.parser.core.java.ChangeSet;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.TableExistsPrecondition;

public class ExampleJavaChangelog {

    @ChangeSet(id = "1", author = "nvoxland", order = 1)
    public void firstChange() {
        System.out.println("Ran first change!");
    }

    @ChangeSet(id = "2", author = "nvoxland", order = 2)
    public void secondChange() {
        System.out.println("Ran second change!");
    }


    @ChangeSet(id = "3", author = "nvoxland", order = 3)
    public RenameTableChange createTable() {
        RenameTableChange renameTableChange = new RenameTableChange();
        renameTableChange.setOldTableName("surname");
        renameTableChange.setNewTableName("last_name");

        return renameTableChange;
    }

//    @Include(order = 4)
//    public String[] include() {
//        return new String[] {
//                "com.example.liquibase.parser.java.NestedChangelog"
//        };
//    }

    @ChangeSet(id = "4", author = "nvoxland", order = 4)
    public liquibase.changelog.ChangeSet complexChangeSet() {
        liquibase.changelog.ChangeSet changeSet = new liquibase.changelog.ChangeSet("4", "nvoxland", false, false, null, null, null, null);

        RenameTableChange renameTableChange = new RenameTableChange();
        renameTableChange.setOldTableName("last_name");
        renameTableChange.setNewTableName("final_name");

        changeSet.addChange(renameTableChange);
        TableExistsPrecondition precondition = new TableExistsPrecondition();
        precondition.setTableName("last_name");

        PreconditionContainer preconditions = new PreconditionContainer();
        preconditions.addNestedPrecondition(precondition);
        changeSet.setPreconditions(preconditions);

        return changeSet;
    }
}
