pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        IMAGE_NAME = 'campus-equipment-booking:set-f'
        CONTAINER_NAME = 'campus-equipment-booking-test'
        APP_PORT = '8080'
    }

    stages {
        stage('Checkout GitHub') {
            steps {
                checkout scm
                sh '''
                    mkdir -p evidence
                    git log -5 --oneline | tee evidence/git-history.txt
                    git status --short | tee evidence/git-status.txt
                    echo "Repository checked out successfully"
                '''
            }
        }

        stage('Build and Test') {
            steps {
                sh '''
                    set -eu

                    if [ -f ./mvnw ]; then
                        chmod +x ./mvnw
                        ./mvnw -B clean verify
                    else
                        mvn -B clean verify
                    fi

                    find target -maxdepth 1 -name '*.jar' -type f -print
                    test -n "$(find target -maxdepth 1 -name '*.jar' -type f -print -quit)"
                '''
            }
            post {
                always {
                    junit(
                        testResults: 'target/surefire-reports/TEST-*.xml',
                        allowEmptyResults: true
                    )
                    archiveArtifacts(
                        artifacts: 'target/*.jar,target/surefire-reports/**',
                        allowEmptyArchive: true
                    )
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    set -eu
                    test -f Dockerfile
                    docker build -t "$IMAGE_NAME" .
                    docker image inspect "$IMAGE_NAME" \
                        | tee evidence/docker-image-inspect.json
                    docker images "$IMAGE_NAME" \
                        | tee evidence/docker-images.txt
                '''
            }
        }

        stage('Run Container and Verify Health') {
            steps {
                sh '''
                    set -eu

                    docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true

                    docker run -d --name "$CONTAINER_NAME" \
                        -p "$APP_PORT:8080" "$IMAGE_NAME"

                    OK=0
                    for i in $(seq 1 30); do
                        if curl -fsS \
                            http://127.0.0.1:8080/actuator/health \
                            -o evidence/docker-health.json; then
                            cat evidence/docker-health.json
                            OK=1
                            break
                        fi
                        sleep 2
                    done

                    docker ps -a --filter \
                        "name=$CONTAINER_NAME" \
                        | tee evidence/docker-container.txt

                    docker logs "$CONTAINER_NAME" \
                        > evidence/docker-container.log 2>&1 || true

                    test "$OK" -eq 1
                    grep -q '"status"[[:space:]]*:[[:space:]]*"UP"' \
                        evidence/docker-health.json
                '''
            }
            post {
                always {
                    sh '''
                        docker rm -f "$CONTAINER_NAME" \
                            >/dev/null 2>&1 || true
                    '''
                }
            }
        }
    }

    post {
        always {
            sh '''
                mkdir -p evidence
                docker images "$IMAGE_NAME" \
                    > evidence/docker-images-final.txt 2>&1 || true
                git status --short \
                    > evidence/git-status-final.txt 2>&1 || true
            '''
            archiveArtifacts(
                artifacts: 'evidence/**',
                allowEmptyArchive: true
            )
        }

        success {
            echo 'Build, tests, Docker image creation and Docker health check completed successfully.'
        }

        failure {
            echo 'Pipeline failed. Inspect the failed stage and archived logs.'
        }
    }
}
