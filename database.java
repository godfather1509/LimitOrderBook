import java.sql.*;

public class database {
    public static void main(String[] args) {
        try {
            // Establish connection
            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/sonoo", "root", "root");

            // Insert data into emp table
            String query = "INSERT INTO emp (id, name, age) VALUES (?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(query);

            // Adding first employee
            pstmt.setInt(1, 1); // ID
            pstmt.setString(2, "John Doe"); // Name
            pstmt.setInt(3, 21); // Job
            pstmt.executeUpdate();

            // Adding second employee
            pstmt.setInt(1, 2);
            pstmt.setString(2, "Jane Smith");
            pstmt.setInt(3, 21); // Job
            pstmt.executeUpdate();

            // Adding third employee
            pstmt.setInt(1, 3);
            pstmt.setString(2, "Mike Brown");
            pstmt.setInt(3, 21); // Job
            pstmt.executeUpdate();

            System.out.println("Data inserted successfully!");

            // Retrieve and display data
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM emp");
            while (rs.next()) {
                System.out.println(rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3));
            }

            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
