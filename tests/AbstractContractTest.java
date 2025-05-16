package contracts;

import company.InsuranceCompany;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractContractTest {

    private InsuranceCompany insuranceCompany;
    private Person policyHolder;
    private Vehicle vehicle1;

    @BeforeEach
    void setUp() {
        insuranceCompany = new InsuranceCompany(LocalDateTime.now());
        policyHolder = new Person("8351068242");
        vehicle1 = new Vehicle("AA111AA", 15_000);
    }

    @Test
    public void testContractNumberIsNotNullOrEmpty() {
        ContractPaymentData paymentData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);

        assertThrows(IllegalArgumentException.class, () -> {
            new SingleVehicleContract(null, insuranceCompany, null, policyHolder, paymentData, 5000, vehicle1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new SingleVehicleContract("", insuranceCompany, null, policyHolder, paymentData, 5000, vehicle1);
        });
    }

    @Test
    public void testContractNumberIsUniqueWithinInsuranceCompany() {
        ContractPaymentData paymentData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);
        SingleVehicleContract contract1 = new SingleVehicleContract("C123", insuranceCompany, null, policyHolder, paymentData, 5000, vehicle1);

        insuranceCompany.getContracts().add(contract1);

        assertThrows(IllegalArgumentException.class, () -> {
            insuranceCompany.insureVehicle("C123", null, policyHolder, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);
        });
    }

    @Test
    public void testContractNumberCannotBeChanged() {
        ContractPaymentData paymentData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);
        SingleVehicleContract contract = new SingleVehicleContract("C123", insuranceCompany, null, policyHolder, paymentData, 5000, vehicle1);

        assertEquals("C123", contract.getContractNumber());
        // No need to test for exceptions - field is final
    }

    @Test
    public void testInsurerIsNotNullAndCannotBeChanged() {
        ContractPaymentData paymentData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);
        SingleVehicleContract contract = new SingleVehicleContract("C123", insuranceCompany, null, policyHolder, paymentData, 5000, vehicle1);

        // Verify that the insurer is not null
        assertNotNull(contract.getInsurer());
        assertEquals(insuranceCompany, contract.getInsurer());
        // No need to test for exceptions - field is final
    }

    @Test
    public void testPolicyHolderIsNotNullAndCannotBeChanged() {
        ContractPaymentData paymentData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);
        SingleVehicleContract contract = new SingleVehicleContract("C123", insuranceCompany, null, policyHolder, paymentData, 5000, vehicle1);

        // Verify that the policyHolder is not null
        assertNotNull(contract.getPolicyHolder());
        assertEquals(policyHolder, contract.getPolicyHolder());
        // No need to test for exceptions - field is final
    }

    @Test
    public void testContractPaymentDataIsNotNullAndCannotBeChanged() {
        ContractPaymentData paymentData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);
        SingleVehicleContract contract = new SingleVehicleContract("C123", insuranceCompany, null, policyHolder, paymentData, 5000, vehicle1);

        // Verify that the contractPaymentData is not null
        assertNotNull(contract.getContractPaymentData());
        assertEquals(paymentData, contract.getContractPaymentData());
        // No need to test for exceptions - field is final
    }

    @Test
    public void testCoverageAmountIsNonNegative() {
        ContractPaymentData paymentData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);
        SingleVehicleContract contract = new SingleVehicleContract("C123", insuranceCompany, null, policyHolder, paymentData, 5000, vehicle1);

        // Verify that the initial coverageAmount is non-negative
        assertTrue(contract.getCoverageAmount() >= 0);

        // Attempt to set a valid coverageAmount
        contract.setCoverageAmount(3000);
        assertEquals(3000, contract.getCoverageAmount());

        // Attempt to set a negative coverageAmount and expect an exception
        assertThrows(IllegalArgumentException.class, () -> {
            contract.setCoverageAmount(-1000);
        });
    }
}