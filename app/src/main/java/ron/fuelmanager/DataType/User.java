package ron.fuelmanager.DataType;

public class User {
    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", budget=" + budget +
                ", car=" + car +
                '}';
    }

    private String firstName, lastName, city, address;
    private Double budget;
    private Car car;

    public User(String firstName, String lastName, String city, String address, double budget, Car car) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.address = address;
        this.budget = budget;
        this.car = car;
    }

    public User() {}

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public double getBudget() {
        return budget;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
