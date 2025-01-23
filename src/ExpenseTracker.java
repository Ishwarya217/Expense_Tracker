package windowsapps.java;

import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/ExpenseTrackerDB";    //database_name
    private static final String USER = "root";  //username
    private static final String PASSWORD = "root"; //password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

class Expense {
    private String date;
    private String category;
    private double amount;
    private String description;

    public Expense(String date, String category, double amount, String description) {
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Date: " + date + ", Category: " + category + ", Amount: $" + amount + ", Description: " + description;
    }
}

class ExpensesTracker {
    private List<Expense> expenses;
    private final String fileName = "expenses.txt";

    public ExpensesTracker() {
        expenses = new ArrayList<>();
        loadExpenses();
    }

    public void addExpense(String date, String category, double amount, String description) {
    	String query = "INSERT INTO Expenses (date, category, amount, description) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, date);
            preparedStatement.setString(2, category);
            preparedStatement.setDouble(3, amount);
            preparedStatement.setString(4, description);
            preparedStatement.executeUpdate();
            System.out.println("Expense added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding expense: " + e.getMessage());
        }
    }

   
    public void viewExpenses() {
    	String query = "SELECT * FROM Expenses";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            if (!resultSet.isBeforeFirst()) {
                System.out.println("No expenses recorded yet.");
            } else {
                System.out.println("\nAll Expenses:");
                while (resultSet.next()) {
                    System.out.println("Date: " + resultSet.getString("date") +
                            ", Category: " + resultSet.getString("category") +
                            ", Amount: $" + resultSet.getDouble("amount") +
                            ", Description: " + resultSet.getString("description"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving expenses: " + e.getMessage());
        }
    }

    public void viewExpensesByCategory(String category) {
    	String query = "SELECT * FROM Expenses WHERE category = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, category);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.isBeforeFirst()) {
                    System.out.println("No expenses found in this category.");
                } else {
                    System.out.println("\nExpenses in Category: " + category);
                    while (resultSet.next()) {
                        System.out.println("Date: " + resultSet.getString("date") +
                                ", Amount: $" + resultSet.getDouble("amount") +
                                ", Description: " + resultSet.getString("description"));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving expenses by category: " + e.getMessage());
        }
    }


    public void totalExpenses() {
    	String query = "SELECT SUM(amount) AS total FROM Expenses";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                double total = resultSet.getDouble("total");
                System.out.println("Total Expenses: $" + total);
            }
        } catch (SQLException e) {
            System.out.println("Error calculating total expenses: " + e.getMessage());
        }
    }

    private void saveExpenses() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Expense expense : expenses) {
                writer.write(expense.getDate() + "," + expense.getCategory() + "," + expense.getAmount() + "," + expense.getDescription());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving expenses.");
        }
    }

    private void loadExpenses() {
        File file = new File(fileName);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 4) {
                        String date = data[0];
                        String category = data[1];
                        double amount = Double.parseDouble(data[2]);
                        String description = data[3];
                        expenses.add(new Expense(date, category, amount, description));
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading expenses.");
            }
        }
    }
}

public class ExpenseTracker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ExpensesTracker tracker = new ExpensesTracker();

        while (true) {
            System.out.println("\nExpense Tracker");
            System.out.println("1. Add Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. View Expenses by Category");
            System.out.println("4. View Total Expenses");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter date (dd-mm-yyyy): ");
                    String date = scanner.nextLine();
                    System.out.print("Enter category (e.g., Food, Travel, Utilities): ");
                    String category = scanner.nextLine();
                    System.out.print("Enter amount: ");
                    double amount = scanner.nextDouble();
                    scanner.nextLine(); // Consume newline
                    System.out.print("Enter description: ");
                    String description = scanner.nextLine();
                    tracker.addExpense(date, category, amount, description);
                    break;
                case 2:
                    tracker.viewExpenses();
                    break;
                case 3:
                    System.out.print("Enter category to view expenses: ");
                    String cat = scanner.nextLine();
                    tracker.viewExpensesByCategory(cat);
                    break;
                case 4:
                    tracker.totalExpenses();
                    break;
                case 5:
                    System.out.println("Exiting Expense Tracker. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
