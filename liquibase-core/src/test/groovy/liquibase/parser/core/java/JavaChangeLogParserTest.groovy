package liquibase.parser.core.java

import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.test.JUnitResourceAccessor
import spock.lang.Specification

class JavaChangeLogParserTest extends Specification {

    def "parse"() {
        when:
        def parser = new JavaChangeLogParser()
        def changelog = parser.parse("com.example.liquibase.parser.java.ExampleJavaChangelog", new ChangeLogParameters(), new JUnitResourceAccessor())

        then:
        changelog.getChangeSets().size() == 4
//        changelog.getChangeSets()[2].changes[0].class.name == "liquibase.change.core.RenameTableChange"
//        changelog.getChangeSets()[3].id == "sdf"
    }
}
