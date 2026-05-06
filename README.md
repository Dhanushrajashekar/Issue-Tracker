# Issue Tracker

A full-stack issue management system built with **Java Spring Boot** and **Bootstrap 5**.

## Tech Stack

| Layer        | Technology                                      |
|--------------|-------------------------------------------------|
| Backend      | Java 21, Spring Boot 3.2, Spring Security       |
| Auth         | JWT (jjwt 0.12), BCrypt                         |
| Database     | MySQL 8 + Spring Data JPA (Hibernate)           |
| Real-time    | WebSocket + STOMP (SockJS)                      |
| Email        | Spring Mail (Gmail SMTP)                        |
| File Storage | Local disk (dev) → AWS S3 (prod)                |
| Frontend     | HTML5, Bootstrap 5, Vanilla JS                  |
| Server       | Nginx (reverse proxy)                           |
| Cloud        | AWS EC2 + Route 53                              |

---

## Project Structure

```
issue-tracker/
├── backend/                  ← Spring Boot Maven project
│   └── src/main/java/com/issuetracker/
│       ├── model/            ← JPA entities (User, Project, Issue, Comment, Attachment, Notification)
│       │   └── enums/        ← Role, IssueStatus, IssuePriority, IssueType
│       ├── repository/       ← Spring Data JPA repositories
│       ├── dto/              ← Request/response data transfer objects
│       ├── security/         ← JWT service + filter + UserDetailsService
│       ├── config/           ← SecurityConfig, WebSocketConfig
│       ├── service/          ← Business logic layer
│       └── controller/       ← REST API controllers
├── frontend/                 ← Static HTML + CSS + JS
│   ├── css/style.css
│   ├── js/
│   │   ├── config.js         ← API base URL + shared helpers
│   │   ├── websocket.js      ← WebSocket/STOMP connection + toast notifications
│   │   ├── dashboard.js
│   │   ├── projects.js
│   │   ├── project-detail.js
│   │   ├── issue-detail.js
│   │   ├── my-issues.js
│   │   └── admin.js
│   ├── index.html            ← Login
│   ├── register.html
│   ├── activate.html         ← Email activation landing page
│   ├── forgot-password.html
│   ├── reset-password.html
│   ├── dashboard.html
│   ├── projects.html
│   ├── project-detail.html   ← Issues list with filters
│   ├── issue-detail.html     ← Full issue view: comments, attachments, watchers
│   ├── my-issues.html        ← Assigned to me / Reported by me
│   └── admin.html            ← User management (Admin only)
└── nginx/nginx.conf          ← Nginx reverse proxy config
```

---

## Roles & Permissions

| Action                        | Reporter | Developer | Admin |
|-------------------------------|----------|-----------|-------|
| Register / Login              | ✅        | ✅         | ✅     |
| View projects they belong to  | ✅        | ✅         | ✅     |
| Create projects               | ✅        | ✅         | ✅     |
| Create / comment on issues    | ✅        | ✅         | ✅     |
| Upload attachments            | ✅        | ✅         | ✅     |
| Watch issues                  | ✅        | ✅         | ✅     |
| Update issue status/assignee  | ✅        | ✅         | ✅     |
| Delete own issues/comments    | ✅        | ✅         | ✅     |
| Delete any issue/comment      | ❌        | ❌         | ✅     |
| View all users                | ❌        | ❌         | ✅     |
| Change user roles             | ❌        | ❌         | ✅     |

> New accounts default to **Developer** role. An Admin can promote or demote any user.

---

## Local Development Setup

### Prerequisites
- Java 21+
- Maven 3.8+
- MySQL 8.0+ *(not needed for dev — H2 in-memory is used instead)*

### 1 — Clone and navigate

```bash
cd issue-tracker/backend
```

### 2 — Run in dev mode (H2 in-memory, no MySQL needed)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend starts at **http://localhost:8080**

- H2 console at **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:issue_tracker` · Username: `sa` · Password: *(empty)*
- Accounts auto-activate in dev — no email verification required

### 3 — Serve the frontend

```bash
# Option A: Node.js serve (recommended)
npx serve frontend -l 3000

