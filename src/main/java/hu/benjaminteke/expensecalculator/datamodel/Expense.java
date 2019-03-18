package hu.benjaminteke.expensecalculator.datamodel;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "expenses")
public class Expense {

    // for QueryBuilder to be able to find the fields
    private static final String TIME_FIELD_NAME = "timeOfExpense";
    private static final String EXPENSE_FIELD_NAME = "expense";
    private static final String BALANCE_FIELD_NAME = "balance";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = TIME_FIELD_NAME)
    private String time;

    @DatabaseField(columnName = EXPENSE_FIELD_NAME)
    private Double expense;

    @DatabaseField(columnName = BALANCE_FIELD_NAME)
    private Double balance;

    Expense() {
        // all persisted classes must define a no-arg constructor with at least package visibility
    }

    public Expense(String timeOfExpense, Double expense, Double balance) {
        this.time = timeOfExpense;
        this.expense = expense;
        this.balance = balance;
    }

    Double getBalance() {
        return balance;
    }

    Object[] getObject() {
        return new Object[]{this.time, this.expense, this.balance};
    }
}