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
```
$ kind create cluster --name brewery-cluster
```
This will create Kubernetes cluster named `brewery-cluster`. You can verify the creation of the cluster by running 
```
$ kind get clusters 
```