# Option B: Python
cd frontend
python -m http.server 3000
```

Frontend at **http://localhost:3000**

### 4 — Register and log in

1. Go to `http://localhost:3000/register.html`
2. Fill in name, email, password → click **Create account**
3. You are redirected to login — sign in immediately (no email needed in dev)
4. Create a project → add issues → invite teammates by email

---

## Production Setup (MySQL)

### 1 — Configure `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/issue_tracker?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=issueuser
spring.datasource.password=yourpassword

jwt.secret=myIssueTrackerSecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmSecurity2024!

spring.mail.username=your-gmail@gmail.com
spring.mail.password=your-gmail-app-password

app.frontend.url=https://yourdomain.com
app.upload.dir=/var/uploads/issue-tracker
```

### 2 — Create the MySQL database

```sql
CREATE DATABASE issue_tracker;
CREATE USER 'issueuser'@'localhost' IDENTIFIED BY 'yourpassword';
GRANT ALL PRIVILEGES ON issue_tracker.* TO 'issueuser'@'localhost';
FLUSH PRIVILEGES;
```

### 3 — Build and run

```bash
mvn clean package -DskipTests
java -jar target/issue-tracker-1.0.0.jar
```

---

## AWS EC2 Deployment

### Step 1 — Launch EC2 instance

- **AMI**: Ubuntu 22.04 LTS
- **Instance type**: t2.micro (free tier) or t3.small
- **Security Group inbound rules**:
  - Port 22 (SSH) — your IP only
  - Port 80 (HTTP) — 0.0.0.0/0
  - Port 443 (HTTPS) — 0.0.0.0/0

### Step 2 — Install dependencies

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y openjdk-21-jdk mysql-server nginx maven
```

### Step 3 — Set up MySQL

```bash
sudo mysql -u root -p
```
```sql
CREATE DATABASE issue_tracker;
CREATE USER 'issueuser'@'localhost' IDENTIFIED BY 'StrongPassword123!';
GRANT ALL PRIVILEGES ON issue_tracker.* TO 'issueuser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### Step 4 — Upload and build

```bash
# On your local machine
scp -i your-key.pem -r backend/ ubuntu@your-ec2-ip:/home/ubuntu/issue-tracker/

