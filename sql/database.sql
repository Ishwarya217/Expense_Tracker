CREATE DATABASE ExpenseTrackerDB;      //create a database named ExpenseTrackerDB
USE ExpenseTrackerDB;                  //use the particular database
//create table for the database    
CREATE TABLE expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date VARCHAR(20),
    category VARCHAR(50),
    amount DOUBLE,
    description TEXT
);
//display the entire table
SELECT * FROM expenses;
