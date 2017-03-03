@Library("BuildUtils")
import com.curalate.jenkins.*

def utils = new BuildUtils()

utils.build { BuildMetadata data ->
    def sbt = new Sbt(steps, data)

    String version = sbt.getAppVersion()

    setBuildName(version)

    stage('Build') {
        sbt.execute("clean compile")
    }

    stage('Test') {
        sbt.execute("scripted")
    }

    if (data.isDeployable) {
        stage('Deploy') {
            sbt.execute("publish")

            tagGitHub(version)
        }
    }
}