# On EC2
cd /home/ubuntu/issue-tracker/backend
mvn clean package -DskipTests
```

### Step 5 — Run as a system service

```bash
sudo nano /etc/systemd/system/issue-tracker.service
```

```ini
[Unit]
Description=Issue Tracker Spring Boot Application
After=network.target mysql.service

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/issue-tracker/backend
ExecStart=/usr/bin/java -jar target/issue-tracker-1.0.0.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable issue-tracker
sudo systemctl start issue-tracker
sudo journalctl -u issue-tracker -f   # live logs
```

### Step 6 — Deploy the frontend

```bash
sudo mkdir -p /var/www/issue-tracker/frontend
sudo cp -r /home/ubuntu/issue-tracker/frontend/* /var/www/issue-tracker/frontend/

# Set API_BASE to empty string so Nginx proxies it
sudo nano /var/www/issue-tracker/frontend/js/config.js
# Change: const API_BASE = '';
```

### Step 7 — Configure Nginx

```bash
sudo nano /etc/nginx/sites-available/issue-tracker
```

```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    # Serve frontend static files
    root /var/www/issue-tracker/frontend;
    index index.html;
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy API requests to Spring Boot
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Proxy WebSocket connections
    location /ws/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 3600s;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/issue-tracker /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### Step 8 — HTTPS with Let's Encrypt

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

---

## API Reference

### Auth
| Method | Endpoint                        | Auth | Description                  |
|--------|---------------------------------|------|------------------------------|
| POST   | `/api/auth/register`            | No   | Create account               |
| POST   | `/api/auth/login`               | No   | Login → returns JWT token    |
| GET    | `/api/auth/activate?token=`     | No   | Activate account via email   |
| POST   | `/api/auth/forgot-password`     | No   | Send password reset email    |
| POST   | `/api/auth/reset-password`      | No   | Reset password with token    |
| GET    | `/api/auth/me`                  | Yes  | Get current user             |

### Projects
| Method | Endpoint                          | Auth | Description               |
|--------|-----------------------------------|------|---------------------------|
| GET    | `/api/projects`                   | Yes  | List my projects          |
| POST   | `/api/projects`                   | Yes  | Create project            |
| GET    | `/api/projects/{id}`              | Yes  | Get project + members     |
| POST   | `/api/projects/{id}/members`      | Yes  | Add member by email       |
| DELETE | `/api/projects/{id}/members/{uid}`| Yes  | Remove member             |

### Issues
| Method | Endpoint                        | Auth | Description                    |
|--------|---------------------------------|------|--------------------------------|
| POST   | `/api/issues`                   | Yes  | Create issue                   |
| GET    | `/api/issues/{id}`              | Yes  | Get issue details              |
| PUT    | `/api/issues/{id}`              | Yes  | Update status/priority/assignee|
| DELETE | `/api/issues/{id}`              | Yes  | Delete issue                   |
| GET    | `/api/issues/project/{id}`      | Yes  | List issues in a project       |
| GET    | `/api/issues/my/assigned`       | Yes  | Issues assigned to me          |
| GET    | `/api/issues/my/reported`       | Yes  | Issues I reported              |
| POST   | `/api/issues/{id}/watch`        | Yes  | Watch an issue                 |
| DELETE | `/api/issues/{id}/watch`        | Yes  | Unwatch an issue               |

### Comments & Attachments
| Method | Endpoint                            | Auth | Description              |
|--------|-------------------------------------|------|--------------------------|
| GET    | `/api/issues/{id}/comments`         | Yes  | List comments            |
| POST   | `/api/issues/{id}/comments`         | Yes  | Add comment              |
| DELETE | `/api/comments/{id}`                | Yes  | Delete comment           |
| GET    | `/api/issues/{id}/attachments`      | Yes  | List attachments         |
| POST   | `/api/issues/{id}/attachments`      | Yes  | Upload file              |
| DELETE | `/api/attachments/{id}`             | Yes  | Delete attachment        |
| GET    | `/api/files/{storedName}`           | No   | Download file            |

### Notifications
| Method | Endpoint                        | Auth | Description              |
|--------|---------------------------------|------|--------------------------|
| GET    | `/api/notifications`            | Yes  | Get all + unread count   |
| PUT    | `/api/notifications/{id}/read`  | Yes  | Mark one as read         |
| PUT    | `/api/notifications/read-all`   | Yes  | Mark all as read         |

### Admin (ROLE_ADMIN only)
| Method | Endpoint                        | Auth  | Description              |
|--------|---------------------------------|-------|--------------------------|
| GET    | `/api/admin/users`              | Admin | List all users           |
| PUT    | `/api/admin/users/{id}/role`    | Admin | Change a user's role     |

---

## How JWT Authentication Works

```
1. POST /api/auth/login  →  { email, password }
2. Server verifies password against BCrypt hash
3. Server creates JWT: header.payload.signature
   - payload: { sub: "email", iat: now, exp: now+24h }
   - signature: HMAC-SHA512(header+payload, secretKey)
4. Client stores JWT in localStorage
5. Every request sends: Authorization: Bearer <token>
6. JwtFilter validates signature + expiry
7. Sets Spring Security context → controllers see Authentication object
```

## How Real-time Notifications Work

```
1. User logs in → gets JWT
2. Frontend connects to /ws with SockJS
3. STOMP CONNECT frame carries JWT in Authorization header
4. ChannelInterceptor validates JWT → sets session principal
5. Frontend subscribes to /user/queue/notifications
6. When issue updated → NotificationService.notifyWatchers()
7. SimpMessagingTemplate.convertAndSendToUser(email, "/queue/notifications", notification)
8. STOMP routes to that user's WebSocket session → browser receives it instantly
9. Frontend shows a toast popup and updates the badge count
```

## How the Watch Mechanism Works

```
1. User clicks "Watch" on an issue
2. POST /api/issues/{id}/watch → user added to issue_watchers table
3. Reporter auto-watches their own issue on creation
4. Commenters auto-watch issues they comment on
5. On any update (status, comment, assignee) → all watchers notified
6. Watcher who triggered the action is excluded from their own notification
7. User clicks "Watching" to unwatch → DELETE /api/issues/{id}/watch
```
