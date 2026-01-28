# GoCode – Final Project 🎓

GoCode is a mobile learning platform that enables students to practice programming directly from an Android application.  
The system supports writing, linting, running code, and receiving short AI-generated hints when execution fails.

---

## 📌 Project Overview
The goal of this project is to provide an interactive coding environment that combines:
- Real code execution
- Immediate feedback
- Guided learning through AI-based hints

The system is designed with a clear separation between the client (Android application) and the server (execution and analysis services).

---

## 🧱 System Architecture

The system is built using a **Client–Server architecture** and consists of four main components:

1. **Android Application (Client)**
2. **Dockerized Execution API (Server – Railway)**
3. **Java Runtime Environment (OpenJDK 17)**
4. **AI Hint Service**

### High-Level Flow
User → Android App → Docker API → (Java / AI) → Docker API → Android App

---

## 1️⃣ Android Application
The Android application is responsible for the user interface and interaction only.

### Responsibilities:
- Code editor for writing Java code
- Input field for standard input (stdin)
- Sending requests to the server (`/lint`, `/run`, `/hint`)
- Displaying output, errors, and AI hints

The application does **not** execute code locally and does **not** contain any sensitive credentials.

---

## 2️⃣ Execution API (FastAPI + Docker + Railway)
The server is implemented using **FastAPI** and runs inside a **Docker container** deployed on **Railway**.

### Responsibilities:
- Receive code from the Android client
- Perform linting and execution
- Enforce execution timeouts
- Return structured results (JSON)
- Communicate with the AI service when needed

Docker ensures:
- Environment isolation
- Consistent behavior across development and production
- Secure execution of untrusted code

---

## 3️⃣ Java Runtime (OpenJDK 17)
Java code execution is handled entirely on the server.

### Execution Process:
1. **Compilation** using `javac`
2. **Execution** using `java`
3. Capture:
   - Standard output
   - Standard error
   - Exit code

This separation allows precise error detection and safe execution.

---

## 4️⃣ AI Hint Service
When code execution fails or produces incorrect results, the server sends contextual information to an external AI service.

### Input to AI:
- Task description
- User code
- Execution output / error
- Exit code

### Output:
- A **short hint** guiding the user toward the solution  
- No full solution is provided to preserve the learning process

The AI is accessed **only from the server**, ensuring API key security.

---

## 🔌 API Endpoints

Base URL (local): `http://localhost:8080`

- `GET /health`  
  Checks server availability

- `POST /lint`  
  Compiles Java code and returns syntax errors with line information

- `POST /run`  
  Compiles and executes Java code with optional standard input

- `POST /hint`  
  Generates a short AI-based hint when execution fails

---

## 🧰 Technologies Used

### Client
- Kotlin
- Android SDK
- Sora CodeEditor

### Server
- Python 3.11
- FastAPI
- Uvicorn
- OpenJDK 17
- OpenAI API

### Deployment
- Docker
- Railway

---


---

## ▶️ Running the Server Locally

```bash
docker build -t gocode-server .
docker run --rm -p 8080:8080 gocode-server
```

# Swagger UI: http://localhost:8080/docs


## 🤖 Enabling AI Hints
```bash
docker run --rm -p 8080:8080 \
  -e OPENAI_API_KEY="sk-XXXX" \
  gocode-server
```



###🔒 Security Considerations

- Code execution is isolated inside Docker containers

- Execution timeouts prevent infinite loops

- OpenAI API keys are never exposed to the client

- All communication is performed over HTTP APIs


###🗺️ Future Work

- Output validation against expected results

- Support for additional programming languages

- Exercise management system

- User progress tracking

- Arena compete

###👤 Authors

- [@Ben-Aharoni](https://github.com/Ben-Aharoni)
- [@Amit-Halaly](https://github.com/Amit-Halaly)
Final Project – Software Engineering

###📄 License

Academic use only


