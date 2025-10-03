import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    private static final String URL = "jdbc:mysql://localhost:3306/ems?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Kala@2911";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("\nUsername (or 'exit' to quit): ");
            String username = sc.nextLine().trim();
            if(username.equalsIgnoreCase("exit")) break;

            System.out.print("Password: ");
            String password = sc.nextLine().trim();

            String role = authenticate(username, password);
            if (role == null) {
                System.out.println("❌ Login failed. Try again.");
                continue;
            }

            System.out.println("✅ Login Successful! Role: " + role);
            if (role.equals("ADMIN")) adminMenu(sc);
            else if (role.equals("MANAGER")) managerMenu(sc);
            else if (role.equals("EMPLOYEE")) employeeMenu(sc, username);
        }
        sc.close();
    }

    // ---------- AUTHENTICATION ----------
    public static String authenticate(String username, String password) {
        String sql = "SELECT role FROM users WHERE username=? AND password=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("role");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ---------- ADMIN MENU ----------
    private static void adminMenu(Scanner sc) {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Add Employee");
            System.out.println("2. Create User");
            System.out.println("3. Logout");
            System.out.print("Choose: ");
            int ch = readInt(sc);

            if (ch == 1) {
                System.out.print("Name: "); String name = sc.nextLine();
                System.out.print("Dept: "); String dept = sc.nextLine();
                System.out.print("Designation: "); String des = sc.nextLine();
                System.out.print("Salary: "); double sal = readDouble(sc);
                System.out.print("Manager ID (or blank): "); String mgr = sc.nextLine();
                addEmployee(name, dept, des, sal, mgr);
            } else if (ch == 2) {
                System.out.print("Emp ID: "); int empId = readInt(sc);
                System.out.print("Username: "); String uname = sc.nextLine();
                System.out.print("Password: "); String pass = sc.nextLine();
                System.out.print("Role (ADMIN/MANAGER/EMPLOYEE): "); String role = sc.nextLine();
                createUser(empId, uname, pass, role);
            } else if (ch == 3) break;
        }
    }

    // ---------- MANAGER MENU ----------
    private static void managerMenu(Scanner sc) {
        while (true) {
            System.out.println("\n--- MANAGER MENU ---");
            System.out.println("1. Assign Task");
            System.out.println("2. Approve/Reject Leave");
            System.out.println("3. Logout");
            System.out.print("Choose: ");
            int ch = readInt(sc);

            if (ch == 1) {
                System.out.print("Emp ID: "); int empId = readInt(sc); sc.nextLine();
                System.out.print("Task Desc: "); String desc = sc.nextLine();
                assignTask(empId, desc);
            } else if (ch == 2) {
                System.out.print("Leave ID: "); int leaveId = readInt(sc);
                System.out.print("Approve or Reject? "); String st = sc.nextLine().toUpperCase();
                if(!st.equals("APPROVED") && !st.equals("REJECTED")) {
                    System.out.println("❌ Invalid status");
                } else updateLeaveStatus(leaveId, st);
            } else if (ch == 3) break;
        }
    }

    // ---------- EMPLOYEE MENU ----------
    private static void employeeMenu(Scanner sc, String username) {
        int empId = getEmployeeIdFromUsername(username);
        if (empId == -1) { System.out.println("❌ Employee not linked."); return; }

        while (true) {
            System.out.println("\n--- EMPLOYEE MENU ---");
            System.out.println("1. Mark Attendance");
            System.out.println("2. Apply Leave");
            System.out.println("3. View Salary");
            System.out.println("4. Logout");
            System.out.print("Choose: ");
            int ch = readInt(sc);

            if (ch == 1) markAttendance(empId);
            else if (ch == 2) {
                System.out.print("From Date (YYYY-MM-DD): "); String f = sc.nextLine();
                System.out.print("To Date (YYYY-MM-DD): "); String t = sc.nextLine();
                System.out.print("Reason: "); String r = sc.nextLine();
                applyLeave(empId, f, t, r);
            } else if (ch == 3) viewSalary(empId);
            else if (ch == 4) break;
        }
    }

    // ---------- HELPERS ----------
    public static int getEmployeeIdFromUsername(String uname) {
        String sql = "SELECT emp_id FROM users WHERE username=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uname);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("emp_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public static void addEmployee(String name, String dept, String des, double salary, String mgrId) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "INSERT INTO employees (name, dept, designation, salary, manager_id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, dept);
                ps.setString(3, des);
                ps.setDouble(4, salary);
                if (mgrId == null || mgrId.isEmpty()) ps.setNull(5, Types.INTEGER);
                else ps.setInt(5, Integer.parseInt(mgrId));
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) System.out.println("✅ Employee added. ID=" + rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void createUser(int empId, String uname, String pass, String role) {
        String sql = "INSERT INTO users (username, password, role, emp_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uname);
            ps.setString(2, pass);
            ps.setString(3, role.toUpperCase());
            ps.setInt(4, empId);
            ps.executeUpdate();
            System.out.println("✅ User created for employee ID " + empId);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void markAttendance(int empId) {
        String sql = "INSERT INTO attendance (emp_id, date, status) VALUES (?, ?, 'PRESENT')";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
            System.out.println("✅ Attendance marked.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void applyLeave(int empId, String from, String to, String reason) {
        String sql = "INSERT INTO leave_requests (emp_id, from_date, to_date, reason, status) VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ps.setString(4, reason);
            ps.executeUpdate();
            System.out.println("✅ Leave applied.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void assignTask(int empId, String desc) {
        String sql = "INSERT INTO tasks (emp_id, description, status) VALUES (?, ?, 'OPEN')";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setString(2, desc);
            ps.executeUpdate();
            System.out.println("✅ Task assigned.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void updateLeaveStatus(int leaveId, String status) {
        String sql = "UPDATE leave_requests SET status=? WHERE leave_id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, leaveId);
            ps.executeUpdate();
            System.out.println("✅ Leave status updated.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void viewSalary(int empId) {
        String month = "October";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            // Check if payroll already exists
            PreparedStatement psCheck = conn.prepareStatement("SELECT * FROM payroll WHERE emp_id=? AND month=?");
            psCheck.setInt(1, empId);
            psCheck.setString(2, month);
            ResultSet rsCheck = psCheck.executeQuery();
            if(rsCheck.next()) {
                System.out.println("💰 Salary already processed for this month.");
                return;
            }

            PreparedStatement ps1 = conn.prepareStatement("SELECT salary FROM employees WHERE emp_id=?");
            ps1.setInt(1, empId);
            ResultSet rs1 = ps1.executeQuery();
            if (!rs1.next()) { System.out.println("❌ Employee not found"); return; }
            double base = rs1.getDouble("salary");

            PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT COUNT(*) AS absent_days FROM attendance WHERE emp_id=? AND status='ABSENT'");
            ps2.setInt(1, empId);
            ResultSet rs2 = ps2.executeQuery();
            int absentDays = 0;
            if (rs2.next()) absentDays = rs2.getInt("absent_days");

            double deductions = absentDays * 1000;
            double bonus = 0;
            double net = base + bonus - deductions;

            PreparedStatement ps3 = conn.prepareStatement(
                    "INSERT INTO payroll (emp_id, month, base_salary, bonus, deductions, net_salary) VALUES (?, ?, ?, ?, ?, ?)");
            ps3.setInt(1, empId);
            ps3.setString(2, month);
            ps3.setDouble(3, base);
            ps3.setDouble(4, bonus);
            ps3.setDouble(5, deductions);
            ps3.setDouble(6, net);
            ps3.executeUpdate();

            System.out.printf("💰 Salary Slip for %d (%s): Base=%.2f, Bonus=%.2f, Deductions=%.2f, Net=%.2f%n",
                    empId, month, base, bonus, deductions, net);

        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ---------- SAFE INPUT ----------
    private static int readInt(Scanner sc) {
        while (true) {
            try { return Integer.parseInt(sc.nextLine()); }
            catch (NumberFormatException e) { System.out.print("❌ Invalid input. Enter a number: "); }
        }
    }

    private static double readDouble(Scanner sc) {
        while (true) {
            try { return Double.parseDouble(sc.nextLine()); }
            catch (NumberFormatException e) { System.out.print("❌ Invalid input. Enter a number: "); }
        }
    }
}
