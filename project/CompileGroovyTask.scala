package build

import sbt._
import Keys._
import groovy.lang.GroovyClassLoader
import org.codehaus.groovy.control.{CompilationUnit, CompilerConfiguration}

object CompileGroovyTask {
    val groovySourceDirectory = SettingKey[File]("groovy-source-directory")
    val groovyOutputDirectory = SettingKey[File]("groovy-output-directory")
    val groovySources = TaskKey[Seq[File]]("groovy-sources")
    val groovyCompile = TaskKey[Unit]("groovy-compile", "Run Groovy compiler")

    val settings = Seq(
        test in Test <<= (test in Test) dependsOn (groovyCompile),
        groovySourceDirectory <<= sourceDirectory(_ / "test" / "groovy"),
        groovyOutputDirectory <<= crossTarget / "test-classes",
        groovySources <<= groovySourceDirectory map {
            dir => (dir ** "*.groovy").get
        },
        groovyCompile <<= groovyCompileTask
    )

    def groovyCompileTask = (groovySources, groovyOutputDirectory, streams, managedClasspath in Test) map {
        (src, dest, s, cp) => {
            def compilationUnit: CompilationUnit = {
                def classLoader(parent: ClassLoader, conf: Option[CompilerConfiguration]): GroovyClassLoader = conf match {
                    case Some(x) => new GroovyClassLoader(parent, x)
                    case None => new GroovyClassLoader(parent)
                }
                def configuration: CompilerConfiguration = {
                    val conf = new CompilerConfiguration
                    conf.setTargetDirectory(dest)
                    conf
                }
                def loaders(conf: CompilerConfiguration): (GroovyClassLoader, GroovyClassLoader) = {
                    val loader = classLoader(getClass.getClassLoader, Some(conf))
                    val transformer = classLoader(getClass.getClassLoader, None)
                    cp.files.foreach {
                        f =>
                            loader.addURL(f.toURI.toURL)
                            transformer.addURL(f.toURI.toURL)
                    }
                    (loader, transformer)
                }

                s.log.info("Compiling " + src.length + " groovy sources to " + dest + "...")

                val conf = configuration
                val classLoaders = loaders(conf)
                val unit = new CompilationUnit(conf, null, classLoaders _1, classLoaders _2)
                unit.addSources(src.map(_.getAbsoluteFile).toArray)
                unit
            }

            compilationUnit compile()
        }
    }
}
