pipeline {
    agent any

    environment {
        MAVEN_HOME = '/usr/local/maven'
        DOCKER_REGISTRY = 'registry.local'
        PROJECT_NAME = 'bookstore-system'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '从GitLab拉取源码...'
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                echo '编译后端微服务...'
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Unit Test') {
            steps {
                echo '运行单元测试...'
                dir('backend') {
                    sh 'mvn test'
                }
            }
        }

        stage('Build Docker Images') {
            parallel {
                stage('Auth Service Image') {
                    steps {
                        sh 'docker build -t ${DOCKER_REGISTRY}/${PROJECT_NAME}-auth:${BUILD_NUMBER} -f docker/Dockerfile --build-arg SERVICE_NAME=bookstore-auth-service backend/'
                    }
                }
                stage('User Service Image') {
                    steps {
                        sh 'docker build -t ${DOCKER_REGISTRY}/${PROJECT_NAME}-user:${BUILD_NUMBER} -f docker/Dockerfile --build-arg SERVICE_NAME=bookstore-user-service backend/'
                    }
                }
                stage('Product Service Image') {
                    steps {
                        sh 'docker build -t ${DOCKER_REGISTRY}/${PROJECT_NAME}-product:${BUILD_NUMBER} -f docker/Dockerfile --build-arg SERVICE_NAME=bookstore-product-service backend/'
                    }
                }
                stage('Order Service Image') {
                    steps {
                        sh 'docker build -t ${DOCKER_REGISTRY}/${PROJECT_NAME}-order:${BUILD_NUMBER} -f docker/Dockerfile --build-arg SERVICE_NAME=bookstore-order-service backend/'
                    }
                }
                stage('Gateway Image') {
                    steps {
                        sh 'docker build -t ${DOCKER_REGISTRY}/${PROJECT_NAME}-gateway:${BUILD_NUMBER} -f docker/Dockerfile --build-arg SERVICE_NAME=bookstore-gateway backend/'
                    }
                }
                stage('Frontend Image') {
                    steps {
                        dir('frontend') {
                            sh 'npm install --legacy-peer-deps && npm run build'
                            sh 'docker build -t ${DOCKER_REGISTRY}/${PROJECT_NAME}-frontend:${BUILD_NUMBER} -f docker/Dockerfile .'
                        }
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                echo '推送Docker镜像到镜像仓库...'
                sh 'docker push ${DOCKER_REGISTRY}/${PROJECT_NAME}-auth:${BUILD_NUMBER}'
                sh 'docker push ${DOCKER_REGISTRY}/${PROJECT_NAME}-user:${BUILD_NUMBER}'
                sh 'docker push ${DOCKER_REGISTRY}/${PROJECT_NAME}-product:${BUILD_NUMBER}'
                sh 'docker push ${DOCKER_REGISTRY}/${PROJECT_NAME}-order:${BUILD_NUMBER}'
                sh 'docker push ${DOCKER_REGISTRY}/${PROJECT_NAME}-gateway:${BUILD_NUMBER}'
                sh 'docker push ${DOCKER_REGISTRY}/${PROJECT_NAME}-frontend:${BUILD_NUMBER}'
            }
        }

        stage('Deploy to K8s') {
            steps {
                echo '部署到Kubernetes集群...'
                sh 'sed -i "s|IMAGE_TAG|${BUILD_NUMBER}|g" k8s/*.yaml'
                sh 'kubectl apply -f k8s/'
                sh 'kubectl rollout status deployment -n bookstore'
            }
        }

        stage('Health Check') {
            steps {
                echo '健康检查...'
                sh 'sleep 30'
                sh 'curl -f http://bookstore-gateway:8080/actuator/health || exit 1'
            }
        }
    }

    post {
        success {
            echo 'DevOps流水线执行成功！'
        }
        failure {
            echo '流水线执行失败，请检查日志。'
        }
    }
}
