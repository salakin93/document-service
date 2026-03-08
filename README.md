# Document Service

Microservicio encargado de la **gestión de documentos PDF** dentro de la plataforma.
Este servicio permite almacenar archivos, registrar su metadata, buscarlos y descargarlos.

Forma parte de una arquitectura de **microservicios basada en Spring Boot, Spring Cloud, Eureka y API Gateway**.

---

# Arquitectura del sistema

Este servicio funciona dentro del siguiente ecosistema:

```
Cliente
   │
   ▼
Gateway Service (API Gateway)
   │
   ▼
Document Service
   │
   ▼
PostgreSQL (document_db)
```

Servicios relacionados:

* **config-service** → configuración centralizada
* **discovery-service (Eureka)** → registro de servicios
* **gateway-service** → punto de entrada del sistema
* **identity-service** → autenticación y generación de JWT
* **document-service** → gestión de documentos

---

# Responsabilidades del servicio

El `document-service` tiene las siguientes responsabilidades:

* subir archivos PDF
* almacenar metadata del documento
* permitir búsquedas por filtros
* permitir descarga de documentos
* validar autenticación mediante JWT
* registrar el servicio en Eureka
* consumir configuración desde Config Server

---

# Tecnologías utilizadas

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* OAuth2 Resource Server (JWT)
* PostgreSQL
* Spring Cloud Config
* Eureka Client
* Lombok
* Gradle

---

# Estructura del proyecto

```
src/main/java/edu/usip/document

api/
  DocumentController.java
  dto/
    request/
      DocumentUploadRequest.java
    response/
      DocumentResponse.java
  error/
    ApiErrorResponse.java
    GlobalExceptionHandler.java

domain/
  Document.java

repo/
  DocumentRepository.java
  DocumentSpecification.java

service/
  DocumentService.java
  FileStorageService.java

storage/
  LocalFileStorageService.java

security/
  SecurityConfig.java
```

---

# Base de datos

El servicio utiliza PostgreSQL.

Tabla principal:

```
documents
```

Campos principales:

| campo        | descripción                 |
| ------------ | --------------------------- |
| id           | identificador del documento |
| title        | título                      |
| author       | autor                       |
| degree       | carrera                     |
| defense_date | fecha de defensa            |
| source_id    | identificador externo       |
| file_name    | nombre del archivo          |
| storage_path | ubicación del archivo       |
| size         | tamaño                      |
| created_by   | usuario que lo creó         |
| created_at   | fecha de creación           |
| active       | estado                      |

---

# Configuración

La configuración se obtiene desde el **Config Server**.

Repositorio:

```
microservices-config
```

Archivo:

```
document-service.properties
```

Ejemplo:

```
server.port=8082
spring.application.name=document-service

eureka.client.service-url.defaultZone=http://localhost:8761/eureka

spring.datasource.url=jdbc:postgresql://localhost:5435/document_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

management.endpoints.web.exposure.include=health,info

file.storage.path=uploads

spring.servlet.multipart.max-file-size=35MB
spring.servlet.multipart.max-request-size=35MB

security.jwt.public-key-path=public.pem
```

---

# Seguridad

El servicio utiliza **JWT firmado por `identity-service`**.

`document-service` **no genera tokens**, solo los valida.

Por lo tanto necesita únicamente:

```
public.pem
```

Este archivo debe estar en la raíz del proyecto.

---

# Endpoints

## Subir documento

```
POST /v1/documents
```

Permisos:

```
ROLE_ADMIN
```

Ejemplo:

```
curl --location 'http://localhost:8080/api/documents' \
--header 'Authorization: Bearer TOKEN' \
--form 'title="Documento de prueba"' \
--form 'author="Alejandro Salazar"' \
--form 'degree="Ingeniería de Sistemas"' \
--form 'defenseDate="2024-11-15"' \
--form 'file=@"/ruta/documento.pdf"'
```

---

## Listar documentos

```
GET /v1/documents
```

Filtros disponibles:

```
title
author
degree
page
size
```

Ejemplo:

```
GET /api/documents?title=sistemas
```

---

## Obtener documento

```
GET /v1/documents/{id}
```

---

## Descargar documento

```
GET /v1/documents/{id}/download
```

Ejemplo:

```
curl --location \
'http://localhost:8080/api/documents/1/download' \
--header 'Authorization: Bearer TOKEN'
```

---

# Integración con Gateway

Las rutas públicas se exponen mediante `gateway-service`.

Configuración:

```
/api/documents
```

se redirige a:

```
/v1/documents
```

---

# Registro en Eureka

El servicio se registra automáticamente en:

```
http://localhost:8761
```

Aparecerá como:

```
DOCUMENT-SERVICE
```

---

# Docker

Dockerfile incluido en el proyecto.

Construir imagen:

```
docker build -t document-service .
```

Ejecutar contenedor:

```
docker run -p 8082:8082 document-service
```

---

# Flujo de subida de documento

```
Cliente
  │
  ▼
Gateway
  │
  ▼
Document Service
  │
  ├─ guarda archivo en storage
  │
  └─ guarda metadata en PostgreSQL
```
