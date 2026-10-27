# TCP authentication system

University course project for the "Computer Systems and Networks" course.

The task was to build user registration and authentication without using a regular REST client-server flow. The desktop client communicates with the backend over TCP sockets and exchanges JSON messages.

The backend is split into two Spring Boot services:

- **auth-service** — registration, login, access/refresh tokens and roles;
- **user-service** — user profile operations protected by JWT validation.

Each service has its own PostgreSQL database and socket server.

## Why sockets

Using raw TCP sockets was part of the course assignment. The main goal was to work with lower-level client-server communication and build the authentication flow on top of it instead of relying on HTTP controllers.

## Features

- user registration and login;
- JWT access and refresh tokens;
- USER and ADMIN roles;
- password hashing;
- token validation in the user service;
- JSON request/response messages over TCP;
- separate auth and profile databases;
- Docker Compose setup for the services and PostgreSQL instances.

## Stack

Java, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL, JWT, TCP sockets, JSON and Docker.

## Running locally

Create the environment file:

```bash
cp .env.example .env
```

Then start the services:

```bash
docker compose up --build
```

Ports:

- auth service — `9090`
- user service — `9091`

The desktop client for this project is kept in a separate repository: `bntu-front-desktop`.

## Project status

Finished as a university course project. The code is kept as-is apart from repository cleanup and documentation, because it represents the implementation used for the assignment.
