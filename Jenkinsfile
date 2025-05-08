def services = [
    "auth-service",
    "product-service",
    "order-service",
    "user-service",
    "inventory-service",
    "notification-service",
    "payment-service",
    "review-service",
    "cart-service",
    "chat-service",
    "ai-service",
    "api-gateway",
    "eureka-service"
]

pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "huutai2403"
    }

    options {
        skipDefaultCheckout(true)
        timestamps()
    }

    stages {

        stage('Checkout Code') {
            steps {
                echo " Checking out repository..."
                git 'https://github.com/huutai243/musical-microservice-BE'
            }
        }

        stage('Build & Package Each Service') {
            steps {
                script {
                    services.each { service ->
                        echo " Building ${service}..."
                        dir("${service}") {
                            sh './mvnw clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    services.each { service ->
                        def imageName = "${DOCKER_REGISTRY}/${service}:latest"
                        echo " Building Docker image for ${service} → ${imageName}"
                        sh "docker build -t ${imageName} ./${service}"
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'USERNAME',
                        passwordVariable: 'PASSWORD'
                    )]) {
                        sh 'echo $PASSWORD | docker login -u $USERNAME --password-stdin'

                        services.each { service ->
                            def imageName = "${DOCKER_REGISTRY}/${service}:latest"
                            echo " Pushing ${imageName} to Docker Hub"
                            sh "docker push ${imageName}"
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "CI completed successfully for all services"
        }
        failure {
            echo " Build failed. Please check logs."
        }
    }
}
