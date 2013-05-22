import org.junit.Test
import org.junit.runner.{RunWith, JUnitCore, Result}
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import java.io.File
import java.lang.Class

/**
 * @author Jakub Torbicki
 */
class GroovyTestRunner {

    val rootPackage = "com.xyz.groovy"

    @Test
    def runGroovyTests {
        assertThat(runJUnit(findAllTests(rootPackage)), equalTo(0))
    }

    def findAllTests(rootPackage: String): List[Class[_]] = {
        def tryToLoadClass(file: File, pkg: String): List[Class[_]] = {
            def resolveClassName(name: String): String = pkg + "." + name.substring(0, name.length - ".class".length)
            if (file.getName.endsWith(".class")) {
                List(getClass.getClassLoader.loadClass(resolveClassName(file.getName)))
            } else {
                Nil
            }
        }

        def doFind(dir: File, pkg: String): List[Class[_]] = {
            (for (file <- dir.listFiles()) yield {
                file.isDirectory match {
                    case true => doFind(file, pkg + "." + file.getName)
                    case false => tryToLoadClass(file, pkg)
                }
            }).flatten.toList
        }
        doFind(new File(getClass.getClassLoader.getResource(".").getPath + rootPackage.replace(".", "/")), rootPackage)
    }

    def runJUnit(tests: List[Class[_]]): Int = {
        def handleResult(result: Result): Int = {
            println(result.getFailures)
            result.getFailureCount
        }
        val jUnit = new JUnitCore()
        tests.foldLeft(0)((acc: Int, test: Class[_]) => handleResult(jUnit.run(test)))
    }

    def isJUnitTest(c: Class[_]): Boolean = c.isAnnotationPresent(classOf[RunWith])
}
