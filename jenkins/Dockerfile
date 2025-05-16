FROM jenkins/jenkins:lts

USER root

RUN apt-get update && \
    apt-get install -y curl gnupg2 software-properties-common && \
    curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /usr/share/keyrings/adoptium.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb bookworm main" | tee /etc/apt/sources.list.d/adoptium.list

RUN apt-get update && \
    apt-get install -y temurin-21-jdk git curl unzip && \
    apt-get clean

RUN groupadd docker || true && \
    usermod -aG docker jenkins

ENV JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

USER jenkins
