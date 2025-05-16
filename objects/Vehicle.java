package objects;

public class Vehicle {
    private final String licensePlate;
    private final int originalValue;

    //Konštruktor:
    public Vehicle(String licensePlate, int originalValue) {
        if (licensePlate == null || !isValidLicensePlate(licensePlate)) {
            throw new IllegalArgumentException("Neplatné licensePlate.");
        }
        if (originalValue <= 0) {
            throw new IllegalArgumentException("Hodnota vozidla originalValue musí byť kladná.");
        }
        this.licensePlate = licensePlate;
        this.originalValue = originalValue;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public int getOriginalValue() {
        return originalValue;
    }

    //moja pomocna metoda na overenie ci je licenseplate ok:
    private boolean isValidLicensePlate(String licensePlate) {
        if (licensePlate.length() != 7) {
            return false;
        }
        for (char c : licensePlate.toCharArray()) {
            if (!Character.isUpperCase(c) && !Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
