# KMA – Developer Setup Guide

### 1. Prerequisites

Before running the project, make sure you have installed:

- Docker & Docker Compose (for containers)
- Node.js 20+ (for local frontend development)
- Java 21 (for local backend development)

### 2. Clone the repository

Open a terminal (Command Prompt) in your desired folder. Then run:

```bash
git clone https://github.com/mazentoraa/KMA.git
cd KMA
```

### 3. Environment setup

*Set up your environment variables for database access:*

```
cp .env.example .env
```

*Then update the values in **.env** with your actual database credentials.*

### 4. First-Time Setup Using Docker

1. From the project root folder, run:

    ```docker-compose up --build```

   - This builds Docker images for backend, frontend, and PostgreSQL.
   - The first build may take a few minutes.

2. Access the services:
   - Frontend: http://localhost:3000
   - Backend: http://localhost:8080
   - PostgreSQL: localhost:5432

Congrats ! Your project is now running.

If you see the test items displayed on the frontend page, it confirms that the frontend, backend, and database are successfully connected!


- For subsequent runs, you can simply run:
   
  ```docker-compose up```

  or start the containers from Docker Desktop.

- If you made code changes and need to rebuild the images, run:

  ```docker-compose up --build```
  

### 5. Branch & Commit Rules

*Branch Naming Conventions*

```
<DEV_PREFIX>/<branch-type>/<short-description>
```

#### Examples:

- `MT/feat/<short-feature-name>`

- `MT/fix/<bug-name>`

- `MT/refactor/<section>`

- `MT/docs/<topic>`



#### Follow commit format:



- `feat(user): add transaction endpoint`

- `fix(login): correct login redirect`

- `chore(config): update Docker config`


### 6. Troubleshooting


If containers fail for some reason:

```
docker-compose down -v
docker-compose up --build
```
