package nrlssc.gradle.conventions

/**
 * Created by scraft on 3/27/2017.
 */
class MockApplicationConvention {
    private String mainClassName = 'unspecified'

    String getMainClassName() {
        return this.mainClassName
    }

    void setMainClassName(String mainClassName) {
        this.mainClassName = mainClassName
    }
}
