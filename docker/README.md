# Local Environment Setup Guide

## Prerequisites
- Java 17
- Maven
- Git
- IntelliJ IDEA
- Docker

## Clone the Repository

Clone the repository to your local machine using the following command:
```zsh
~ git clone --depth 1 https://github.com/BreweriesFest/brewery-app.git -b main
```

## Setting up Mongo, Kafka and Redis containers using `docker-compose`
The setup.yml file is used to create and run the Mongo, Kafka and Redis containers. The docker-compose command is used to manage the containers.

Open a terminal window and navigate to the folder containing the setup.yml file.

#### To create and run the Docker containers, run the following command:
```zsh
~ docker-compose -f setup.yml up -d
```
#### To stop the Docker containers, run the following command:
```zsh
~ docker-compose -f setup.yml stop
```
#### To start the Docker containers stopped previously, run the following command:
```zsh
~ docker-compose -f setup.yml start
```
#### To stop and delete the Docker containers completely , run the following command:
```zsh
~ docker-compose -f setup.yml down -v
```

You can verify the containers are running using the docker ps command.
```zsh
~ docker ps
CONTAINER ID   IMAGE                  COMMAND                  CREATED              STATUS              PORTS                      NAMES
bac2d9e88025   mongo-express:latest       "tini -- /docker-ent…"   6 minutes ago   Up 6 minutes   0.0.0.0:8081->8081/tcp                           docker-mongo-express-1
b8783af8fba6   bashj79/kafka-kraft        "/bin/start_kafka.sh"    6 minutes ago   Up 6 minutes   0.0.0.0:9092->9092/tcp                           docker-kafka-1
126c559aa020   mongo:latest               "docker-entrypoint.s…"   6 minutes ago   Up 6 minutes   0.0.0.0:27017->27017/tcp                         docker-mongo-1
878e6bbf029d   redis/redis-stack:latest   "/entrypoint.sh"         6 minutes ago   Up 6 minutes   0.0.0.0:6379->6379/tcp, 0.0.0.0:8001->8001/tcp   docker-redis-1
```
