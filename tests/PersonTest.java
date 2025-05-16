package objects;

import company.InsuranceCompany;
import contracts.AbstractContract;
import contracts.SingleVehicleContract;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    @Test
    void testValidBirthNumbers() {
        // Valid 9-digit birth numbers (pre-1954)
        assertTrue(Person.isValidBirthNumber("480417080"));  // Valid male birth number
        assertTrue(Person.isValidBirthNumber("485417647"));  // Valid female birth number

        // Valid 10-digit birth numbers (post-1954)
        assertTrue(Person.isValidBirthNumber("8004172022"));  // Valid male birth number
        assertTrue(Person.isValidBirthNumber("8054176449"));  // Valid female birth number
    }

    @Test
    void testInvalidBirthNumbers() {
        // Null or wrong length
        assertFalse(Person.isValidBirthNumber(null));
        assertFalse(Person.isValidBirthNumber("12345678"));
        assertFalse(Person.isValidBirthNumber("12345678901"));

        // Contains non-digit characters
        assertFalse(Person.isValidBirthNumber("94O521999A"));

        // Invalid month (outside 1-12 and 51-62 range)
        assertFalse(Person.isValidBirthNumber("9413219991"));
        assertFalse(Person.isValidBirthNumber("9463219991"));

        // 9-digit number with year > 53
        assertFalse(Person.isValidBirthNumber("540512123"));

        // Invalid date (February 30th)
        assertFalse(Person.isValidBirthNumber("9402309995"));

        // Invalid checksum for 10-digit number
        assertFalse(Person.isValidBirthNumber("9405219996"));
    }

    @Test
    void testValidRegistrationNumbers() {
        assertTrue(Person.isValidRegistrationNumber("123456"));
        assertTrue(Person.isValidRegistrationNumber("12345678"));
        assertTrue(Person.isValidRegistrationNumber("555555"));
    }

    @Test
    void testInvalidRegistrationNumbers() {
        assertFalse(Person.isValidRegistrationNumber(null));
        assertFalse(Person.isValidRegistrationNumber("1234"));
        assertFalse(Person.isValidRegistrationNumber("123456789"));
        assertFalse(Person.isValidRegistrationNumber("12345A"));
    }

    @Test
    void testPersonConstructorWithValidBirthNumbers() {
        // Valid 9-digit birth numbers
        // Female
        Person person1 = new Person("205417685");
        assertEquals("205417685", person1.getId());
        assertEquals(LegalForm.NATURAL, person1.getLegalForm());

        // Male
        Person person2 = new Person("200417514");
        assertEquals("200417514", person2.getId());
        assertEquals(LegalForm.NATURAL, person2.getLegalForm());

        // Valid 10-digit birth numbers
        // Male
        Person person3 = new Person("8004179909");
        assertEquals("8004179909", person3.getId());
        assertEquals(LegalForm.NATURAL, person3.getLegalForm());

        // Female
        Person person4 = new Person("8054176383");
        assertEquals("8054176383", person4.getId());
        assertEquals(LegalForm.NATURAL, person4.getLegalForm());
    }

    @Test
    void testPersonConstructorWithValidRegistrationNumbers() {
        // 6-digit registration number
        Person person1 = new Person("123456");
        assertEquals("123456", person1.getId());
        assertEquals(LegalForm.LEGAL, person1.getLegalForm());

        // 8-digit registration number
        Person person2 = new Person("12345678");
        assertEquals("12345678", person2.getId());
        assertEquals(LegalForm.LEGAL, person2.getLegalForm());
    }

    @Test
    void testPersonConstructorWithInvalidIds() {
        // Null or empty input
        assertThrows(IllegalArgumentException.class, () -> new Person(null));
        assertThrows(IllegalArgumentException.class, () -> new Person(""));

        // Non-digit characters
        assertThrows(IllegalArgumentException.class, () -> new Person("123A456"));
        assertThrows(IllegalArgumentException.class, () -> new Person("8004A72022"));

        // Invalid length
        assertThrows(IllegalArgumentException.class, () -> new Person("12345")); // Too short
        assertThrows(IllegalArgumentException.class, () -> new Person("1234567890123")); // Too long

        // Invalid birth numbers
        assertThrows(IllegalArgumentException.class, () -> new Person("9413219991")); // Invalid month
        assertThrows(IllegalArgumentException.class, () -> new Person("9463219991")); // Invalid month
        assertThrows(IllegalArgumentException.class, () -> new Person("540512123")); // Invalid year for 9-digit
        assertThrows(IllegalArgumentException.class, () -> new Person("9402309995")); // Invalid date
        assertThrows(IllegalArgumentException.class, () -> new Person("9405219996")); // Invalid checksum
    }

    @Test
    void testPayout() {
        Person person = new Person("8004175146");
        assertEquals(0, person.getPaidOutAmount());

        person.payout(1000);
        assertEquals(1000, person.getPaidOutAmount());

        person.payout(500);
        assertEquals(1500, person.getPaidOutAmount());

        assertThrows(IllegalArgumentException.class, () -> person.payout(0));
        assertThrows(IllegalArgumentException.class, () -> person.payout(-100));
    }

    @Test
    void testAddContract() {
        // Create two different persons
        Person policyHolder = new Person("8004175146");
        Person beneficiary = new Person("8054176383");

        // Create a simple company
        InsuranceCompany company = new InsuranceCompany(LocalDate.now().atStartOfDay());

        // Create payment data
        ContractPaymentData paymentData = new ContractPaymentData(
                100,
                PremiumPaymentFrequency.MONTHLY,
                LocalDate.now().atStartOfDay(),
                0);

        // Create a contract
        Vehicle vehicle = new Vehicle("ABC1234", 5000);
        SingleVehicleContract contract = new SingleVehicleContract(
                "C001",
                company,
                beneficiary,
                policyHolder,
                paymentData,
                3000,
                vehicle);

        // Initially, contracts set should be empty
        assertEquals(0, policyHolder.getContracts().size());

        // Add a valid contract
        policyHolder.addContract(contract);
        assertEquals(1, policyHolder.getContracts().size());
        assertTrue(policyHolder.getContracts().contains(contract));

        // Add another contract
        Vehicle vehicle2 = new Vehicle("XYZ789Y", 8000);
        SingleVehicleContract contract2 = new SingleVehicleContract(
                "C002",
                company,
                beneficiary,
                policyHolder,
                paymentData,
                5000,
                vehicle2);

        policyHolder.addContract(contract2);
        assertEquals(2, policyHolder.getContracts().size());
        assertTrue(policyHolder.getContracts().contains(contract2));

        // Test throwing exception with null
        assertThrows(IllegalArgumentException.class, () -> policyHolder.addContract(null));
    }
}