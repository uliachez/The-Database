import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Interface extends JFrame {
    private Database database;
    private JTable table;
    private DefaultTableModel tabelModel;

    public Interface() {
        database = new Database();
        setTitle("The Database");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
        setVisible(true);
        tabelModel = new DefaultTableModel(new String[] {"ID", "FIO", "Ade", "Salary", "Employee Status"}, 0);
        table = new JTable(tabelModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        JButton loadButton = new JButton("Load");
        JButton saveButton = new JButton("Save");
        JButton addButton = new JButton("Add");
        JButton removeButton = new JButton("Remove");
        JButton searchButton = new JButton("Search");
        JButton deleteButton = new JButton("Clear All");
        JButton editButton = new JButton("Edit");
        JButton exportButton = new JButton("Export in xlsx");

        loadButton.addActionListener(e -> loadDatabase());
        saveButton.addActionListener(e -> saveDatabase());
        addButton.addActionListener(e -> addRecord());
        removeButton.addActionListener(e -> removeRecord());
        searchButton.addActionListener(e -> searchRecord());
        deleteButton.addActionListener(e -> clearTable());
        editButton.addActionListener(e -> editRecord());
        exportButton.addActionListener(e -> exportInXlsx());

        panel.add(loadButton);
        panel.add(saveButton);
        panel.add(addButton);
        panel.add(removeButton);
        panel.add(searchButton);
        panel.add(deleteButton);
        panel.add(editButton);
        panel.add(exportButton);
        add(panel, BorderLayout.SOUTH);

        checkAndRestoreBackup();
    }
    private void handleWindowClosing() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Do you want to save changes to the backup file before exiting?",
                "Exit Confirmation",
                JOptionPane.YES_NO_CANCEL_OPTION
        );
        if (choice == JOptionPane.YES_OPTION) {
            try {
                database.createBackup();
                dispose();
            }
            catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to update backup file: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
        else if (choice == JOptionPane.NO_OPTION) {
            dispose();
        }
    }
    private void checkAndRestoreBackup() {
        File backupFile = new File("backup.txt");
        if (backupFile.exists()) {
            int response = JOptionPane.showConfirmDialog(this,
                    "A backup file was found. Do you want to restore the database from it?",
                    "Restore Backup", JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                try {
                    database.restoreFromBackup();
                    updateTable();
                    JOptionPane.showMessageDialog(this, "Database restored from backup.");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error restoring from backup: " + e.getMessage());
                }
            } else {
                database.deleteBackup(); // Удаляем backup, если пользователь отказывается
            }
        }
    }

    private void exportInXlsx() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as Excel file");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".xlsx")) {
                filePath += ".xlsx";
            }
            try {
                database.exportToXlsx(filePath);
                JOptionPane.showMessageDialog(this, "Database exported successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting database: " + ex.getMessage());
            }
        }
    }
    private void editRecord() {
        try {
            String idString = JOptionPane.showInputDialog("Enter ID of the record to edit:");
            if (idString == null) {
                JOptionPane.showMessageDialog(this, "Operation cancelled.");
                return;
            }
            int id = Integer.parseInt(idString);
            if (!database.recordExists(id)) {
                JOptionPane.showMessageDialog(this, "Record not found. Unable to edit.");
                return;
            }
            String newFIO = JOptionPane.showInputDialog("Enter new FIO:");
            String newAgeString = JOptionPane.showInputDialog("Enter new Age:");
            String newSalaryString = JOptionPane.showInputDialog("Enter new Salary:");
            String newStatusString = JOptionPane.showInputDialog("Enter new Status: ");
            if (newFIO == null || newAgeString == null || newSalaryString == null || newStatusString == null) {
                JOptionPane.showMessageDialog(this, "Operation cancelled.");
                return;
            }
            int newAge = Integer.parseInt(newAgeString);
            double newSalary = Double.parseDouble(newSalaryString);
            boolean newStatus = Boolean.parseBoolean(newStatusString);
            if (database.updateRecord(id, newFIO, newAge, newSalary, newStatus)) {
                JOptionPane.showMessageDialog(this, "Record updated successfully.");
                updateTable();
            }
            else {
                JOptionPane.showMessageDialog(this, "Failed to update the record.");
            }
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Failed to update record. Invalid data types provided.");
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + e.getMessage());
        }
    }
    private void loadDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                database.loadFromFile(fileChooser.getSelectedFile().getAbsolutePath());
                updateTable();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    private void saveDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                database.saveToFile(fileChooser.getSelectedFile().getAbsolutePath());
                database.createBackup();
                database.deleteBackup();
                JOptionPane.showMessageDialog(this, "Database saved successfully!");
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving database: " + e.getMessage());
            }
        }
    }
    private void addRecord() {
        try {
            String idString = JOptionPane.showInputDialog("Enter ID: ");
            String fioString = JOptionPane.showInputDialog("Enter FIO: ");
            String ageString = JOptionPane.showInputDialog("Enter age: ");
            String salaryString = JOptionPane.showInputDialog("Enter salary: ");
            String statusString = JOptionPane.showInputDialog("Enter status: ");
            if (idString == null || fioString == null || ageString == null || salaryString == null || statusString == null) {
                JOptionPane.showMessageDialog(this, "Operation cancelled.");
                return;
            }
            int id = Integer.parseInt(idString);
            String fio = fioString;
            int age = Integer.parseInt(ageString);
            double salary = Double.parseDouble(salaryString);
            boolean status = Boolean.parseBoolean(statusString);
            if (database.recordExists(id)) {
                JOptionPane.showMessageDialog(this, "Record cannot be created. ID already exists in the table.");
                return;
            }
            try {
                database.addRecord(new Record(id, fio, age, salary, status)); // Обработка IOException
                updateTable();
            }
            catch (IOException ioException) {
                JOptionPane.showMessageDialog(this, "An error occurred while creating a backup: " + ioException.getMessage());
            }
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Failed to create record. Invalid data types provided.");
        }
    }
    private void removeRecord() {
        String[] fields = {"ID", "FIO", "Age", "Salary", "Status"};
        String field = (String) JOptionPane.showInputDialog(
                this,
                "Select field to delete by:",
                "Delete Record",
                JOptionPane.QUESTION_MESSAGE,
                null,
                fields,
                fields[0]
        );

        if (field != null) {
            String value = JOptionPane.showInputDialog(this, "Enter value for " + field + ":");
            if (value != null) {
                try {
                    database.removeRecordFromBD(field.toLowerCase(), value);
                    updateTable();
                    JOptionPane.showMessageDialog(this, "Records deleted successfully.");
                }
                catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "An error occurred while creating a backup: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    private void searchRecord() {
        String[] fields = {"ID", "FIO", "Age", "Salary", "Status"};
        String field = (String) JOptionPane.showInputDialog(
                this,
                "Select field to search by:",
                "Search Record",
                JOptionPane.QUESTION_MESSAGE,
                null,
                fields,
                fields[0]
        );
        if (field != null) {
            String value = JOptionPane.showInputDialog(this, "Enter value for " + field + ":");
            if (value != null) {
                ArrayList<Record> results = database.searchRecordFromBD(field.toLowerCase(), value);
                if (!results.isEmpty()) {
                    StringBuilder resultStr = new StringBuilder("Search results:\n");
                    for (Record record : results) {
                        resultStr.append(record).append("\n");
                    }
                    JOptionPane.showMessageDialog(this, resultStr.toString());
                }
                else {
                    JOptionPane.showMessageDialog(this, "No records found matching the criteria.");
                }
            }
        }
    }
    private void clearTable() {
        try {
            database.clearAllRecords();
            updateTable();
            JOptionPane.showMessageDialog(this, "Table cleared successfully.");
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(this, "An error occurred while creating a backup: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void updateTable() {
        tabelModel.setRowCount(0);
        for (Record record : database.getRecords()) {
            tabelModel.addRow(new Object[]{record.getId(), record.getFio(), record.getAge(), record.getSalary(), record.getStatusIsActive()});
        }
    }
}