package contracts;

import company.InsuranceCompany;
import objects.Person;
import objects.LegalForm;
import payment.ContractPaymentData;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class MasterVehicleContract extends AbstractVehicleContract {
    private final Set<SingleVehicleContract> childContracts;

    //Konštruktor:
    public MasterVehicleContract(String contractNumber, InsuranceCompany insurer, Person beneficiary, Person policyHolder) {
        super(contractNumber, insurer, beneficiary, policyHolder, null, 0);

        if (policyHolder.getLegalForm() != LegalForm.LEGAL) {
            throw new IllegalArgumentException("Poistník musí byť právnická osoba.");
        }

        this.childContracts = new LinkedHashSet<>();
    }

    public Set<SingleVehicleContract> getChildContracts() {
        return childContracts;
    }

    public void requestAdditionOfChildContract(SingleVehicleContract contract) {
        if (contract == null) {
            throw new IllegalArgumentException("Pridana zmluva contract nesmie byť null.");
        }
        //poziada poisťovňu o presun single do master
        getInsurer().moveSingleVehicleContractToMasterVehicleContract(this, contract);
    }

    public void addChildContract(SingleVehicleContract contract) {
        this.childContracts.add(contract);
    }

    @Override
    public void pay(int amount) {
        insurer.getHandler().pay(this, amount);
    }

    @Override
    public void setInactive() {
        for (SingleVehicleContract contract : childContracts) {
            contract.setInactive(); //Nastaví všetky dcerske zmluvy ako neaktívne
        }
        super.setInactive();  //Nastavi aj seba ako neaktívnu
    }

    @Override
    public boolean isActive() {
        if (childContracts.isEmpty()) {
            return super.isActive(); // ak nemá dcerske zmluvy, vrati svoj stav
        }
        for (SingleVehicleContract contract : childContracts) {
            if (contract.isActive()) {
                return true; // aspoň jedna dcerska zmluva je aktivna
            }
        }
        return false; // ziadne dcerske zmluvy nie su aktivne
    }

    //toto som doplnil ako posledne
    @Override
    public void updateBalance() {
        insurer.chargePremiumOnContract(this); //zavola specialnu verziu pre Mastervehicle contract
    }
}
