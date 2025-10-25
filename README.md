# Expense Tracker Backend - Deployment Guide

## Local Development with PostgreSQL

### Prerequisites
- Java 21+
- Maven 3.6+
- PostgreSQL 12+

### Setup Local PostgreSQL
1. Install PostgreSQL
2. Create database:
   ```sql
   CREATE DATABASE expense_db;
   CREATE USER postgres WITH ENCRYPTED PASSWORD 'password';
   GRANT ALL PRIVILEGES ON DATABASE expense_db TO postgres;
   ```

3. Update `application.properties` if needed (default settings should work)

4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Deployment on Render

### Method 1: Using render.yaml (Recommended)
1. Push your code to GitHub
2. Connect your GitHub repository to Render
3. Render will automatically use the `render.yaml` configuration
4. Set the following environment variables in Render dashboard:
   - `EMAIL_USERNAME`: Your Gmail address
   - `EMAIL_PASSWORD`: Your Gmail app password
   - `CORS_ALLOWED_ORIGINS`: Your Vercel frontend URL

### Method 2: Manual Setup
1. Create a new Web Service on Render
2. Connect your GitHub repository
3. Use these settings:
   - **Environment**: Java
   - **Build Command**: `chmod +x build.sh && ./build.sh`
   - **Start Command**: `java -Dspring.profiles.active=production -Dserver.port=$PORT -jar target/myProject-0.0.1-SNAPSHOT.jar`
   
4. Add a PostgreSQL database:
   - Create a new PostgreSQL database on Render
   - Copy the connection string

5. Set Environment Variables:
   ```
   DATABASE_URL=<your-render-postgres-connection-string>
   JWT_SECRET_KEY=<generate-a-secure-random-string>
   EMAIL_USERNAME=<your-gmail>
   EMAIL_PASSWORD=<your-gmail-app-password>
   CORS_ALLOWED_ORIGINS=<your-vercel-frontend-url>
   UPLOAD_DIR=/tmp/uploads
   SPRING_PROFILES_ACTIVE=production
   ```

### Important Notes
1. **Database Migration**: The app will automatically create tables on first run
2. **File Uploads**: Files are stored in `/tmp/uploads` (temporary storage on Render)
3. **CORS**: Update `CORS_ALLOWED_ORIGINS` with your actual Vercel URL
4. **JWT Secret**: Generate a strong random string for `JWT_SECRET_KEY`

### Frontend Configuration
Update your frontend API base URL to point to your Render backend:
```javascript
const API_BASE_URL = 'https://your-render-service-name.onrender.com/api';
```

### Health Check
The app exposes a health endpoint at `/actuator/health` for monitoring.

## Environment Variables Reference

| Variable | Description | Example |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection string | `postgresql://user:pass@host:5432/db` |
| `JWT_SECRET_KEY` | Secret key for JWT tokens | `your-super-secret-key-here` |
| `EMAIL_USERNAME` | Gmail address for notifications | `your-email@gmail.com` |
| `EMAIL_PASSWORD` | Gmail app password | `your-app-password` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `https://app.vercel.app` |
| `UPLOAD_DIR` | File upload directory | `/tmp/uploads` |
| `PORT` | Server port (auto-set by Render) | `8080` |

## Troubleshooting

### Common Issues:
1. **Database Connection**: Ensure DATABASE_URL is correctly set
2. **CORS Errors**: Verify CORS_ALLOWED_ORIGINS matches your frontend URL exactly
3. **File Upload Issues**: Check UPLOAD_DIR permissions and disk space
4. **JWT Errors**: Ensure JWT_SECRET_KEY is set and long enough (>32 characters)

### Logs:
Check Render logs for detailed error information:
```bash
# In Render dashboard, go to your service > Logs
```