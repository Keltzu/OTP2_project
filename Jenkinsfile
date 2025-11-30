pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven-3'
    }

    environment {
        APP_NAME    = 'otp2-shopping-cart'
        IMAGE_TAG   = "${env.BUILD_NUMBER}"
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
                sh 'mvn -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
        }

        stage('Docker build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        // Lyhyt ajokoe Docker-imagella
        stage('Docker smoke test') {
            when {
                expression { false } // vaihda true, jos haluat ottaa tämän käyttöön
            }
            steps {
                sh """
                   docker run --rm ${DOCKER_IMAGE} --help
                """
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            junit 'target/surefire-reports/*.xml'
        }
        success {
            echo "Build ja Docker-image (${DOCKER_IMAGE}) valmistuivat onnistuneesti."
        }
        failure {
            echo "Build tai Docker-vaihe epäonnistui – tarkista lokit."
        }
    }
}
