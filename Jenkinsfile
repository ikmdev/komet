#!groovy

@Library("titan-library") _

pipeline {

    agent any

    tools {
        jdk "java-21"
        maven 'default'
        git 'git'
    }

    environment {
        SONAR_AUTH_TOKEN    = credentials('sonarqube_pac_token')
        SONARQUBE_URL       = "${GLOBAL_SONARQUBE_URL}"
        SONAR_HOST_URL      = "${GLOBAL_SONARQUBE_URL}"

        GPG_PASSPHRASE      = credentials('gpg_passphrase')

        BRANCH_NAME = "${GIT_BRANCH.startsWith('origin/') ? GIT_BRANCH['origin/'.length()..-1] : GIT_BRANCH}"
        // // run the build at 03:10 on every day-of-week from Monday through Friday but only on the main branch
        // String cron_string = BRANCH_NAME == "main" ? "10 3 * * 1-5" : ""
    }

    triggers {
        //cron(cron_string)
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }

    options {
        // Set this to true if you want to clean workspace during the prep stage
        skipDefaultCheckout(false)

        // Console debug options
        timestamps()
        ansiColor('xterm')

        // Discard old builds to conserve CI/CD storage
        buildDiscarder logRotator(
            numToKeepStr: '10'
        )

        // Necessary for communicating status to gitlab
        gitLabConnection('fda-shield-group')
    }

    stages {
        stage('Maven Build') {
            steps {
                updateGitlabCommitStatus name: 'build', state: 'running'
                script{
                    configFileProvider([configFile(fileId: 'settings.xml', variable: 'MAVEN_SETTINGS')]) {
                        sh """
                        mvn clean install -s '${MAVEN_SETTINGS}' \
                            --batch-mode \
                            -e \
                            -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                            -Dmaven.build.cache.enabled=false \
                            -PcodeQuality
                        """
                    }
                }
            }
        }
        stage('SonarQube Scan') {
            steps{
                configFileProvider([configFile(fileId: 'settings.xml', variable: 'MAVEN_SETTINGS')]) {
                    withSonarQubeEnv(installationName: 'EKS SonarQube', envOnly: true) {
                        // This expands the environment variables SONAR_CONFIG_NAME, SONAR_HOST_URL, SONAR_AUTH_TOKEN that can be used by any script.
                        sh """
                            mvn sonar:sonar \
                                -Dsonar.qualitygate.wait=true \
                                -Dsonar.token=${SONAR_AUTH_TOKEN} \
                                -s '${MAVEN_SETTINGS}' \
                                -Dmaven.build.cache.enabled=false \
                                --batch-mode
                        """
                    }
                }
                script{
                    configFileProvider([configFile(fileId: 'settings.xml', variable: 'MAVEN_SETTINGS')]) {

                        def pmd = scanForIssues tool: [$class: 'Pmd'], pattern: '**/target/pmd.xml'
                        publishIssues issues: [pmd]

                        def spotbugs = scanForIssues tool: [$class: 'SpotBugs'], pattern: '**/target/spotbugsXml.xml'
                        publishIssues issues:[spotbugs]

                        publishIssues id: 'analysis', name: 'All Issues',
                            issues: [pmd, spotbugs],
                            filters: [includePackage('io.jenkins.plugins.analysis.*')]
                    }
                }
            }
            post {
                always {
                    echo "post always SonarQube Scan"
                }
            }
        }

        stage("Publish to Nexus Repository Manager") {
            steps {
                script {
                    pomModel = readMavenPom(file: 'pom.xml')
                    pomVersion = pomModel.getVersion()
                    isSnapshot = pomVersion.contains("-SNAPSHOT")
                    repositoryId = 'maven-releases'

                    if (isSnapshot) {
                        repositoryId = 'maven-snapshots'
                    }

                    configFileProvider([configFile(fileId: 'settings.xml', variable: 'MAVEN_SETTINGS')]) {
                        sh """
                            mvn deploy \
                            --batch-mode \
                            -e \
                            -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                            -Dmaven.build.cache.enabled=false \
                            -DskipTests \
                            -DskipITs \
                            -Dmaven.main.skip \
                            -Dmaven.test.skip \
                            -s '${MAVEN_SETTINGS}' \
                            -P inject-application-properties \
                            -DrepositoryId='${repositoryId}' \
                            -PsignArtifacts -Dgpg.passphrase='${GPG_PASSPHRASE}'
                        """
                    }
                }
            }
        }

        stage("Get POM Version") {
            steps {
                // Clean before checkout / build
                cleanWs()
                checkout scm

                // Get the release information
                script {
                    pomModel = readMavenPom(file: 'pom.xml')
                    pomVersion = pomModel.getVersion()
                    isSnapshot = pomVersion.contains("-SNAPSHOT")
                    pomGroupId = pomModel.groupId
                    repositoryId = 'maven-releases'

                    mvnInstallerArgs = '-P create-installer'

                    if (isSnapshot) {
                        snapshotBranchName = BRANCH_NAME
                        if (BRANCH_NAME != "main") {
                            try {
                                snapshotBranchName = BRANCH_NAME.split("/")[1].substring(0, Math.min(BRANCH_NAME.split("/")[1].length(), 15))
                            } catch(Throwable th) {
                                snapshotBranchName = BRANCH_NAME.substring(0, Math.min(BRANCH_NAME.length(), 15))
                            }
                        }
                        jpackageAppName = "Komet-SNAPSHOT-\${NODE_NAME}-" + snapshotBranchName
                        jpackageAppVersion = pomVersion.split('\\.')[0] + "." + pomVersion.split('\\.')[1] + "."  + BUILD_NUMBER
                        mvnInstallerArgs +=     """ \
                                                    -D"jpackage.app.name"=${jpackageAppName} \
                                                    -D"jpackage.app.dest"=target/dist/snapshot-installer \
                                                    -D"jpackage.app.version"=${jpackageAppVersion} \
                                                """
                    }

                    echo "BRANCH_NAME: ${BRANCH_NAME}"
                    echo "pomVersion: ${pomVersion}"
                    echo "isSnapshot: ${isSnapshot}"
                    echo "pomGroupId: ${pomGroupId}"
                }
            }
        }

        stage("Build Installers for Multiple OS and Publish to Nexus Repository Manager") {
            parallel {
                stage("Linux Installer") {
                    agent {
                        label 'linux'
                    }
                    stages {
                        stage("Build Linux Installer") {
                            steps {
                                // Clean before checkout / build
                                cleanWs()
                                checkout scm

                                configFileProvider([configFile(fileId: 'settings.xml', variable: 'MAVEN_SETTINGS')]) {
                                    //Build Komet and create installer
                                    sh """
                                        mvn --version
                                        mvn clean install \
                                            -s ${MAVEN_SETTINGS} \
                                            ${mvnInstallerArgs} \
                                            --batch-mode \
                                            -e \
                                            -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                                            -Dmaven.build.cache.enabled=false \
                                            -DskipTests \
                                            -DskipITs
                                    """
                                }
                            }
                            post {
                                always {
                                    archiveArtifacts artifacts: 'application/target/dist/snapshot-installer/*.*, application/target/dist/installer/*.*', fingerprint: true
                                }
                            }
                        }
                        stage("Publish Linux Installer Release to Nexus Repository Manager") {
                            when{
                                expression{
                                    buildingTag() && !isSnapshot
                                }
                            }
                            steps {
                                configFileProvider([configFile(fileId: 'settings.xml', variable: 'MAVEN_SETTINGS')]) {
                                    //Deploy Komet Installer to Nexus
                                    sh """
                                        mvn deploy:deploy-file \
                                            --batch-mode \
                                            -e \
                                            -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                                            -Dmaven.build.cache.enabled=false \
                                            -s ${MAVEN_SETTINGS} \
                                            -Dfile=\$(find application/target/dist/installer/ -name komet-${pomVersion}*.rpm) \
                                            -Durl=https://nexus.build.tinkarbuild.com/repository/maven-releases/ \
                                            -DgroupId=${pomGroupId} \
                                            -DartifactId=komet-installer-linux \
                                            -Dversion=${pomVersion} \
                                            -Dclassifier=unsigned \
                                            -Dtype=pkg \
                                            -Dpackaging=pkg \
                                            -DrepositoryId=titan-maven-releases
                                    """
                                }
                            }
                        }
                    }
                }
                stage("Mac M1 Installer") {
                    agent {
                        label 'mac_m1'
                    }
                    stages {
                        stage("Build Mac M1 Installer") {
                            steps {
                                // Clean before checkout / build
                                cleanWs()
                                checkout scm

                                configFileProvider([configFile(fileId: 'settings.xml', variable: 'MAVEN_SETTINGS')]) {
                                    //Build Komet and create installer
                                    sh """
                                        mvn --version
                                        mvn clean install \
                                            -s ${MAVEN_SETTINGS} \
                                            ${mvnInstallerArgs} \
                                            --batch-mode \
                                            -e \
                                            -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                                            -Dmaven.build.cache.enabled=false \
                                            -DskipTests \
                                            -DskipITs
                                    """
                                }
                            }
                            post {
                                always {
                                    archiveArtifacts artifacts: 'application/target/dist/snapshot-installer/*.*, application/target/dist/installer/*.*', fingerprint: true
                                }
                            }
                        }
                        stage("Publish Mac M1 Installer Release to Nexus Repository Manager") {
                            when{
                                expression{
                                    buildingTag() && !isSnapshot
                                }
                            }
                            steps{
                                configFileProvider([configFile(fileId: 'settings.xml', variable: 'MAVEN_SETTINGS')]) {
                                    //Deploy Komet Installer to Nexus
                                    sh """
                                        mvn deploy:deploy-file \
                                            --batch-mode \
                                            -e \
                                            -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                                            -Dmaven.build.cache.enabled=false \
                                            -s ${MAVEN_SETTINGS} \
                                            -Dfile=\$(find application/target/dist/installer/ -name Komet-${pomVersion}*.pkg) \
                                            -Durl=https://nexus.build.tinkarbuild.com/repository/maven-releases/ \
                                            -DgroupId=${pomGroupId} \
                                            -DartifactId=komet-installer-macm1 \
                                            -Dversion=${pomVersion} \
                                            -Dclassifier=unsigned \
                                            -Dtype=pkg \
                                            -Dpackaging=pkg \
                                            -DrepositoryId=titan-maven-releases
                                    """
                                }
                            }
                        }
                    }
                }
                stage("Mac Intel Installer") {
                    agent {
                        label 'mac_intel'
                    }
                    stages {
                        stage("Build Mac Intel Installer") {
                            steps {
                                // Clean before checkout / build
                                cleanWs()
                                checkout scm

                                configFileProvider([configFile(fileId: 'settings.xml', variable: 'MAVEN_SETTINGS')]) {
                                    //Build Komet and create installer
                                    sh """
                                        mvn --version
                                        mvn clean install \
                                            -s ${MAVEN_SETTINGS} \
                                            ${mvnInstallerArgs} \
                                            --batch-mode \
                                            -e \
                                            -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                                            -Dmaven.build.cache.enabled=false \
                                            -DskipTests \
                                            -DskipITs
                                    """
                                }
                            }
                            post {
                                always {
                                    archiveArtifacts artifacts: 'application/target/dist/snapshot-installer/*.*, application/target/dist/installer/*.*', fingerprint: true
                                }
                            }
                        }
                        stage("Publish Mac Intel Installer Release to Nexus Repository Manager") {
                            when{
                                expression{
                                    buildingTag() && !isSnapshot
                                }
                            }
                            steps{
                                configFileProvider([configFile(fileId: 'settings.xml', variable: 'MAVEN_SETTINGS')]) {
                                    //Deploy Komet Installer to Nexus
                                    sh """
                                        mvn deploy:deploy-file \
                                            --batch-mode \
                                            -e \
                                            -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                                            -Dmaven.build.cache.enabled=false \
                                            -s ${MAVEN_SETTINGS} \
                                            -Dfile=\$(find application/target/dist/installer/ -name Komet-${pomVersion}*.pkg) \
                                            -Durl=https://nexus.build.tinkarbuild.com/repository/maven-releases/ \
                                            -DgroupId=${pomGroupId} \
                                            -DartifactId=komet-installer-macintel \
                                            -Dversion=${pomVersion} \
                                            -Dclassifier=unsigned \
                                            -Dtype=pkg \
                                            -Dpackaging=pkg \
                                            -DrepositoryId=titan-maven-releases
                                    """
                                }
                            }
                        }
                    }
                }
                stage("Windows Installer") {
                    agent {
                        label 'windows'
                    }
                    stages {
                        stage("Build Windows Installer") {
                            steps {
                                // Clean before checkout / build
                                cleanWs()
                                checkout scm

                                script {
                                    //Format mvn args for windows
                                    mvnInstallerArgs_Windows = mvnInstallerArgs\
                                                                    .replaceAll("\\\$\\{NODE_NAME\\}", NODE_NAME)\
                                                                    .replaceAll("/","\\\\")
                                }

                                configFileProvider([configFile(fileId: 'settings.xml', variable: 'MAVEN_SETTINGS')]) {
                                    //Build Komet and create installer
                                    bat """
                                        mvn --version && \
                                        mvn clean install \
                                            -s ${MAVEN_SETTINGS} \
                                            ${mvnInstallerArgs_Windows} \
                                            --batch-mode \
                                            -e \
                                            -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                                            -Dmaven.build.cache.enabled=false \
                                            -DskipTests \
                                            -DskipITs
                                    """
                                }
                            }
                            post {
                                always {
                                    archiveArtifacts artifacts: 'application/target/dist/snapshot-installer/*.*, application/target/dist/installer/*.*', fingerprint: true
                                }
                            }
                        }
                        stage("Publish Windows Installer Release to Nexus Repository Manager") {
                            when{
                                expression{
                                    buildingTag() && !isSnapshot
                                }
                            }
                            steps{
                                configFileProvider([configFile(fileId: 'settings.xml', variable: 'MAVEN_SETTINGS')]) {
                                    //Deploy Komet Installer to Nexus
                                    bat """
                                        setx JAVA_HOME "${JAVA_HOME}"
                                        set PATH=${JAVA_HOME}\\bin'${M2_HOME}\\bin;${WIX_HOME}\\bin;%PATH%
                                        mvn deploy:deploy-file \
                                            --batch-mode \
                                            -e \
                                            -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                                            -Dmaven.build.cache.enabled=false \
                                            -s ${MAVEN_SETTINGS} \
                                            -Dfile=application\\target\\dist\\installer\\Komet-${pomVersion}.msi \
                                            -Durl=https://nexus.build.tinkarbuild.com/repository/maven-releases/ \
                                            -DgroupId=${pomGroupId} \
                                            -DartifactId=komet-installer-windows \
                                            -Dversion=${pomVersion} \
                                            -Dclassifier=unsigned \
                                            -Dtype=msi \
                                            -Dpackaging=msi \
                                            -DrepositoryId=titan-maven-releases
                                    """
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        failure {
            updateGitlabCommitStatus name: 'build', state: 'failed'
            emailext(

                recipientProviders: [requestor(), culprits()],
                subject: "Build failed in Jenkins: ${env.JOB_NAME} - #${env.BUILD_NUMBER}",
                body: """
                    Build failed in Jenkins: ${env.JOB_NAME} - #${BUILD_NUMBER}

                    See attached log or URL:
                    ${env.BUILD_URL}

                """,
                attachLog: true
            )
        }
        aborted {
            updateGitlabCommitStatus name: 'build', state: 'canceled'
        }
        unstable {
            updateGitlabCommitStatus name: 'build', state: 'failed'
            emailext(
                subject: "Unstable build in Jenkins: ${env.JOB_NAME} - #${env.BUILD_NUMBER}",
                body: """
                    See details at URL:
                    ${env.BUILD_URL}

                """,
                attachLog: true
            )
        }
        changed {
            updateGitlabCommitStatus name: 'build', state: 'success'
            emailext(
                recipientProviders: [requestor(), culprits()],
                subject: "Jenkins build is back to normal: ${env.JOB_NAME} - #${env.BUILD_NUMBER}",
                body: """
                Jenkins build is back to normal: ${env.JOB_NAME} - #${env.BUILD_NUMBER}

                See URL for more information:
                ${env.BUILD_URL}
                """
            )
        }
        success {
            updateGitlabCommitStatus name: 'build', state: 'success'
        }
        cleanup {
            // Clean the workspace after build
            cleanWs(cleanWhenNotBuilt: false,
                deleteDirs: true,
                disableDeferredWipeout: true,
                notFailBuild: true,
                patterns: [
                [pattern: '.gitignore', type: 'INCLUDE']
            ])
        }
    }
}
