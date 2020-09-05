import com.ezros.controllers.SqlController
import org.junit.jupiter.api.Assertions.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.io.File
import java.nio.file.Paths

class SqlControllerTest : Spek({
    val absolutePath = Paths.get("").toAbsolutePath().toString()
    val helper = Helper()
    val sqlManager = SqlController()
    val connection = "jdbc:postgresql://ec2-54-247-89-181.eu-west-1.compute.amazonaws.com:5432/d2kflm7gum4g90?user=ytgpabwaislipg&password=5da0f2d2c7fa21d19c087f8e9e75980249bbc9be11ddc478d9cff6d2eafe8301"

    beforeGroup {
        helper.setup()
        File("$absolutePath/src/test/resources/query_file.sql").writeText("SELECT * FROM users")
        File("$absolutePath/src/test/resources/query_file_fail.sql").writeText("SELECT * FROM nonExistingTable")
    }

    afterGroup {
        helper.cleanUp()
        File("$absolutePath/src/test/resources/query_file.sql").delete()
        File("$absolutePath/src/test/resources/query_file_fail.sql").delete()
        sqlManager.close()
    }

    Feature("Sql manager") {

        Scenario("can connect to DB") {

            Given("a .properties file with db credentials") { }
            When("connecting to the database") {
                sqlManager.connect(connection)
            }
            Then("it should connect to the desired database") {
                assertTrue(sqlManager.isConnected())
            }
        }

        Scenario("cannot connect to DB") {
            var connectionWrong = ""

            Given("a .properties file with wrong db credentials") {
                if (sqlManager.isConnected()) {
                    sqlManager.close()
                }

                connectionWrong = "jdbc:blah"
            }
            When("connecting to the database") {
                sqlManager.connect(connectionWrong)
            }
            Then("it should return false") {
                assertFalse(sqlManager.isConnected())
            }
        }

        Scenario("can execute file scripts") {
            var fileScript = File("")
            var result: Map<String, String> = mapOf()

            Given("a file containing SQL scripts") {
                if (!sqlManager.isConnected()) {
                    sqlManager.connect(connection)
                }

                fileScript = File("$absolutePath/src/test/resources/query_file.sql")
            }
            When("executing the scripts") {
                result = sqlManager.runScript("1.0.0.0", "", fileScript)
            }
            Then("it should successfully execute the scripts and return it as successful") {
                assertTrue(result.isEmpty())
            }
        }

        Scenario("can execute/manage file scripts that failed") {
            var fileScript = File("")
            var result: Map<String, String> = mapOf()

            Given("a file containing failing SQL scripts") {
                if (!sqlManager.isConnected()) {
                    sqlManager.connect(connection)
                }

                fileScript = File("$absolutePath/src/test/resources/query_file_fail.sql")
            }
            When("executing the scripts") {
                result = sqlManager.runScript("1.0.0.0", "", fileScript)
            }
            Then("it should execute the scripts and return it as failed") {
                assertTrue(result.isNotEmpty())
            }
        }
    }
})