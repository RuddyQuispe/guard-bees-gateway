# demo-keycloak-microservice

![Java](https://img.shields.io/badge/Java-25-blue?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-green?logo=spring)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.1-green?logo=spring)
![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?logo=apache-maven)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15.2-336791?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Latest-2496ED?logo=docker)
![Keycloak](https://img.shields.io/badge/Keycloak-26.5+-FF0000?logo=keycloak)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0-6BA539?logo=openapis)
![Swagger](https://img.shields.io/badge/Swagger-UI-85EA2D?logo=swagger)
![Lombok](https://img.shields.io/badge/Lombok-1.18.42-brown)
![MapStruct](https://img.shields.io/badge/MapStruct-1.6.3-blue)
![Spring WebFlux](https://img.shields.io/badge/Spring%20WebFlux-Reactive-green?logo=spring)
![OAuth2](https://img.shields.io/badge/OAuth2-OIDC-informational)
![License](https://img.shields.io/badge/License-MIT-yellow)


## Project Description
This repository hosts a microservices architecture that integrates with Keycloak for authentication and authorization. It includes an API Gateway and two microservices - Admin and Inventory. The overall architecture is designed for scalability, security, and maintainability.

## Architecture Diagram
```
┌──────────────────────────────────────────────────────────────┐
│              API Gateway (Puerto 8090)                        │
│        Spring Cloud Gateway + Spring Security OAuth2          │
│                                                               │
│  Dependencias:                                                │
│  - spring-boot-starter-oauth2-resource-server                │
│  - spring-boot-starter-oauth2-client                         │
│  - spring-boot-starter-security                              │
│  - spring-cloud-starter-gateway-server-webflux               │
│                                                               │
│  Funciones:                                                   │
│  - Validación de Tokens JWT                                  │
│  - Enrutamiento de Requests                                  │
│  - Autorización basada en Roles                              │
└────────────┬───────────────┬──────────────┬──────────────────┘
             │               │              │
        ┌────▼────────┐ ┌───▼────────┐ ┌──▼─────────────┐
        │   Admin      │ │ Inventory  │ │   Keycloak    │
        │  Service     │ │  Service   │ │   Service     │
        │(Puerto 8081) │ │(8082)      │ │  (8080)       │
        │              │ │            │ │               │
        │ - WebFlux    │ │- WebFlux   │ │ - OAuth2/OIDC │
        │ - OpenAPI    │ │- OpenAPI   │ │ - JWT Manager │
        │ - REST API   │ │- REST API  │ │ - User & Role │
        │              │ │            │ │   Management  │
        └──────────────┘ └────────────┘ └──┬────────────┘
                                            │
                                            │ (Relación directa)
                                            │
                        ┌───────────────────▼────────────┐
                        │   PostgreSQL 15.2 (8432)       │
                        │   Base de Datos Keycloak       │
                        │   - Usuarios                   │
                        │   - Roles                      │
                        │   - Permisos                   │
                        │   - Configuración              │
                        └────────────────────────────────┘
```


## Components
- **API Gateway**: Routes requests to the appropriate microservice, handles authentication, and serves as a single entry point for the system.
- **Admin Microservice**: Responsible for managing user data and system administration.
- **Inventory Microservice**: Manages inventory data, allowing for CRUD operations on products.
- **Keycloak v26.5**: Provides identity and access management to the system, handling user authentication and authorization.

## Technology Stack
- **Programming Languages**: Java, JavaScript
- **Frameworks**: Spring Boot, Express.js
- **Database**: PostgreSQL
- **Containerization**: Docker
- **API Gateway**: Kubernetes Ingress or Spring Cloud Gateway
- **Authentication**: Keycloak

## Setup Instructions
1. **Clone the repository**:
   ```bash
   git clone https://github.com/RuddyQuispe/demo-keycloak-microservice.git
   cd demo-keycloak-microservice
