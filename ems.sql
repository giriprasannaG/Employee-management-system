-- ---------------- DROP EXISTING TABLES ----------------
DROP TABLE IF EXISTS payroll;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS employees;

-- ---------------- DATABASE ----------------
CREATE DATABASE IF NOT EXISTS ems;
USE ems;

-- ---------------- EMPLOYEES ----------------
CREATE TABLE employees (
    emp_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    dept VARCHAR(50),
    designation VARCHAR(50),
    salary DOUBLE NOT NULL,
    manager_id INT,
    FOREIGN KEY (manager_id) REFERENCES employees(emp_id) ON DELETE SET NULL
);

-- ---------------- USERS ----------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    role ENUM('ADMIN','MANAGER','EMPLOYEE') NOT NULL,
    emp_id INT,
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE
);

-- ---------------- ATTENDANCE ----------------
CREATE TABLE attendance (
    att_id INT AUTO_INCREMENT PRIMARY KEY,
    emp_id INT NOT NULL,
    date DATE NOT NULL,
    status ENUM('PRESENT','ABSENT') DEFAULT 'PRESENT',
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE
);

-- ---------------- LEAVE REQUESTS ----------------
CREATE TABLE leave_requests (
    leave_id INT AUTO_INCREMENT PRIMARY KEY,
    emp_id INT NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    reason VARCHAR(255),
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE
);

-- ---------------- TASKS ----------------
CREATE TABLE tasks (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    emp_id INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    status ENUM('OPEN','DONE') DEFAULT 'OPEN',
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE
);

-- ---------------- PAYROLL ----------------
CREATE TABLE payroll (
    pay_id INT AUTO_INCREMENT PRIMARY KEY,
    emp_id INT NOT NULL,
    month VARCHAR(20) NOT NULL,
    base_salary DOUBLE NOT NULL,
    bonus DOUBLE DEFAULT 0,
    deductions DOUBLE DEFAULT 0,
    net_salary DOUBLE NOT NULL,
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE
);

-- ---------------- SAMPLE DATA ----------------
-- Admin employee & user
INSERT INTO employees (name, dept, designation, salary) VALUES ('Admin User', 'IT', 'Admin', 0);
INSERT INTO users (username, password, role, emp_id) VALUES ('admin', 'admin123', 'ADMIN', 1);

-- Manager employee & user
INSERT INTO employees (name, dept, designation, salary) VALUES ('Ravi Kumar', 'IT', 'Manager', 60000);
INSERT INTO users (username, password, role, emp_id) VALUES ('manager1', 'manager123', 'MANAGER', 2);

-- Employee employee & user
INSERT INTO employees (name, dept, designation, salary, manager_id) VALUES ('Sita Devi', 'IT', 'Developer', 30000, 2);
INSERT INTO users (username, password, role, emp_id) VALUES ('employee1', 'employee123', 'EMPLOYEE', 3);
