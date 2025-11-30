pipeline {
    agent any

    tools {
        jdk 'JDK17'        // sama nimi kuin Manage Jenkins → Global Tool Configuration → JDK
        maven 'Maven3'    // sama nimi kuin Maven-työkalulla
    }

    environment {
        APP_NAME     = 'otp2-shopping-cart'
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
        DOCKER_IMAGE = "${APP_NAME}:${IMAGE_TAG}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                // Windows-komento
                bat 'mvn -B clean package -DskipTests'
            }
        }
        stage('Test') {
            steps {
                bat 'mvn -B test'
            }
        }
        stage('Coverage') {
            steps {
                jacoco(
                    execPattern: '**/target/jacoco.exec',
                    classPattern: 'target/classes',
                    sourcePattern: 'src/main/java',
                    inclusionPattern: '**/*.class'
                )
            }
        }
        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv('LocalSonar') {
                    bat '''
                        mvn -B sonar:sonar ^
                          -Dsonar.projectKey=otp2 ^
                          -Dsonar.projectName="otp2-shopping-cart" ^
                          -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
            }
        }
        stage('Docker build') {
            steps {
                // Jenkins korvaa ${DOCKER_IMAGE} ennen kuin komento menee Windowsille
                bat "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        stage('Docker smoke test') {
            when {
                expression { false } // vaihda true, jos haluat ottaa käyttöön
            }
            steps {
                bat "docker run --rm ${DOCKER_IMAGE} --help"
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true

            // sallitaan tyhjä tulos ettei build kaadu jos testejä ei vielä ole
            junit testResults: 'target/surefire-reports/*.xml',
                  allowEmptyResults: true
        }
        success {
            echo "Build ja Docker-image (${DOCKER_IMAGE}) valmistuivat onnistuneesti."
        }
        failure {
            echo "Build tai Docker-vaihe epäonnistui – tarkista lokit."
        }
    }
}
