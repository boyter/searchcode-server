pipeline {
    agent any

    stages {
        stage('Build & Test') {
            steps {
                sh 'docker build -t searchcode-server-unit-test -f ./assets/docker/Dockerfile.test .'
            }
        }
    }
}