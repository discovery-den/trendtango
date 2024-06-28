# Event-Based Microservice Architecture Project - TrendTango part 1

## Project Description

This project demonstrates an event-based microservice architecture designed to manage stock and news data. It is divided into two main components:

1. **Scheduler Application**: This application runs a scheduler that pulls stock and news data from a finance API, stores the data in a Google Cloud Storage (GCS) bucket, and sends notifications to a Kafka topic.
2. **Spark Job**: Triggered by Kafka event messages, this Spark job reads files from the GCS bucket, merges stock data with news, and stores the processed data in a Redis database. The workflow is orchestrated using Airflow (Google Composer).

## Technologies Used
- **Java**
- **Spring Boot**
- **Spring Cloud**
- **Spring Kafka**
- **Apache Spark**

## Resources Utilized
- **Kafka Server**: Free tier of Upstream for setting up a free Kafka server.
- **Finance API**: Free finance API for data retrieval.
- **Google Cloud Platform (GCP)**: Free GCP project account for storage.
- **In-memory DB**: Used to store jobMetadata.

This project leverages various free-tier services to build a scalable and efficient microservice architecture.

## Short Description

This GitHub project showcases an event-driven microservice architecture designed to manage stock and news data. It consists of two main parts: a scheduler application that retrieves data from a finance API, stores it in a Google Cloud Storage (GCS) bucket, and publishes notifications to a Kafka topic. Built using Java with Spring Boot, Spring Cloud, and Spring Kafka, the project utilizes free-tier services like Upstream for Kafka hosting and a free finance API, along with a Google Cloud Platform (GCP) free project account for storage.
