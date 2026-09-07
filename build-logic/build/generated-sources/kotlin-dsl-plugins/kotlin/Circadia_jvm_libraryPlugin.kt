/**
 * Precompiled [circadia.jvm.library.gradle.kts][Circadia_jvm_library_gradle] script plugin.
 *
 * @see Circadia_jvm_library_gradle
 */
public
class Circadia_jvm_libraryPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Circadia_jvm_library_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
