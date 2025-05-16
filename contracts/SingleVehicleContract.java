package contracts;

import company.InsuranceCompany;
import objects.Person;
import objects.Vehicle;
import payment.ContractPaymentData;

public class SingleVehicleContract extends AbstractVehicleContract {
    private final Vehicle insuredVehicle;

    //Konštruktor:
    public SingleVehicleContract(String contractNumber, InsuranceCompany insurer, Person beneficiary,
                                 Person policyHolder, ContractPaymentData contractPaymentData, int coverageAmount,
                                 Vehicle vehicleToInsure) {
        super(contractNumber, insurer, beneficiary, policyHolder, contractPaymentData, coverageAmount);

        if (contractPaymentData == null) {
            throw new IllegalArgumentException("contractPaymentData nesmie byť null.");
        }

        if (vehicleToInsure == null) {
            throw new IllegalArgumentException("vehicleToInsure nesmie byť null.");
        }

        this.insuredVehicle = vehicleToInsure;
    }

    public Vehicle getInsuredVehicle() {
        return insuredVehicle;
    }
}
