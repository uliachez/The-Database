public class Record {
    private int id; //ключевое поле
    private String fio;
    private int age;
    private double salary;
    private boolean statusIsActive;

    public Record(int id, String fio, int age, double salary, boolean statusIsActive) {
        this.id = id;
        this.fio = fio;
        this.age = age;
        this.salary = salary;
        this.statusIsActive = statusIsActive;
    }
    public int getId() {
        return id;
    }
    public String getFio() {
        return fio;
    }
    public int getAge() {
        return age;
    }
    public double getSalary() {
        return salary;
    }
    public boolean getStatusIsActive() {
        return statusIsActive;
    }
    public String toString() {
        return id + ", " + fio + ", " + age + ", " + salary + ", " + statusIsActive;
    }
}
