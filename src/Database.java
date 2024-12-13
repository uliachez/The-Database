import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;

public class Database {
    private HashMap<Integer, Record> records; //Хранилище записей
    private String filePath;

    public Database() {
        records = new HashMap<>();
    }
    public void loadFromFile(String filePath) throws IOException {
        this.filePath = filePath;
        records.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(", ");
                int id = Integer.parseInt(parts[0]);
                String fio = parts[1];
                int age = Integer.parseInt(parts[2]);
                double salary = Double.parseDouble(parts[3]);
                boolean statusIsActive = Boolean.parseBoolean(parts[4]);
                records.put(id, new Record(id, fio, age, salary, statusIsActive)); //Загрузка в HashMap
            }
        }
    }
    private boolean backupCreated = false;
    public void createBackup() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("backup.txt"))) {
            for (Record record : records.values()) {
                bw.write(record.toString());
                bw.newLine();
            }
        }
        backupCreated = true; // Можно оставить, чтобы указать, что backup был создан
    }
    public void deleteBackup() {
        File backupFile = new File("backup.txt");
        if (backupFile.exists()) {
            backupFile.delete();
        }
    }
    public void restoreFromBackup() throws IOException {
        records.clear();
        try (BufferedReader br = new BufferedReader(new FileReader("backup.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(", ");
                int id = Integer.parseInt(parts[0]);
                String fio = parts[1];
                int age = Integer.parseInt(parts[2]);
                double salary = Double.parseDouble(parts[3]);
                boolean status = Boolean.parseBoolean(parts[4]);
                records.put(id, new Record(id, fio, age, salary, status));
            }
        }
    }
    public void saveToFile(String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Record record : records.values()) {
                bw.write(record.toString());
                bw.newLine();
            }
        }
    }
    public void exportToXlsx(String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Database Records");
        String[] headers = {"ID", "FIO", "Age", "Salary", "Status"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        int rowNum = 1;
        for (Record record : records.values()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(record.getId());
            row.createCell(1).setCellValue(record.getFio());
            row.createCell(2).setCellValue(record.getAge());
            row.createCell(3).setCellValue(record.getSalary());
            row.createCell(4).setCellValue(record.getStatusIsActive());
        }
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }
    public void addRecord(Record record) throws IOException {
        if (records.containsKey(record.getId())) {
            throw new IllegalArgumentException("Record with ID " + record.getId() + " already exists.");
        }
        records.put(record.getId(), record);
        createBackup(); // Создаем backup при первом изменении
    }
    public void removeRecordFromBD(String field, String value) throws IOException {
        boolean recordRemoved = false;

        switch (field.toLowerCase()) {
            case "id":
                try {
                    int id = Integer.parseInt(value);
                    if (records.remove(id) != null) {
                        recordRemoved = true; // Флаг успешного удаления
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID format");
                }
                break;

            case "fio":
                recordRemoved = records.values().removeIf(record -> record.getFio().equalsIgnoreCase(value));
                break;

            case "age":
                try {
                    int age = Integer.parseInt(value);
                    recordRemoved = records.values().removeIf(record -> record.getAge() == age);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid age format.");
                }
                break;

            case "salary":
                try {
                    double salary = Double.parseDouble(value);
                    recordRemoved = records.values().removeIf(record -> record.getSalary() == salary);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid salary format.");
                }
                break;

            case "status":
                try {
                    boolean status = Boolean.parseBoolean(value);
                    recordRemoved = records.values().removeIf(record -> record.getStatusIsActive() == status);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid status format.");
                }
                break;

            default:
                System.out.println("Invalid field name.");
        }

        if (recordRemoved) {
            createBackup(); // Создаем backup при первом изменении
        }
    }
    public ArrayList<Record> searchRecordFromBD(String field, String value) {
        ArrayList<Record> results = new ArrayList<>();
        switch (field.toLowerCase()) {
            case "id":
                try {
                    int id = Integer.parseInt(value);
                    Record record = records.get(id);
                    if (record != null) {
                        results.add(record);
                    }
                }
                catch (NumberFormatException e){
                    System.out.println("Invalid ID format");

                }
                break;
            case "fio":
                for (Record record : records.values()) {
                    if (record.getFio().equalsIgnoreCase(value)) {
                        results.add(record);
                    }
                }
                break;
            case "age":
                try {
                    int age = Integer.parseInt(value);
                    for (Record record : records.values()) {
                        if (record.getAge() == age) {
                            results.add(record);
                        }
                    }
                }
                catch (NumberFormatException e) {
                    System.out.println("Invalid age format.");
                }
                break;
            case "salary":
                try {
                    double salary = Double.parseDouble(value);
                    for (Record record : records.values()) {
                        if (record.getSalary() == salary) {
                            results.add(record);
                        }
                    }
                }
                catch (NumberFormatException e) {
                    System.out.println("Invalid salary format.");
                }
                break;
            case "status":
                try {
                    boolean status = Boolean.parseBoolean(value);
                    for (Record record : records.values()) {
                        if (record.getStatusIsActive() == status) {
                            results.add(record);
                        }
                    }
                }
                catch (NumberFormatException e) {
                    System.out.println("Invalid salary format.");
                }
                break;
            default:
                System.out.println("Invalid field name.");
        }
        return results;
    }
    public void clearAllRecords() throws IOException {
        if (!records.isEmpty()) {
            records.clear();
            createBackup(); // Создаем backup при очистке базы
        }
    }
    public boolean recordExists(int id) {
        return records.containsKey(id);
    }
    public boolean updateRecord(int id, String newFIO, int newAge, double newSalary, boolean newStatus) throws IOException {
        if (records.containsKey(id)) {
            records.put(id, new Record(id, newFIO, newAge, newSalary, newStatus));
            createBackup(); // Создаем backup при первом изменении
            return true;
        }
        return false;
    }
    public ArrayList<Record> getRecords() {
        return new ArrayList<>(records.values()); //Возвращаем все записи
    }
}
