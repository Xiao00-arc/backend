# Admin & Manager Setup + Email Notifications

## Part 1: Create Admin and Manager Users

Run these SQL scripts in your Render PostgreSQL database:

### 1. Connect to Your Render Database

1. Go to your Render dashboard
2. Click on your PostgreSQL database
3. Click **"Connect"** → Copy the **External Connection String**
4. Use a PostgreSQL client (pgAdmin, DBeaver, or psql command line)

### 2. Run These SQL Scripts

```sql
-- Create Admin User
-- Username: admin
-- Password: admin123
INSERT INTO users (username, email, password, role, employee_id, department_id, manager_id)
VALUES (
    'admin', 
    'admin@yourcompany.com', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCy', 
    'ADMIN', 
    'EMP-ADMIN-001', 
    NULL, 
    NULL
);

-- Create Manager User
-- Username: manager
-- Password: manager123
INSERT INTO users (username, email, password, role, employee_id, department_id, manager_id)
VALUES (
    'manager', 
    'manager@yourcompany.com', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
    'MANAGER', 
    'EMP-MGR-001', 
    NULL, 
    NULL
);
```

### 3. Verify Users Were Created

```sql
SELECT id, username, email, role, employee_id FROM users;
```

---

## Part 2: Email Notification Setup

### ✅ What's Been Configured

1. **Email Service**: Already configured with Gmail SMTP
   - Host: smtp.gmail.com
   - From: zenosvalkyre@gmail.com
   - Admin notifications: zenosvalkyre@gmail.com

2. **New Expense Notifications**: 
   - When ANY user creates a new expense, an email is sent to the admin
   - Email includes: submitter name, amount, description, date, status

3. **Manager Approval Notifications**:
   - When expense is $100 or more, manager gets notified
   - Only if the employee has a manager assigned

### Email Notification Features

**Admin Email on New Expense:**
```
Subject: New Expense Submitted - [Description]

A new expense has been submitted:

Submitted by: [username] ([email])
Description: [description]
Amount: $[amount]
Date: [date]
Status: PENDING

Please review this expense in the system.
```

**Manager Email for Approval:**
```
Subject: New Expense Approval Request

Hello [Manager Name],

A new expense claim for $[amount] submitted by [employee] is awaiting your approval.

Description: [description]

Please log in to the system to review it.
```

---

## Part 3: Environment Variables on Render

Add this environment variable to your Render backend:

```
ADMIN_EMAIL=zenosvalkyre@gmail.com
```

**Steps:**
1. Go to Render Dashboard → Your Backend Service
2. Click **"Environment"** tab
3. Add new variable:
   - Key: `ADMIN_EMAIL`
   - Value: `zenosvalkyre@gmail.com`
4. Click **"Save Changes"**

---

## Testing Email Notifications

1. **Login** to your application (use any employee account)
2. **Create a new expense** through the frontend
3. **Check your email** (zenosvalkyre@gmail.com) - you should receive a notification

### Troubleshooting

If emails are not being sent:

1. **Check Render Logs** for email errors
2. **Verify Gmail App Password** is correct in application.properties
3. **Check spam folder** in your email
4. **Verify email configuration**:
   ```properties
   spring.mail.username=zenosvalkyre@gmail.com
   spring.mail.password=dnlz dijm uuub xhpw
   ```

---

## User Credentials Summary

| Username | Password    | Role    | Email                    |
|----------|-------------|---------|--------------------------|
| admin    | admin123    | ADMIN   | admin@yourcompany.com    |
| manager  | manager123  | MANAGER | manager@yourcompany.com  |

---

## Next Steps

1. ✅ Run the SQL scripts to create admin and manager users
2. ✅ Deploy the updated backend to Render (git push)
3. ✅ Add ADMIN_EMAIL environment variable on Render
4. ✅ Test by creating a new expense and checking email
5. 📧 Update admin email addresses in SQL if needed

**Note:** The email notification is sent asynchronously, so it won't slow down the expense creation process.
