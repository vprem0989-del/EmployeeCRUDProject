
package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class InsertEmployeeData {

    public static void main(String[] args) {

        String[] names = {
                "Rahul", "Amit", "Priya", "Neha", "Vikas",
                "Pooja", "Rohit", "Anjali", "Karan", "Sneha"
        };

        String[] cities = {
                "Kanpur", "Lucknow", "Delhi", "Noida", "Jaipur"
        };

        String[] departments = {
                "IT", "HR", "Finance", "Sales", "Marketing"
        };

        // Employee ki database IDs store karne ke liye
        ArrayList<Long> employeeIds = new ArrayList<>();

        String employeeSQL =
                "INSERT INTO public.employees " +
                        "(name, email, phone_number, address, department) " +
                        "VALUES (?, ?, ?, ?, ?) RETURNING employee_id";

        String salarySQL =
                "INSERT INTO public.salaries (employee_id, amount) " +
                        "VALUES (?, ?)";

        try (Connection con = DatabaseConnection.getConnection()) {

            // Dono tables mein changes ek transaction mein honge
            con.setAutoCommit(false);

            try (
                    PreparedStatement employeeStmt =
                            con.prepareStatement(employeeSQL);

                    PreparedStatement salaryStmt =
                            con.prepareStatement(salarySQL)
            ) {

                // Loop 1000 baar chalega
                for (int i = 1; i <= 1000; i++) {

                    String name = names[(i - 1) % names.length];
                    String city = cities[(i - 1) % cities.length];
                    String department =
                            departments[(i - 1) % departments.length];

                    employeeStmt.setString(1, name);
                    employeeStmt.setString(
                            2, "employee" + i + "@example.com");
                    employeeStmt.setString(
                            3, "TEST" + String.format("%07d", i));
                    employeeStmt.setString(
                            4, "House " + i + ", " + city);
                    employeeStmt.setString(5, department);

                    // Employee insert karo aur uski ID lo
                    try (ResultSet rs = employeeStmt.executeQuery()) {
                        if (rs.next()) {
                            employeeIds.add(rs.getLong("employee_id"));
                        }
                    }
                }

                // Har employee ke liye salary prepare karo
                for (int i = 0; i < employeeIds.size(); i++) {

                    salaryStmt.setLong(1, employeeIds.get(i));

                    // Sample salary: 25000 se 49000 tak
                    salaryStmt.setBigDecimal(
                            2,
                            java.math.BigDecimal.valueOf(
                                    25000 + (i % 25) * 1000
                            )
                    );

                    salaryStmt.addBatch();
                }

                // Saari salary entries ek saath execute karo
                salaryStmt.executeBatch();

                // Changes database mein save karna ka liya
                con.commit();

                System.out.println("Employees inserted: "
                        + employeeIds.size());
                System.out.println("Salaries inserted: "
                        + employeeIds.size());
                System.out.println("Data saved successfully!");

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
