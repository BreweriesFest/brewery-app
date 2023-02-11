# Beer Service

## Description
The Beer Service is a microservice that provides basic information about beers, including details such as name, style, and alcohol content. It uses GraphQL for data retrieval and allows for the creation of new beers. The service integrates with the Inventory Service, Scheduler, and Order Service to manage beer inventory levels and validate beer availability.

## Features
- Retrieve information about beers using GraphQL queries
- Create new beers using GraphQL mutations
- Validate beer availability based on data from the Order Service
- Trigger restocking of beers through the Inventory Service by consuming events triggered by the Scheduler
- Retrieve inventory information when requested through GraphQL

## Technologies Used
- Kubernetes for deployment
- GraphQL for data retrieval and creation
- Kafka for event-driven communication between microservices
- Redis for caching of beer details (as they are static)
- MongoDB for data persistence
- Reactive programming for implementation

## Getting Started
To run this service, you'll need to set up a local development environment with the following tools:
- [Docker](https://www.docker.com/)
- [Kubernetes](https://kubernetes.io/) using [Kind](https://kind.sigs.k8s.io/)
- [GraphQL](https://graphql.org/)
- [Kafka](https://kafka.apache.org/)
- [Redis](https://redis.io/)
- [MongoDB](https://www.mongodb.com/)

Once you've installed the required tools, follow these steps to get started:
1. Clone the repository
2. Navigate to the `beer-service` directory
3. Deploy the required dependencies for the service using Docker and Kind
4. Start the service locally using the command `<insert command here>`
5. Interact with the service using GraphQL queries and mutations

## Environment Setup
Here's a more detailed guide for setting up the environment:
1. Install Docker and Kind on your local machine
2. Clone the repository
3. Navigate to the `beer-service` directory
4. Deploy the required dependencies (Kafka, Redis, MongoDB) using Docker and Kind
5. Start the service locally using the command `<insert command here>`
