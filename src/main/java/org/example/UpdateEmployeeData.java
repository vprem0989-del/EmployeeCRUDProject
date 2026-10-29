
package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class UpdateEmployeeData {

    public static void main(String[] args) {

        // Different sample addresses
        String[] addresses = {
                "Civil Lines, Kanpur, Uttar Pradesh",
                "Gomti Nagar, Lucknow, Uttar Pradesh",
                "Rohini, Delhi",
                "Sector 62, Noida, Uttar Pradesh",
                "Malviya Nagar, Jaipur, Rajasthan",
                "Kankarbagh, Patna, Bihar",
                "Salt Lake, Kolkata, West Bengal",
                "Andheri West, Mumbai, Maharashtra",
                "Indiranagar, Bengaluru, Karnataka",
                "Banjara Hills, Hyderabad, Telangana"
        };

        ArrayList<Long> employeeIds = new ArrayList<>();
        ArrayList<String> employeeNames = new ArrayList<>();

        // Har employee ka unique phone number rakhne ke liye
        Set<String> phoneNumbers = new HashSet<>();

        String selectSQL =
                "SELECT employee_id, name " +
                        "FROM public.employees ORDER BY employee_id";

        String updateSQL =
                "UPDATE public.employees " +
                        "SET email = ?, phone_number = ?, address = ? " +
                        "WHERE employee_id = ?";

        try (Connection con = DatabaseConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                // Step 1: Existing employees ki IDs aur names lo
                try (PreparedStatement selectStmt =
                             con.prepareStatement(selectSQL);
                     ResultSet rs = selectStmt.executeQuery()) {

                    while (rs.next()) {
                        employeeIds.add(
                                rs.getLong("employee_id"));

                        employeeNames.add(
                                rs.getString("name"));
                    }
                }

                // Step 2: Existing records update karo
                try (PreparedStatement updateStmt =
                             con.prepareStatement(updateSQL)) {

                    for (int i = 0; i < employeeIds.size(); i++) {

                        long id = employeeIds.get(i);
                        String name = employeeNames.get(i);

                        // Name se email banao
                        String cleanName = name
                                .toLowerCase()
                                .replaceAll("[^a-z0-9]", "");

                        String email =
                                cleanName + id + "@gmail.com";

                        // Unique 10-digit mobile-style number
                        String phone;

                        do {
                            int firstDigit =
                                    ThreadLocalRandom.current()
                                            .nextInt(6, 10);

                            long remainingDigits =
                                    ThreadLocalRandom.current()
                                            .nextLong(100000000L,
                                                    1000000000L);

                            phone = firstDigit
                                    + String.valueOf(remainingDigits);

                        } while (!phoneNumbers.add(phone));

                        // Address list se alag address choose karo
                        String address =
                                addresses[i % addresses.length];

                        updateStmt.setString(1, email);
                        updateStmt.setString(2, phone);
                        updateStmt.setString(3, address);
                        updateStmt.setLong(4, id);

                        updateStmt.addBatch();
                    }

                    // Saare updates batch mein execute karo
                    updateStmt.executeBatch();
                }

                con.commit();

                System.out.println(
                        "Employees updated: " + employeeIds.size());
                System.out.println(
                        "Email, phone and address updated successfully!");

            } catch (Exception e) {
                con.rollback();

                System.out.println(
                        "Error occurred. Changes rolled back.");

                e.printStackTrace();
            }

        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}
