import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import company.InsuranceCompany;
import contracts.InvalidContractException;
import contracts.MasterVehicleContract;
import contracts.SingleVehicleContract;
import contracts.TravelContract;
import objects.LegalForm;
import objects.Person;
import objects.Vehicle;
import payment.ContractPaymentData;
import payment.PaymentHandler;
import payment.PaymentInstance;
import payment.PremiumPaymentFrequency;

/**
 * Additional tests for the insurance system implementation.
 * 
 * These tests cover functionality not addressed in the RequiredTests.java file:
 * - Person validation (rodné číslo, IČO)
 * - Vehicle validation
 * - ContractPaymentData behavior
 * - TravelContract functionality
 * - MasterVehicleContract functionality
 * - Contract transfers between individual and master contracts
 * - Claim processing
 * - Payment handling
 */
public class AdditionalTests {

    InsuranceCompany insuranceCompany;
    Person naturalPerson1;
    Person naturalPerson2;
    Person naturalPerson3;
    Person legalPerson1;
    Person legalPerson2;
    Vehicle vehicle1;
    Vehicle vehicle2;
    Vehicle vehicle3;
    LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2025, 4, 15, 12, 0);
        insuranceCompany = new InsuranceCompany(testTime);
        // Valid rodné číslo for natural persons
        naturalPerson1 = new Person("8351068242"); // Male born in 1983
        naturalPerson2 = new Person("0402114911"); // Female born in 2004 (RČ with +50 to month)
        naturalPerson3 = new Person("530512123"); // 9-digit RČ (pre-1954)
        // Valid IČO for legal persons
        legalPerson1 = new Person("12345678"); // 8-digit IČO
        legalPerson2 = new Person("123456"); // 6-digit IČO
        vehicle1 = new Vehicle("AA111AA", 15_000);
        vehicle2 = new Vehicle("BANAN22", 22_000);
        vehicle3 = new Vehicle("SOMRYBA", 8_000);
    }

    // SECTION 1: TESTING PERSON VALIDATION

    @Test
    public void testPersonValidation() {
        // Test legal form detection
        assertEquals(LegalForm.NATURAL, naturalPerson1.getLegalForm());
        assertEquals(LegalForm.NATURAL, naturalPerson2.getLegalForm());
        assertEquals(LegalForm.NATURAL, naturalPerson3.getLegalForm());
        assertEquals(LegalForm.LEGAL, legalPerson1.getLegalForm());
        assertEquals(LegalForm.LEGAL, legalPerson2.getLegalForm());

        // Test invalid IDs
        assertThrows(IllegalArgumentException.class, () -> new Person(null)); // null ID
        assertThrows(IllegalArgumentException.class, () -> new Person("")); // empty ID
        assertThrows(IllegalArgumentException.class, () -> new Person("12345")); // too short IČO
        assertThrows(IllegalArgumentException.class, () -> new Person("123456789")); // too long IČO
        assertThrows(IllegalArgumentException.class, () -> new Person("12A45678")); // IČO with non-digits

        // Test invalid rodné číslo
        assertThrows(IllegalArgumentException.class, () -> new Person("12345678901")); // too long
        assertThrows(IllegalArgumentException.class, () -> new Person("83A1068242")); // non-digits
        assertThrows(IllegalArgumentException.class, () -> new Person("8313068242")); // invalid month (13)
        assertThrows(IllegalArgumentException.class, () -> new Person("8300068242")); // invalid month (00)
        assertThrows(IllegalArgumentException.class, () -> new Person("8351328242")); // invalid day (32)
        assertThrows(IllegalArgumentException.class, () -> new Person("8351008242")); // invalid day (00)

        // Test payout method
        assertEquals(0, naturalPerson1.getPaidOutAmount()); // Initial value is 0
        naturalPerson1.payout(500);
        assertEquals(500, naturalPerson1.getPaidOutAmount());
        naturalPerson1.payout(1000);
        assertEquals(1500, naturalPerson1.getPaidOutAmount()); // Accumulates

        // Test invalid payout
        assertThrows(IllegalArgumentException.class, () -> naturalPerson1.payout(0)); // Zero is invalid
        assertThrows(IllegalArgumentException.class, () -> naturalPerson1.payout(-100)); // Negative is invalid
    }

    // SECTION 2: TESTING VEHICLE VALIDATION

    @Test
    public void testVehicleValidation() {
        // Valid license plate and value
        Vehicle validVehicle = new Vehicle("ABC1234", 10000);
        assertEquals("ABC1234", validVehicle.getLicensePlate());
        assertEquals(10000, validVehicle.getOriginalValue());

        // Invalid license plate
        assertThrows(IllegalArgumentException.class, () -> new Vehicle(null, 10000)); // null
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("", 10000)); // empty
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("AB2345", 10000)); // too short
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("AB123456", 10000)); // too long
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("AB12-34", 10000)); // invalid character
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("ab12345", 10000)); // lowercase letters

        // Invalid value
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("ABC1234", 0)); // zero
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("ABC1234", -100)); // negative
    }

    // SECTION 3: TESTING CONTRACT PAYMENT DATA

    @Test
    public void testContractPaymentData() {
        LocalDateTime paymentTime = LocalDateTime.of(2025, 1, 1, 0, 0);

        // Valid data
        ContractPaymentData data = new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY, paymentTime, 0);
        assertEquals(100, data.getPremium());
        assertEquals(PremiumPaymentFrequency.MONTHLY, data.getPremiumPaymentFrequency());
        assertEquals(paymentTime, data.getNextPaymentTime());
        assertEquals(0, data.getOutstandingBalance());

        // Invalid data
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(0, PremiumPaymentFrequency.MONTHLY, paymentTime, 0)); // zero premium
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(-10, PremiumPaymentFrequency.MONTHLY, paymentTime, 0)); // negative
                                                                                                      // premium
        assertThrows(IllegalArgumentException.class, () -> new ContractPaymentData(100, null, paymentTime, 0)); // null
                                                                                                                // frequency
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY, null, 0)); // null payment time

        // Test update next payment time
        data.updateNextPaymentTime();
        // For MONTHLY, should add 1 month
        assertEquals(paymentTime.plusMonths(1), data.getNextPaymentTime());

        // Test with QUARTERLY frequency
        data.setPremiumPaymentFrequency(PremiumPaymentFrequency.QUARTERLY);
        data.updateNextPaymentTime();
        // For QUARTERLY, should add 3 months
        assertEquals(paymentTime.plusMonths(1).plusMonths(3), data.getNextPaymentTime());

        // Test with SEMI_ANNUAL frequency
        data.setPremiumPaymentFrequency(PremiumPaymentFrequency.SEMI_ANNUAL);
        data.updateNextPaymentTime();
        // For SEMI_ANNUAL, should add 6 months
        assertEquals(paymentTime.plusMonths(1).plusMonths(3).plusMonths(6), data.getNextPaymentTime());

        // Test with ANNUAL frequency
        data.setPremiumPaymentFrequency(PremiumPaymentFrequency.ANNUAL);
        data.updateNextPaymentTime();
        // For ANNUAL, should add 12 months
        assertEquals(paymentTime.plusMonths(1).plusMonths(3).plusMonths(6).plusMonths(12), data.getNextPaymentTime());
    }

    // SECTION 4: TESTING TRAVEL CONTRACT

    @Test
    public void testTravelContract() {
        // Create a travel contract
        ContractPaymentData data = new ContractPaymentData(50, PremiumPaymentFrequency.ANNUAL, testTime, 0);
        Set<Person> insuredPersons = new HashSet<>();
        insuredPersons.add(naturalPerson1);
        insuredPersons.add(naturalPerson2);

        // Test valid construction
        // Constructor: (contractNumber, insurer, policyHolder, contractPaymentData,
        // coverageAmount, personsToInsure)
        TravelContract travelContract = new TravelContract("T1", insuranceCompany, legalPerson1, data, 100,
                insuredPersons);
        assertEquals("T1", travelContract.getContractNumber());
        assertEquals(insuranceCompany, travelContract.getInsurer());
        assertEquals(legalPerson1, travelContract.getPolicyHolder());
        assertEquals(data, travelContract.getContractPaymentData());
        assertEquals(100, travelContract.getCoverageAmount());
        assertEquals(insuredPersons, travelContract.getInsuredPersons());
        assertTrue(travelContract.isActive());

        // Test invalid construction
        // Empty insured persons set
        Set<Person> emptySet = new HashSet<>();
        assertThrows(IllegalArgumentException.class,
                () -> new TravelContract("T2", insuranceCompany, legalPerson1, data, 100, emptySet));

        // Null insured persons set
        assertThrows(IllegalArgumentException.class,
                () -> new TravelContract("T2", insuranceCompany, legalPerson1, data, 100, null));

        // Null payment data
        assertThrows(IllegalArgumentException.class,
                () -> new TravelContract("T2", insuranceCompany, legalPerson1, null, 100, insuredPersons));

        // Check that legal person can be policy holder for travel contract
        // (Not forbidden in the assignment)
        TravelContract travelContractWithLegalPerson = new TravelContract(
                "T3", insuranceCompany, legalPerson1, data, 100, insuredPersons);
        assertEquals(legalPerson1, travelContractWithLegalPerson.getPolicyHolder());
    }

    // SECTION 5: TESTING INSURE PERSONS (TRAVEL CONTRACT CREATION BY INSURANCE
    // COMPANY)

    @Test
    public void testInsurePersons() {
        // Create a set of persons to insure
        Set<Person> insuredPersons = new HashSet<>();
        insuredPersons.add(naturalPerson1);
        insuredPersons.add(naturalPerson2);

        // Test valid insurance
        // Method signature: insurePersons(contractNumber, policyHolder, premium,
        // premiumPaymentFrequency, personsToInsure)
        TravelContract contract = insuranceCompany.insurePersons(
                "TP1", naturalPerson3, 30, PremiumPaymentFrequency.QUARTERLY, insuredPersons);

        // Verify contract properties
        assertEquals("TP1", contract.getContractNumber());
        assertEquals(naturalPerson3, contract.getPolicyHolder());
        assertEquals(insuredPersons, contract.getInsuredPersons());
        // Coverage amount should be 10 * number of insured persons
        assertEquals(20, contract.getCoverageAmount()); // 10 * 2 persons
        // Payment data should be set correctly
        assertEquals(30, contract.getContractPaymentData().getPremium());
        assertEquals(PremiumPaymentFrequency.QUARTERLY, contract.getContractPaymentData().getPremiumPaymentFrequency());
        // Next payment should be set to current time
        // assertEquals(insuranceCompany.getCurrentTime(), contract.getContractPaymentData().getNextPaymentTime());
        // Outstanding balance should be charged immediately
        assertEquals(30, contract.getContractPaymentData().getOutstandingBalance());

        // Verify contract is in the insurance company's set
        assertTrue(insuranceCompany.getContracts().contains(contract));
        // Verify contract is in the policy holder's set
        assertTrue(naturalPerson3.getContracts().contains(contract));

        // Test invalid insurance - premium too low
        // Should be at least 5 * number of insured persons annually
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insurePersons("TP2", naturalPerson3, 9,
                PremiumPaymentFrequency.ANNUAL, insuredPersons)); // 9 < 5*2

        // Test duplicate contract number
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insurePersons("TP1", naturalPerson3, 30,
                PremiumPaymentFrequency.QUARTERLY, insuredPersons));
    }

    // SECTION 6: TESTING MASTER VEHICLE CONTRACT

    @Test
    public void testMasterVehicleContract() {
        // Only legal person can be policy holder for master contract
        assertThrows(IllegalArgumentException.class,
                () -> new MasterVehicleContract("MV1", insuranceCompany, null, naturalPerson1));

        // Create a valid master contract with legal person
        MasterVehicleContract masterContract = new MasterVehicleContract("MV1", insuranceCompany, null, legalPerson1);

        // Verify properties
        assertEquals("MV1", masterContract.getContractNumber());
        assertEquals(insuranceCompany, masterContract.getInsurer());
        assertNull(masterContract.getBeneficiary());
        assertEquals(legalPerson1, masterContract.getPolicyHolder());
        // Payment data should be null
        assertNull(masterContract.getContractPaymentData());
        // Coverage amount should be 0
        assertEquals(0, masterContract.getCoverageAmount());
        // Child contracts should be empty
        assertTrue(masterContract.getChildContracts().isEmpty());
        // Contract should be active
        assertTrue(masterContract.isActive());

        // Test create master contract through insurance company
        MasterVehicleContract masterContract2 = insuranceCompany.createMasterVehicleContract("MV2", null, legalPerson1);

        // Verify contract is in the insurance company's set
        assertTrue(insuranceCompany.getContracts().contains(masterContract2));
        // Verify contract is in the policy holder's set
        assertTrue(legalPerson1.getContracts().contains(masterContract2));

        // Test duplicate contract number
        assertThrows(IllegalArgumentException.class,
                () -> insuranceCompany.createMasterVehicleContract("MV2", null, legalPerson1));
    }

    // SECTION 7: TESTING CONTRACT TRANSFERS

    @Test
    public void testMoveSingleVehicleContractToMasterVehicleContract() {
        // Create a single vehicle contract
        SingleVehicleContract singleContract = insuranceCompany.insureVehicle(
                "SV1", null, legalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);

        // Create a master contract
        MasterVehicleContract masterContract = insuranceCompany.createMasterVehicleContract("MV1", null, legalPerson1);

        // Move the single contract to the master contract
        // Method signature:
        // moveSingleVehicleContractToMasterVehicleContract(masterVehicleContract,
        // singleVehicleContract)
        insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(masterContract, singleContract);

        // Verify single contract is removed from insurance company's contracts
        assertFalse(insuranceCompany.getContracts().contains(singleContract));
        // Verify single contract is removed from policy holder's contracts
        assertFalse(legalPerson1.getContracts().contains(singleContract));
        // Verify single contract is added to master contract's child contracts
        assertTrue(masterContract.getChildContracts().contains(singleContract));

        // Test null arguments
        assertThrows(IllegalArgumentException.class,
                () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(masterContract, null));
        assertThrows(IllegalArgumentException.class,
                () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(null, singleContract));

        // Test different policy holders
        SingleVehicleContract otherContract = insuranceCompany.insureVehicle(
                "SV2", null, naturalPerson1, 440, PremiumPaymentFrequency.ANNUAL, vehicle2);
        assertThrows(InvalidContractException.class,
                () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(masterContract, otherContract));

        // Test inactive contracts
        SingleVehicleContract inactiveContract = insuranceCompany.insureVehicle(
                "SV3", null, legalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle3);
        inactiveContract.setInactive();
        assertThrows(InvalidContractException.class, () -> insuranceCompany
                .moveSingleVehicleContractToMasterVehicleContract(masterContract, inactiveContract));

        MasterVehicleContract inactiveMaster = insuranceCompany.createMasterVehicleContract("MV2", null, legalPerson1);
        inactiveMaster.setInactive();
        SingleVehicleContract yetAnotherContract = insuranceCompany.insureVehicle(
                "SV4", null, legalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle3);
        assertThrows(InvalidContractException.class, () -> insuranceCompany
                .moveSingleVehicleContractToMasterVehicleContract(inactiveMaster, yetAnotherContract));
    }

    // SECTION 8: TESTING PAYMENT HANDLING

    @Test
    public void testPaymentHandler() {
        // Test constructor validation
        assertThrows(IllegalArgumentException.class, () -> new PaymentHandler(null));

        // Create payment handler
        PaymentHandler handler = new PaymentHandler(insuranceCompany);

        // Create a contract
        SingleVehicleContract contract = insuranceCompany.insureVehicle(
                "SV1", null, naturalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);

        // Test payment
        handler.pay(contract, 200);

        // Verify payment reduced the outstanding balance
        assertEquals(100, contract.getContractPaymentData().getOutstandingBalance()); // 300 - 200

        // Verify payment was recorded in history
        assertTrue(handler.getPaymentHistory().containsKey(contract));
        assertEquals(1, handler.getPaymentHistory().get(contract).size());
        PaymentInstance payment = handler.getPaymentHistory().get(contract).iterator().next();
        assertEquals(200, payment.getPaymentAmount());
        assertEquals(insuranceCompany.getCurrentTime(), payment.getPaymentTime());

        // Test invalid payments
        assertThrows(IllegalArgumentException.class, () -> handler.pay(contract, 0)); // Zero amount
        assertThrows(IllegalArgumentException.class, () -> handler.pay(contract, -100)); // Negative amount
        assertThrows(IllegalArgumentException.class, () -> handler.pay(null, 100)); // Null contract

        // Test paying inactive contract
        contract.setInactive();
        assertThrows(InvalidContractException.class, () -> handler.pay(contract, 100));

        // Test paying contract from different insurer
        InsuranceCompany otherCompany = new InsuranceCompany(testTime);
        SingleVehicleContract otherContract = otherCompany.insureVehicle(
                "OC1", null, naturalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);
        assertThrows(InvalidContractException.class, () -> handler.pay(otherContract, 100));
    }

    // SECTION 9: TESTING PREMIUM CHARGING

    @Test
    public void testChargePremiumOnContracts() {
        // Create a contract with monthly frequency
        SingleVehicleContract contract = insuranceCompany.insureVehicle(
                "SV1", null, naturalPerson1, 100, PremiumPaymentFrequency.MONTHLY, vehicle1);

        // Verify initial state
        assertEquals(100, contract.getContractPaymentData().getOutstandingBalance());
        //assertEquals(testTime, contract.getContractPaymentData().getNextPaymentTime());

        // Advance time by 2 months
        LocalDateTime newTime = testTime.plusMonths(2);
        insuranceCompany.setCurrentTime(newTime);

        // Charge premiums
        insuranceCompany.chargePremiumsOnContracts();

        // Verify two monthly premiums were charged
        assertEquals(300, contract.getContractPaymentData().getOutstandingBalance()); // 100 + 100 + 100
        // Next payment should be 2 months after initial
        //assertEquals(testTime.plusMonths(2), contract.getContractPaymentData().getNextPaymentTime());
    }

    // SECTION 10: TESTING CLAIM PROCESSING

    @Test
    public void testProcessClaimSingleVehicleContract() {
        // Create a contract with beneficiary
        SingleVehicleContract contract = insuranceCompany.insureVehicle(
                "SV1", naturalPerson2, naturalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);
        contract.setCoverageAmount(5000);

        // Process claim with minor damage (less than 70% of vehicle value)
        insuranceCompany.processClaim(contract, 5000);

        // Verify beneficiary received payment
        assertEquals(5000, naturalPerson2.getPaidOutAmount());
        // Contract should still be active
        assertTrue(contract.isActive());

        // Process claim with total damage (>=70% of vehicle value)
        SingleVehicleContract contract2 = insuranceCompany.insureVehicle(
                "SV2", null, naturalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);
        contract2.setCoverageAmount(5000);

        insuranceCompany.processClaim(contract2, 10500); // 70% of 15000 = 10500

        // Verify policy holder received payment (no beneficiary)
        assertEquals(5000, naturalPerson1.getPaidOutAmount());
        // Contract should be inactive due to total damage
        assertFalse(contract2.isActive());

        // Test invalid arguments
        assertThrows(IllegalArgumentException.class,
                () -> insuranceCompany.processClaim((SingleVehicleContract) null, 5000));
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(contract, 0)); // Zero damage
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(contract, -100)); // Negative
                                                                                                           // damage

        // Test inactive contract
        contract.setInactive();
        assertThrows(InvalidContractException.class, () -> insuranceCompany.processClaim(contract, 5000));
    }

    @Test
    public void testProcessClaimTravelContract() {
        // Create a travel contract with multiple insured persons
        Set<Person> insuredPersons = new HashSet<>();
        insuredPersons.add(naturalPerson1);
        insuredPersons.add(naturalPerson2);
        insuredPersons.add(naturalPerson3);

        TravelContract contract = insuranceCompany.insurePersons(
                "TP1", legalPerson1, 30, PremiumPaymentFrequency.QUARTERLY, insuredPersons);
        contract.setCoverageAmount(300);

        // Process claim affecting only some insured persons
        Set<Person> affectedPersons = new HashSet<>();
        affectedPersons.add(naturalPerson1);
        affectedPersons.add(naturalPerson2);

        insuranceCompany.processClaim(contract, affectedPersons);

        // Verify affected persons received payment (300 / 2 = 150 each)
        assertEquals(150, naturalPerson1.getPaidOutAmount());
        assertEquals(150, naturalPerson2.getPaidOutAmount());
        assertEquals(0, naturalPerson3.getPaidOutAmount()); // Not affected

        // Contract should be inactive after claim
        assertFalse(contract.isActive());

        // Test invalid arguments
        assertThrows(IllegalArgumentException.class,
                () -> insuranceCompany.processClaim((TravelContract) null, affectedPersons));
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(contract, null)); // Null
                                                                                                           // affected
                                                                                                           // persons
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(contract, new HashSet<>())); // Empty
                                                                                                                      // affected
                                                                                                                      // persons

        // Test with person not in insured persons
        Set<Person> invalidPersons = new HashSet<>();
        invalidPersons.add(legalPerson2); // Not in insured persons

        TravelContract contract2 = insuranceCompany.insurePersons(
                "TP2", legalPerson1, 30, PremiumPaymentFrequency.QUARTERLY, insuredPersons);

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(contract2, invalidPersons));

        // Test inactive contract
        contract2.setInactive();
        assertThrows(InvalidContractException.class, () -> insuranceCompany.processClaim(contract2, affectedPersons));
    }

    // SECTION 11: TESTING MASTER CONTRACT ACTIVE STATUS

    @Test
    public void testMasterContractActiveStatus() {
        // Create a master contract
        MasterVehicleContract masterContract = insuranceCompany.createMasterVehicleContract("MV1", null, legalPerson1);

        // Initially should be active
        assertTrue(masterContract.isActive());

        // Should be active if it has no child contracts
        assertTrue(masterContract.getChildContracts().isEmpty());
        assertTrue(masterContract.isActive());

        // Add some child contracts
        SingleVehicleContract child1 = insuranceCompany.insureVehicle(
                "C1", null, legalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);
        SingleVehicleContract child2 = insuranceCompany.insureVehicle(
                "C2", null, legalPerson1, 450, PremiumPaymentFrequency.ANNUAL, vehicle2);

        insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(masterContract, child1);
        insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(masterContract, child2);

        // Master should be active when all children are active
        assertTrue(masterContract.isActive());

        // Deactivate one child
        child1.setInactive();

        // Master should still be active with at least one active child
        assertTrue(masterContract.isActive());

        // Deactivate all children
        child2.setInactive();

        // Master should be inactive when all children are inactive
        assertFalse(masterContract.isActive());

        // Setting master inactive should set all children inactive
        MasterVehicleContract anotherMaster = insuranceCompany.createMasterVehicleContract("MV2", null, legalPerson1);
        SingleVehicleContract anotherChild1 = insuranceCompany.insureVehicle(
                "AC1", null, legalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle3);
        SingleVehicleContract anotherChild2 = insuranceCompany.insureVehicle(
                "AC2", null, legalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle3);

        insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(anotherMaster, anotherChild1);
        insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(anotherMaster, anotherChild2);

        // All should be active initially
        assertTrue(anotherMaster.isActive());
        assertTrue(anotherChild1.isActive());
        assertTrue(anotherChild2.isActive());

        // Set master inactive
        anotherMaster.setInactive();

        // Verify all are inactive
        assertFalse(anotherMaster.isActive());
        assertFalse(anotherChild1.isActive());
        assertFalse(anotherChild2.isActive());
    }
}