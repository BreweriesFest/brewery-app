# Beer Service

## Description

The Beer Service is a microservice that provides basic information about beers, including details such as name, style,
and alcohol content. It uses GraphQL for data retrieval and allows for the creation of new beers. The service integrates
with the Inventory Service, Scheduler, and Order Service to manage beer inventory levels and validate beer availability.

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
- Reactive programming

## Getting Started

To run this service, you'll need to set up a local development environment as described in [Environment Setup Guide](./docker/README.md).

Once you've set up your environment, follow these steps to get started:

1. Navigate to the `beer-service` directory
2. Start the service locally using the command <insert command here>
3. Interact with the service using GraphQL queries and mutations
