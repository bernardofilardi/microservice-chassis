# 📦 Microservice Chassis &amp; Archetype 

> **Production-Ready Spring Boot 3 Archetype** construído sobre **Java 21 (Virtual Threads)**, **Arquitetura Hexagonal (Ports &amp; Adapters)** e **Observabilidade com OpenTelemetry**. Projetado como um *chassis* corporativo para a criação de microsserviços escaláveis, manuteníveis e resilientes. 

--- 

## 📐 Visão Geral de Arquitetura 

Este *chassis* impõe o desacoplamento de responsabilidades através da **Arquitetura Hexagonal (Ports &amp; Adapters)**. O núcleo do domínio de negócio permanece isolado de frameworks, bancos de dados e correias de mensageria, garantindo alta manutenibilidade, evolutividade e facilidade de testes automatizados. 

```mermaid 
graph TD 
    subgraph "Infrastructure Layer (Adapters)" 
        WebAPI[REST Controller / Spring MVC] 
        KafkaAdapter[Kafka Consumer / Producer] 
        DBAdapter[PostgreSQL / Spring Data JPA] 
    end 
    
    subgraph "Application Layer (Ports)" 
        InPort[Inbound Ports / Use Cases] 
        OutPort[Outbound Ports / Repository Interfaces] 
    end 
    
    subgraph "Domain Layer (Core)" 
        DomainModel[Entities &amp; Business Rules] 
    end 
    
    WebAPI -->|Calls| InPort 
    KafkaAdapter -->|Calls| InPort 
    InPort --> DomainModel 
    DomainModel --> OutPort 
    DBAdapter -->|Implements| OutPort