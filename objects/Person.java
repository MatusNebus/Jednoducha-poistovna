package objects;

import contracts.AbstractContract;
import java.util.LinkedHashSet;
import java.util.Set;
import java.time.LocalDate;


public class Person {
    private final String id;
    private final LegalForm legalForm;
    private int paidOutAmount;
    private final Set<AbstractContract> contracts;

    //Konštruktor:
    public Person(String id) {
        if (id == null || !isValidId(id)) {
            throw new IllegalArgumentException("Neplatné id.");
        }
        this.id = id;
        this.legalForm = determineLegalForm(id);
        this.paidOutAmount = 0;
        this.contracts = new LinkedHashSet<>(); //linked to uklada v poradi, v akom sa do nej vkladali
    }

    public static boolean isValidBirthNumber(String birthNumber) {
        if (birthNumber == null || !(birthNumber.length() == 9 || birthNumber.length() == 10) || !birthNumber.chars().allMatch(Character::isDigit)) {
            return false;
        }

        try {
            int year = Integer.parseInt(birthNumber.substring(0, 2));
            int month = Integer.parseInt(birthNumber.substring(2, 4));
            int day = Integer.parseInt(birthNumber.substring(4, 6));

            if ((month >= 51 && month <= 62)) { //je to zena, odcitam 50
                month -= 50;
            }
            if (month < 1 || month > 12) { //ak to nie je ani po odcitani medzi 1-12, tak neplatne
                return false;
            }

            if (birthNumber.length() == 9) {  //Rok určíme podľa dĺžky rodneho cisla
                if (year > 53) {  // rok 19RR, ale len do roku 1953 vrátane
                    return false;
                }
                year += 1900;
            } else {    // dlzka je 10, takze kontrolná suma pre 10 ciferne rodne cislo:
                int checksum = 0;
                for (int i = 0; i < 10; i++) {
                    int digit = Character.getNumericValue(birthNumber.charAt(i));
                    checksum += (i % 2 == 0 ? 1 : -1) * digit;
                }
                if (checksum % 11 != 0) {
                    return false;
                }
                // rok 19RR alebo 20RR - treba rozlisit 00–53 su 2000–2053, inak 1900–1999
                year += (year <= 53 ? 2000 : 1900);
            }

            // Pokus o vytvorenie dátumu – ak failne, nie je to validné RČ
            LocalDate.of(year, month, day);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidRegistrationNumber(String registrationNumber) {
        if (registrationNumber == null) return false;
        if (registrationNumber.length() == 6 || registrationNumber.length() == 8) {
            return registrationNumber.chars().allMatch(Character::isDigit); // ci su vsetky znaky cisla
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public int getPaidOutAmount() {
        return paidOutAmount;
    }

    public LegalForm getLegalForm() {
        return legalForm;
    }

    public Set<AbstractContract> getContracts() {
        return contracts;
    }

    public void addContract(AbstractContract contract) {
        if (contract == null) {
            throw new IllegalArgumentException("Zmluv contract nesmie byť null.");
        }
        this.contracts.add(contract); //pridam do mnoziny contractov
    }

    //payout vyplatí peniaze osobe, zvýši celkovú vyplatenú sumu:
    public void payout(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("vyplatena suma amount musí byť kladná.");
        }
        this.paidOutAmount += amount;
    }

    //nizsie su dve moje funkcie pomocne:
    private boolean isValidId(String id) {
        return isValidBirthNumber(id) || isValidRegistrationNumber(id);
    }

    //ci je fyzicka alebo pravnicka osoba:
    private LegalForm determineLegalForm(String id) {
        if (isValidBirthNumber(id)) {
            return LegalForm.NATURAL;
        } else {
            return LegalForm.LEGAL;
        }
    }
}
