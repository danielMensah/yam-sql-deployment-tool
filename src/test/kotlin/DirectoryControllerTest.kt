import com.ezros.controllers.DirectoryController
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import java.io.File
import java.io.IOException
import java.nio.file.Paths

class DirectoryControllerTest : Spek({
    val helper = Helper()

    beforeGroup {
        helper.setup()
    }

    afterGroup {
        helper.cleanUp()
    }

    Feature("Directory manager") {
        val directoryManager = DirectoryController();
        val absolutePath = Paths.get("").toAbsolutePath().toString()

        Scenario("read directory") {
            var path = ""
            var actual: List<File> = listOf()
            var expected: List<File> = listOf(File("$absolutePath/src/test/resources/testData/create_table.sql"))

            Given("an initial path") {
                path = "$absolutePath/src/test/resources/testData"
            }
            When("reading the directory") {
                actual = directoryManager.read(path)
            }
            Then("it should return all files in the directory") {
                assertEquals(actual.size, expected.size)
                assertTrue(actual[0].name == expected[0].name)
            }
        }

//        Scenario("read directory") {
//            var path = ""
//
//            Given("an initial path") {
//                path = "$absolutePath/src/test/resources/blah"
//            }
//            When("reading a non-existing directory") {
////                actual = directoryManager.read(path)
//            }
//            Then("it should throw an error") {
//               assertThrows(
//                    IOException::class.java,
//                    { directoryManager.read(path) },
//                   "Directory does not exist! \n $path"
//                )
//            }
//        }

        Scenario("read empty path") {
            var path = ""
            var actual: List<File> = listOf()

            Given("an initial path") {
                path = "$absolutePath/src/test/resources/emptyDir"
            }
            When("reading an empty directory") {
                actual = directoryManager.read(path)
            }
            Then("it should return an empty list") {
                assertTrue(actual.isEmpty())
            }
        }

        Scenario("rename directory") {
            val oldPath = "$absolutePath/src/test/resources/current"
            var newPath = ""

            Given("a directory name") {
                newPath = "$absolutePath/src/test/resources/1.0.0.0"
            }
            When("renaming the directory") {
                directoryManager.renameDirectory(oldPath, newPath)
            }
            Then("it should change the old directory name with the new name supplied") {
                val dir = File(newPath)

                assertTrue(dir.exists())
                assertTrue(dir.isDirectory)
            }
        }

        Scenario("copy files") {
            var source = ""
            var target = ""

            Given("a source and target directory") {
                source = "$absolutePath/src/test/resources/testData"
                target = "$absolutePath/src/test/resources/schema"
            }
            When("renaming the directory") {
                directoryManager.copyFiles(File(source), target)
            }
            Then("it should copy files from source folder to target folder") {
                val files = File(target).list().ifEmpty { arrayOf() }

                assertTrue(files.isNotEmpty())
            }
        }
    }
})