## Environment Setup Guide

### Prerequisites
- Java 17
- Maven
- Git
- IntelliJ IDEA
- Docker
- Kind

### Clone the Repository


### Set up the Environment

#### Set up the Kubernetes Cluster using `kind`
```console
kind create cluster --image kindest/node:v1.26.0 --name brewery-cluster
```
This will create Kubernetes cluster named `brewery-cluster`. You can verify the creation of the cluster by running 
```console
kind get clusters 
```
Set default cluster to `brewery-cluster`
```console
kubectl cluster-info --context kind-brewery-cluster
```

#### Set up the Kafka
```console
kubectl apply -f k8s/kafka-kraft/kafka.yml
```
This will set up the Kafka cluster. You can verify the setup by running the following command:
```zsh
~ kubectl -n kafka-kraft get pods
NAME      READY   STATUS    RESTARTS   AGE
kafka-0   1/1     Running   0          2m42s
kafka-1   1/1     Running   0          43s
kafka-2   1/1     Running   0          40s
kafka-3   1/1     Running   0          37s
kafka-4   1/1     Running   0          34s
```
