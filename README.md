<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Expense Manager - README</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background: #f9f9f9; color: #333; line-height: 1.7; }
        h1 { color: #2c3e50; }
        h2 { color: #3498db; border-bottom: 3px solid #3498db; padding-bottom: 10px; margin-top: 30px; }
        h3 { color: #2980b9; }
        table { border-collapse: collapse; width: 100%; margin: 15px 0; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
        th { background-color: #f2f2f2; }
        pre { background: #f4f4f4; padding: 15px; border-radius: 5px; overflow-x: auto; border-left: 5px solid #ccc; }
        .note { background: #fff3cd; padding: 15px; border-left: 6px solid #ffc107; margin: 20px 0; }
        .warning { background: #ffebee; padding: 15px; border-left: 6px solid #f44336; margin: 20px 0; }
        .badges img { margin-right: 8px; height: 28px; }
        code { background: #eee; padding: 2px 4px; border-radius: 3px; font-family: monospace; }
    </style>
</head>
<body>

    <div class="badges">
    <img src="https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk" alt="Java 21">
    <img src="https://img.shields.io/badge/Spring%20Boot-3.4-green?style=for-the-badge&logo=springboot" alt="Spring Boot 3.4">
    <img src="https://img.shields.io/badge/PostgreSQL-Database-blue?style=for-the-badge&logo=postgresql" alt="PostgreSQL">
    <img src="https://img.shields.io/badge/RabbitMQ-Messaging-orange?style=for-the-badge&logo=rabbitmq" alt="RabbitMQ">
    <img src="https://img.shields.io/badge/Vite-Frontend-yellow?style=for-the-badge&logo=vite" alt="Vite">
    <img src="https://img.shields.io/badge/Nginx-Reverse%20Proxy-green?style=for-the-badge&logo=nginx" alt="Nginx">
    <img src="https://img.shields.io/badge/Docker-Containerized-blue?style=for-the-badge&logo=docker" alt="Docker">
    <img src="https://img.shields.io/badge/AWS-EC2-orange?style=for-the-badge&logo=amazonaws" alt="AWS EC2">
</div>

    <h1>Expense Manager</h1>
    <h2>📌 What is this project?</h2>
    <p>
        A full-stack expense tracking system where: 
    </p>

<ul>
    <li>Users manage expenses in a web UI</li>
    <li>Backend handles authentication, storage, and business logic</li>
    <li>System runs in multiple environments (dev / docker / production)</li>
    <li>Infrastructure is fully containerized with Docker</li>
</ul>

    <h2>Prerequisites</h2>
    <ul>
        <li>Git</li>
        <li>Docker (version 20+)</li>
        <li>Docker Compose (v2+)</li>
        <li><em>No need to install Java, Node.js, PostgreSQL, or anything else locally.</em></li>
    </ul>

    <h2>Table of Contents</h2>
    <ul>
        <li><a href="#about">About the Project</a></li>
        <li><a href="#features">Features</a></li>
        <li><a href="#backend">Backend</a></li>
        <li><a href="#frontend">Frontend</a></li>
        <li><a href="#env">Environment Configuration</a></li>
        <li><a href="#deployment">Deployment & Infrastructure</a></li>
        <li><a href="#quickstart">Quick Start with Docker</a></li>
        <li><a href="#devsetup">Development Setup</a></li>
        <li><a href="#api">API Documentation</a></li>
    </ul>

   

    <h2 id="features">Features</h2>
    <ul>
        <li>User registration, login, and account management</li>
        <li>Add, edit, delete, and view expenses</li>
        <li>Basic report generation</li>
        <li>File upload support</li>
        <li>Real-time updates using WebSocket</li>
    </ul>

    <h2 id="backend">Backend</h2>
    <p>The backend is built with Spring Boot. Here are some things I implemented:</p>
    <ul>
        <li><strong>Liquibase</strong> for database schema updates and versioning</li>
        <li><strong>Spring Data JPA</strong> + Hibernate for database operations</li>
        <li><strong>JWT</strong> for authentication and authorization</li>
        <li><strong>RabbitMQ</strong> for background processing</li>
        <li><strong>Rate Limiting</strong> on important endpoints</li>
        <li><strong>Spring Actuator</strong> for basic monitoring</li>
        <li><strong>Access logs</strong> enabled via Tomcat</li>
        <li><strong>Caching</strong> using Spring Cache + Caffeine (in-memory)</li>
    </ul>

    <h2 id="frontend">Frontend</h2>
    <p>The frontend is built with Vite and served using Nginx as a reverse proxy.</p>
    <ul>
        <li><strong>Multi-stage Docker build</strong> for optimized image size</li>
        <li>Uses <strong>Alpine-based Nginx image</strong></li>
        <li><code>.dockerignore</code> file to reduce build context and speed up builds</li>
        <li>Separate environment files (<code>.env.dev</code>, <code>.env.mobile</code>, <code>.env.prod</code>)</li>
        <li>Configured <strong>Nginx as reverse proxy</strong> to forward requests to backend</li>
    </ul>

    <h2 id="env">Environment Configuration</h2>
    <h3>Backend Spring Profiles</h3>
    <p>The backend uses three configuration files:</p>
    <ul>
        <li><code>application.yml</code> — Common settings (applies to all profiles)</li>
        <li><code>application-dev.yml</code> — Development settings (detailed logs, full actuator, etc.)</li>
        <li><code>application-prod.yml</code> — Production settings (optimized pool, minimal logs, secure)</li>
    </ul>

    <h3>1. Environment Variables in Project Root (.env.prod)</h3>
    <pre><code># ==================== Database ====================
POSTGRES_DB=expense_db
POSTGRES_USER=Nikos
POSTGRES_PASSWORD=your_secure_password_here

# ==================== Spring Boot ====================
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/expense_db
SPRING_DATASOURCE_USERNAME=Nikos
SPRING_DATASOURCE_PASSWORD=your_secure_password_here

# ==================== Security ====================
JWT_SECRET=your_strong_jwt_secret_here

# ==================== Cloudinary ====================
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret

# ==================== RabbitMQ ====================
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest</code></pre>
    <div class="warning">
        <strong>Security Note:</strong> Add <code>.env.prod</code> to <code>.gitignore</code>. Never commit real secrets.
    </div>

    <h3>2. Frontend Environment Variables ( frontend/ project folder)</h3>
    <table>
        <tr><th>File</th><th>Use Case</th><th>VITE_API_URL</th><th>VITE_WS_URL</th></tr>
        <tr><td>.env.dev</td><td>Local Desktop</td><td>http://localhost:8080</td><td>ws://localhost:8080/ws-expense-tracker</td></tr>
        <tr><td>.env.mobile</td><td>Mobile/Network</td><td>http://YOUR_LOCAL_IP:8080</td><td>ws://YOUR_LOCAL_IP:8080/ws-expense-tracker</td></tr>
        <tr><td>.env.prod</td><td>Production</td><td>/api</td><td>ws://EC2-public-ip/ws-expense-tracker</td></tr>
    </table>

    <h2 id="deployment">Deployment & Infrastructure</h2>
    <p>Currently running on <strong>AWS EC2 t3.micro</strong> (1GB RAM).</p>
    <p><strong>Resource Optimizations:</strong></p>
    <ul>
        <li>Added swap space</li>
        <li>Set JVM flags: <code>-Xmx512m -Xms256m</code></li>
        <li>Using in-memory cache to reduce load</li>
    </ul>

   <h2 id="running">Running the Application</h2>

<h3>Option 1: Full Docker Setup (Local)</h3>

<p>Runs the complete application stack using Docker:</p>

<ul>
    <li>Frontend (React + Nginx)</li>
    <li>Spring Boot Backend</li>
    <li>PostgreSQL</li>
    <li>RabbitMQ</li>
</ul>

<pre><code>git clone https://github.com/Nikos32-ux/Expense-manager-app.git
cd Expense-manager-app
docker compose up --build</code></pre>

<p><strong>Access:</strong></p>

<ul>
    <li>Frontend → <code>http://localhost</code></li>
    <li>Backend API → <code>http://localhost:8080</code></li>
</ul>

<p><strong>Uses:</strong></p>

<ul>
    <li><code>.env.prod</code></li>
    <li><code>application-prod.yml</code></li>
</ul>

<hr>
    <h3>Option 2: Local Development Setup</h3>
    <p>Runs the backend from IntelliJ and the frontend from the Vite development server.</p>

   <p><strong>Start infrastructure services:</strong></p>

<pre><code>docker compose up -d postgres rabbitmq</code></pre>
    <p>Then in IntelliJ Run Configuration, add these variables:</p>
    <pre><code>SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/expense_db
SPRING_DATASOURCE_USERNAME=Nikos
SPRING_DATASOURCE_PASSWORD=your_secure_password_here
JWT_SECRET=your_strong_jwt_secret_here
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret</code></pre>
    <p>Run the backend from IntelliJ</p>

    <h3>Option 3: Production (AWS EC2)</h3>

<p>
Same Docker setup, deployed on server.
</p>

<pre><code>docker compose up -d --build</code></pre>

<p><strong>Uses:</strong></p>
<ul>
    <li>.env.prod</li>
    <li>same Spring profile as Docker mode</li>
</ul>

    <hr>n 2
    <p><strong>Made with ❤️ by a developer who is constantly learning</strong></p>

</body>
</html>