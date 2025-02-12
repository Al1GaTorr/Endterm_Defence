import java.sql.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;

class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/mydatabase";
    private static final String USER = "postgres";
    private static final String PASSWORD = "875173";


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

class CarDealership {
    private String name;
    public ArrayList<Car> cars;

    public CarDealership(String name) {
        this.name = name;
        this.cars = new ArrayList<>();
        loadCarsFromDatabase();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Car> getCars() {
        return cars;
    }
    public void clearDatabase() {
        String query = "DELETE FROM cars"; 
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            System.out.println("DB has successfully cleared.");
        } catch (SQLException e) {
            System.err.println("Error in clearing database: " + e.getMessage());
        }
    }

    public void loadCarsFromDatabase() {
        cars.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cars")) {

            while (rs.next()) {
                Car car = new Car(
                        rs.getInt("id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getInt("quantity_in_stock")
                );
                cars.add(car);
            }
        } catch (SQLException e) {
            System.err.println("Error in loading cars: " + e.getMessage());
        }
    }

    public void addCarToDatabase(Car car) {
        String query = "INSERT INTO cars (brand, model, quantity_in_stock) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, car.getBrand());
            stmt.setString(2, car.getModel());
            stmt.setInt(3, car.getQuantityInStock());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                car.setId(rs.getInt(1));
            }

            cars.add(car);
        } catch (SQLException e) {
            System.err.println("Error in adding car: " + e.getMessage());
        }
    }
    public void removeCarfromDatabase(Car car) {
        String query = "DELETE FROM cars WHERE brand = ? AND model = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, car.getBrand());
            stmt.setString(2, car.getModel());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                cars.remove(car);
                loadCarsFromDatabase();
                System.out.println("Car was deleted: " + car.getBrand() + " " + car.getModel());
            } else {
                System.out.println("Car not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error in deleting car: " + e.getMessage());
        }
    }


    public Car searchCarByBrand(String brand) {
        for (Car car : cars) {
            if (car.getBrand().equalsIgnoreCase(brand)) {
                return car;
            }
        }
        return null;
    }

    public void updateCarStockInDatabase(Car car) {
        String query = "UPDATE cars SET quantity_in_stock = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, car.getQuantityInStock());
            stmt.setInt(2, car.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error in stock quantity:  " + e.getMessage());
        }
    }
}

class Car {
    private int id;
    private String brand;
    private String model;
    private int quantityInStock;

    public Car(int id, String brand, String model, int quantityInStock) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.quantityInStock = quantityInStock;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }
}

class GUI_Endterm extends JFrame {
    private JTextField inputField;
    private JTextArea outputArea;
    private CarDealership dealership;

    public GUI_Endterm() {
        dealership = new CarDealership("Elite Auto");
        setTitle("Car Showroom");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(new Color(112, 128, 144));

        JLabel label = new JLabel("Welcome to the Car Showroom!", SwingConstants.CENTER);
        label.setBounds(200, 50, 400, 50);
        label.setForeground(Color.WHITE);
        add(label);

        JButton addCarButton = new JButton("Add Car");
        addCarButton.setBounds(200, 100, 150, 50);
        add(addCarButton);
        addCarButton.addActionListener(e -> addCar());


        JButton removeCarButton = new JButton("Remove Car");
        removeCarButton.setBounds(400, 100, 150, 50);
        add(removeCarButton);
        removeCarButton.addActionListener(e -> removeCar());

        JButton buyCarButton = new JButton("Request Purchase");
        buyCarButton.setBounds(600, 100, 150, 50);
        add(buyCarButton);
        buyCarButton.addActionListener(e -> buyCar());

        JButton displayInfoButton = new JButton("Show Inventory");
        displayInfoButton.setBounds(800, 100, 150, 50);
        add(displayInfoButton);
        displayInfoButton.addActionListener(e -> displayCars());



        JButton clearDatabaseButton = new JButton("Clear Database");
        clearDatabaseButton.setBounds(1000, 100, 150, 50);
        add(clearDatabaseButton);
        clearDatabaseButton.addActionListener(e -> {
            dealership.clearDatabase();
            outputArea.setText("Database cleared.\n");
        });

        inputField = new JTextField();
        inputField.setBounds(200, 220, 200, 30);
        add(inputField);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBounds(200, 260, 400, 250);
        add(scrollPane);
    }

    private void addCar() {
        String brand = JOptionPane.showInputDialog("Enter Car Brand:");
        String model = JOptionPane.showInputDialog("Enter Car Model:");
        int stock;
        try {
            stock = Integer.parseInt(JOptionPane.showInputDialog("Enter Stock Quantity:"));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid stock number.");
            return;
        }
        Car car = new Car(0, brand, model, stock);
        dealership.addCarToDatabase(car);
        dealership.loadCarsFromDatabase();
        outputArea.append("Added Car: " + brand + " " + model + " (" + stock + " in stock)\n");
    }
    private void removeCar() {
        String brand = JOptionPane.showInputDialog("Enter Car Brand:");
        String model = JOptionPane.showInputDialog("Enter Car Model:");
        int stock;
        try {
            stock = Integer.parseInt(JOptionPane.showInputDialog("Enter Stock Quantity:"));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid stock number.");
            return;
        }
        Car car = new Car(0, brand, model, stock);
        dealership.removeCarfromDatabase(car);
        outputArea.append(brand + " " + model + " was deleted ");
    }

    private void buyCar() {
        String brand = JOptionPane.showInputDialog("Enter Car Brand to Request:");
        Car car = dealership.searchCarByBrand(brand);
        if (car != null && car.getQuantityInStock() > 0) {
            car.setQuantityInStock(car.getQuantityInStock() - 1);
            dealership.updateCarStockInDatabase(car);
            outputArea.append("Purchase request completed for " + brand + " " + car.getModel() + "\n");
        } else {
            outputArea.append("Car not available.\n");
        }
    }

    private void displayCars() {
        dealership.loadCarsFromDatabase();
        outputArea.setText("");
        outputArea.append("\n--- Car Inventory ---\n");
        for (Car car : dealership.getCars()) {
            outputArea.append(car.getBrand() + " " + car.getModel() + " - Stock: " + car.getQuantityInStock() + "\n");
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI_Endterm().setVisible(true));
    }
}
