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
```zsh
~ kind create cluster --image kindest/node:v1.26.0 --name brewery-cluster
Creating cluster "brewery-cluster" ...
 ✓ Ensuring node image (kindest/node:v1.26.0) 🖼 
 ✓ Preparing nodes 📦  
 ✓ Writing configuration 📜 
 ✓ Starting control-plane 🕹️ 
 ✓ Installing CNI 🔌 
 ✓ Installing StorageClass 💾 
Set kubectl context to "brewery-cluster"
You can now use your cluster with:

kubectl cluster-info --context kind-brewery-cluster

Thanks for using kind! 😊
```
This will create Kubernetes cluster named `brewery-cluster`. You can verify the creation of the cluster by running 
```zsh
~ kind get clusters
brewery-cluster
```
Set default cluster to `brewery-cluster`
```zsh
~ kubectl config get-contexts 
CURRENT   NAME                   CLUSTER                AUTHINFO               NAMESPACE 
          kind-brewery-cluster   kind-brewery-cluster   kind-brewery-cluster   

~ kubectl config use-context kind-brewery-cluster
Switched to context "kind-brewery-cluster".

~ kubectl config current-context
kind-brewery-cluster
```

#### Set up the Kafka
```zsh
~ kubectl apply -f k8s/kafka-kraft/kafka.yml
namespace/kafka-kraft created
persistentvolume/kafka-pv-volume created
persistentvolumeclaim/kafka-pv-claim created
service/kafka-svc created
statefulset.apps/kafka created
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

#### Set up the MongoDB
```zsh
~ kubectl apply -f k8s/mongo/mongo.yml
namespace/mongo created
secret/mongo-secret created
deployment.apps/mongodb-deployment created
service/mongodb-service created
configmap/mongo-configmap created
deployment.apps/mongo-express created
service/mongo-express-service created
```
You can verify the setup by running the following command:
```zsh
~ kubectl -n mongo get pods
NAME                                  READY   STATUS    RESTARTS   AGE
mongo-express-5b7f8b797f-xzztl        1/1     Running   0          3m8s
mongodb-deployment-56fd9c6bb6-dt8nx   1/1     Running   0          3m9s
```

#### Set up the Redis
```zsh
~ kubectl apply -f k8s/redis/redis.yml
namespace/redis created
deployment.apps/redis-master created
service/redis-master created
```
You can verify the setup by running the following command:
```zsh
~ kubectl -n redis get pods
NAME                            READY   STATUS    RESTARTS   AGE
redis-master-6d66bd4cd4-q8j48   1/1     Running   0          89s
```