import java.io.File
import java.nio.file.Paths

class Helper {
    private val absolutePath = Paths.get("").toAbsolutePath().toString()
    private val directories: List<String> = arrayListOf("emptyDir", "current", "testData", "schema", "1.0.0.0")
    private val files: Map<String, String> = mapOf("testData" to "create_table.sql", "schema" to "create_table.sql")

    fun setup() {

        directories.forEach{dir ->
            if (!File("$absolutePath/src/test/resources/$dir").exists()) {
                if (File("$absolutePath/src/test/resources/$dir").mkdir()) {
                    print("$dir folder created! \n")
                } else {
                    print("could not create $dir folder! \n")
                }
            } else {
                print("$dir already exists!")
            }

        }

        files.forEach{file ->
            if (!File("$absolutePath/src/test/resources/${file.key}/${file.value}").exists()) {
                if (File("$absolutePath/src/test/resources/${file.key}/${file.value}").createNewFile()) {
                    File("$absolutePath/src/test/resources/testData/create_table.sql").writeText("CREATE TABLE IF NOT EXISTS Animals (\n" +
                            "    id serial not null primary key,\n" +
                            "    name text not null \n" +
                            "  );")

                    print("create_table.sql file created! \n")
                } else {
                    print("could not create file $file \n")
                }
            } else {
                print("$file already exists!")
            }
        }

        print("Setup Done! \n")
    }

    fun cleanUp() {
        files.forEach{file ->
            if (File("$absolutePath/src/test/resources/${file.key}/${file.value}").exists()) {
                if (File("$absolutePath/src/test/resources/${file.key}/${file.value}").delete()) {
                    print("create_table.sql file deleted! \n")
                } else {
                    print("could not delete file $file \n")
                }
            } else {
                print("$file does not exists!")
            }

        }

        directories.forEach{dir ->
            if (File("$absolutePath/src/test/resources/$dir").exists()) {
                if (File("$absolutePath/src/test/resources/$dir").delete()) {
                    print("$dir folder deleted! \n")
                } else {
                    print("could not delete $dir folder! \n")
                }
            } else {
                print("$dir does not exists!")
            }
        }

        print("Cleanup Done!")
    }
}