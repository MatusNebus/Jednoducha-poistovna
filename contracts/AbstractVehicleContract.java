package contracts;

import company.InsuranceCompany;
import objects.Person;
import payment.ContractPaymentData;

public abstract class AbstractVehicleContract extends AbstractContract {
    protected Person beneficiary; //beneficiary je oprávnená osoba

    //Konštruktor:
    public AbstractVehicleContract(String contractNumber, InsuranceCompany insurer, Person beneficiary,
                                   Person policyHolder, ContractPaymentData contractPaymentData, int coverageAmount) {
        super(contractNumber, insurer, policyHolder, contractPaymentData, coverageAmount);

        // Beneficiary môže byť podla zadania null, ale ak je rovnaký ako policyHolder, vyhodíme výnimku
        //vraj nemozes zavolat equals() na nieco co je null, lebo to vyhodi chybu
        if (beneficiary != null && beneficiary.equals(policyHolder)) {
            throw new IllegalArgumentException("Beneficient nemôže byť rovnaký ako poistník.");
        }

        this.beneficiary = beneficiary;
    }

    public void setBeneficiary(Person beneficiary) {
        // Beneficiary môže byť podla zadania null, ale ak je rovnaký ako policyHolder, vyhodíme výnimku
        if (beneficiary != null && beneficiary.equals(getPolicyHolder())) {
            throw new IllegalArgumentException("Beneficient nemôže byť rovnaký ako poistník.");
        }
        this.beneficiary = beneficiary;
    }

    public Person getBeneficiary() {
        return beneficiary;
    }
}
