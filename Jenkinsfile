pipeline {
    agent any

    stages {
        stage('Build & Test') {
            steps {
                sh 'docker build -t searchcode-server-test -f ./assets/docker/test/Dockerfile .'
            }
        }
    }
}