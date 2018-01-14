package ron.fuelmanager.DataType;

/**
 * Created by Ron on 13/01/2018.
 */

public final class Car {

    private String brand;
    private int modelID;
    private String model;
    private double mpg;

    public Car(String brand, int modelID, String model, double mpg) {
        this.brand = brand;
        this.modelID = modelID;
        this.model = model;
        this.mpg = mpg;
    }

    public Car() {}

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getModelID() {
        return modelID;
    }

    public void setModelID(int modelID) {
        this.modelID = modelID;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getMpg() {
        return mpg;
    }

    public void setMpg(double mpg) {
        this.mpg = mpg;
    }
}
