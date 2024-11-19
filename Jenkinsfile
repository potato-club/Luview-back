pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'Cloning Repository'
                git branch: 'master',
                    credentialsId: 'Luview',
                    url: 'https://github.com/potato-club/Luview-back.git'
            }
        }
    }
}
