# Brewery Project

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://github.com/codespaces/new?hide_repo_select=true&ref=main&repo=568351362&machine=standardLinux32gb&devcontainer_path=.devcontainer%2Fdevcontainer.json&location=SouthEastAsia)

![](https://img.shields.io/badge/Java-17-brightgreen)
[![BEER-SERVICE](../../actions/workflows/maven.yml/badge.svg)](../../actions/workflows/service-beer.yml)
[![INVENTORY-SERVICE](../../actions/workflows/maven.yml/badge.svg)](../../actions/workflows/service-inventory.yml)
[![ORDER-SERVICE](../../actions/workflows/maven.yml/badge.svg)](../../actions/workflows/service-order.yml)
[![SCHEDULER](../../actions/workflows/maven.yml/badge.svg)](../../actions/workflows/service-scheduler.yml)

## Overview

This repository contains a mono repo for a brewery project, consisting of multiple submodules including the Beer
Service, Inventory Service, Order Service, and Scheduler. These services use technologies such as Kubernetes, Kafka,
Redis, and MongoDB to manage the brewing and distribution of beer.

## Getting Started

The steps to set up the environment and start the services are common for all submodules. To get started:

1. Clone the repository
2. Follow the instructions in the [Environment Setup Guide](./docker/README.md) to set up the necessary tools and
   dependencies
3. For information on starting a specific service, refer to the `README` file for that service

## Sub-Modules

- [Beer Service](./beer-service/README.md)