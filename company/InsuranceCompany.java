package company;

import contracts.*;
import objects.Person;
import objects.Vehicle;
import payment.*;
import java.time.LocalDateTime;
import java.util.*;

public class InsuranceCompany {
    private final Set<AbstractContract> contracts;
    private final PaymentHandler handler;
    private LocalDateTime currentTime;

    //konštruktor:
    public InsuranceCompany(LocalDateTime currentTime) {
        if (currentTime == null) {
            throw new IllegalArgumentException("Čas nesmie byť null.");
        }
        this.currentTime = currentTime;
        this.contracts = new LinkedHashSet<>(); // zachováva poradie
        this.handler = new PaymentHandler(this);
    }

    public LocalDateTime getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(LocalDateTime currentTime) {
        if (currentTime == null) {
            throw new IllegalArgumentException("Čas nesmie byť null.");
        }
        this.currentTime = currentTime;
    }

    public Set<AbstractContract> getContracts() {
        //return Collections.unmodifiableSet(contracts);
        return contracts;
    }

    public PaymentHandler getHandler() {
        return handler;
    }

    public SingleVehicleContract insureVehicle(
            String contractNumber,
            Person beneficiary,
            Person policyHolder,
            int proposedPremium,
            PremiumPaymentFrequency proposedPaymentFrequency,
            Vehicle vehicleToInsure
    ) {
        // Kontrola potrebných parametrov (v zadani sa hovorí o nevalidných hodnotách)
        if (contractNumber == null || contractNumber.isEmpty()
                || policyHolder == null
                || proposedPaymentFrequency == null
                || vehicleToInsure == null) {
            throw new IllegalArgumentException("Žiadny parameter nesmie byť null alebo prázdny.");
        }

        if (contractExists(contractNumber)) {  //kazde cislo zmluvy musi byt unikatne
            throw new IllegalArgumentException("Zmluva s týmto číslom už v danej poistovni jestvuje");
        }

        if (proposedPremium <= 0) {
            throw new IllegalArgumentException("Navrhnuté poistné musí byť kladné.");
        }

        // Výpočet počtu platieb za rok podľa frekvencie
        int factor=0;   //musim to tu nastavit, lebo inak ma java problem, a zaroven nechcem dat default,
                        //lebo to v zadani explicitne nie je napisane
        switch (proposedPaymentFrequency) {
            case MONTHLY -> factor = 12;
            case QUARTERLY -> factor = 4;
            case SEMI_ANNUAL -> factor = 2;
            case ANNUAL -> factor = 1;
            //default sem nedavam lebo to v zadani nie je explicitne napisane
            //default -> throw new IllegalArgumentException("Neznáma frekvencia platby.");
        }

        //Výpočet ročnej sumy a kontrola voči minimu (2 percenta z hodnoty vozidla)
        int annualPayment = proposedPremium * factor;
        int minRequired = (int) Math.ceil(vehicleToInsure.getOriginalValue() * 0.02);
        if (annualPayment < minRequired) {
            throw new IllegalArgumentException("Ročná platba musi byt vacsia alebo rovna rovná 2% z ceny vozidla");
        }

        //Vytvorim nove platobne udaje
        ContractPaymentData paymentData = new ContractPaymentData(
                proposedPremium,
                proposedPaymentFrequency,
                currentTime,
                0
        );

        //Vytvorim zmluvu s coverageAmount = polovica hodnoty vozidla
        SingleVehicleContract contract = new SingleVehicleContract(
                contractNumber,
                this,
                beneficiary,
                policyHolder,
                paymentData,
                vehicleToInsure.getOriginalValue() / 2,
                vehicleToInsure
        );

        chargePremiumOnContract(contract);
        contracts.add(contract);
        policyHolder.addContract(contract);
        return contract;
    }

    private boolean contractExists(String contractNumber) {
        for (AbstractContract c : contracts) {
            if (c.getContractNumber().equals(contractNumber)) {
                return true;
            }
        }
        return false;
    }

    public TravelContract insurePersons(
            String contractNumber,
            Person policyHolder,
            int proposedPremium,
            PremiumPaymentFrequency proposedPaymentFrequency,
            Set<Person> personsToInsure
    ) {
        //musim kontrolovat vsetky tieto parametre lebo su pouzivane vo vypoctoch
        if (contractNumber == null || policyHolder == null
                || proposedPaymentFrequency == null || personsToInsure == null) {
            throw new IllegalArgumentException("Žiadny parameter nesmie byť null.");
        }

        if (personsToInsure.isEmpty()) {
            throw new IllegalArgumentException("Množina poistených osôb nesmie byť prázdna.");
        }

        if (contractExists(contractNumber)) {
            throw new IllegalArgumentException("Zmluva s týmto číslom už jestvuje.");
        }

        if (proposedPremium <= 0) {
            throw new IllegalArgumentException("proposedPremium musí byť kladné.");
        }

        int annualPayment = proposedPremium * (12 / proposedPaymentFrequency.getValueInMonths());
        int minimumRequired = personsToInsure.size() * 5;
        if (annualPayment < minimumRequired) {
            throw new IllegalArgumentException("Ročná platba musi byt vacsia alebo rovna patnasobku poctu poistenych osob.");
        }

        //vytvorim platobne udaje
        ContractPaymentData paymentData = new ContractPaymentData(
                proposedPremium,
                proposedPaymentFrequency,
                currentTime,
                0
        );

        //vytvorim novu zmluvu
        TravelContract contract = new TravelContract(
                contractNumber,
                this,
                policyHolder,
                paymentData,
                personsToInsure.size() * 10,
                personsToInsure
        );

        //aktualizujem nedoplatok, ulozim zmluvu
        chargePremiumOnContract(contract);
        contracts.add(contract);
        policyHolder.addContract(contract);
        return contract;
    }

    public MasterVehicleContract createMasterVehicleContract(
            String contractNumber,
            Person beneficiary,
            Person policyHolder
    ) {
        if (contractNumber == null || policyHolder == null) {
            throw new IllegalArgumentException("contractnumber ani policyholder nesmu byť null.");
        }

        if (contractExists(contractNumber)) {
            throw new IllegalArgumentException("Zmluva s týmto číslom už jestvuje.");
        }

        //Vytvorim MasterVehicleContract
        MasterVehicleContract contract = new MasterVehicleContract(
                contractNumber,
                this,
                beneficiary,
                policyHolder);

        //ulozim zmluvu
        contracts.add(contract);
        policyHolder.addContract(contract);
        return contract;
    }

    public void moveSingleVehicleContractToMasterVehicleContract(
            MasterVehicleContract masterVehicleContract,
            SingleVehicleContract singleVehicleContract

    ) {
        //nic nesmie byt null
        if (singleVehicleContract == null || masterVehicleContract == null) {
            throw new IllegalArgumentException("Žiadny parameter nesmie byť null.");
        }

        //Obe zmluvy musia byť aktívne
        if (!singleVehicleContract.isActive() || !masterVehicleContract.isActive()) {
            throw new InvalidContractException("Obe zmluvy musia byť aktívne.");
        }

        //Obe zmluvy musia patriť jednej poisťovni
        if (!singleVehicleContract.getInsurer().equals(this)
                || !masterVehicleContract.getInsurer().equals(this)) {
            throw new InvalidContractException("Zmluvy musia patriť rovnakej poisťovni.");
        }

        //obe zmluvy musia mat rovnakeho poistnika
        if (!singleVehicleContract.getPolicyHolder().equals(masterVehicleContract.getPolicyHolder())) {
            throw new InvalidContractException("Zmluvy musia mať rovnakého poistníka.");
        }

        //odstranim zmluvu tu
        contracts.remove(singleVehicleContract);
        singleVehicleContract.getPolicyHolder().getContracts().remove(singleVehicleContract);

        //Pridam do mastervehicle contract
        //masterVehicleContract.getChildContracts().add(singleVehicleContract);
        masterVehicleContract.addChildContract(singleVehicleContract);
    }

    public void chargePremiumOnContract(AbstractContract contract) {
        ContractPaymentData data = contract.getContractPaymentData();

        //ak je termin splatnosti rovny alebo alebo pred casom currenttime
        while (!data.getNextPaymentTime().isAfter(currentTime)) {
            //zvysi nedoplatok o hodnotu premium
            int newBalance = data.getOutstandingBalance() + data.getPremium();
            data.setOutstandingBalance(newBalance);
            data.updateNextPaymentTime(); //updatene cas splatnosti
        }
    }

    public void chargePremiumOnContract(MasterVehicleContract contract) {
        for (SingleVehicleContract child : contract.getChildContracts()) {
            chargePremiumOnContract(child);
        }
    }

    public void chargePremiumsOnContracts() {
        for (AbstractContract contract : contracts) {
            if (contract.isActive()) {
                contract.updateBalance();
            }
        }
    }

    public void processClaim(SingleVehicleContract singleVehicleContract, int expectedDamages) {
        if (singleVehicleContract == null || expectedDamages <= 0) {
            throw new IllegalArgumentException("Neplatné vstupné údaje.");
        }

        if (!singleVehicleContract.isActive()) {
            throw new InvalidContractException("Zmluva nie je aktívna.");
        }

        //Vyplatenie poistneho plnenia
        int payoutAmount = singleVehicleContract.getCoverageAmount();
        Person recipient = singleVehicleContract.getBeneficiary();

        if (recipient != null) {
            recipient.payout(payoutAmount);
        } else {
            singleVehicleContract.getPolicyHolder().payout(payoutAmount);
        }

        //ak je skoda vacsia ako 70 percent ceny vozidla
        int vehicleValue = singleVehicleContract.getInsuredVehicle().getOriginalValue();
        if (expectedDamages >= (int)(vehicleValue * 0.7)) {
            singleVehicleContract.setInactive();
        }
    }

    public void processClaim(TravelContract travelContract, Set<Person> affectedPersons) {
        if (travelContract == null || affectedPersons == null || affectedPersons.isEmpty()) {
            throw new IllegalArgumentException("Neplatné vstupné údaje.");
        }

        if (!travelContract.getInsuredPersons().containsAll(affectedPersons)) {
            throw new IllegalArgumentException("Neplatné osoby – musia byť medzi poistenými.");
        }

        if (!travelContract.isActive()) {
            throw new InvalidContractException("Zmluva nie je aktívna.");
        }

        int payoutAmount = travelContract.getCoverageAmount() / affectedPersons.size();

        for (Person person : affectedPersons) {
            person.payout(payoutAmount);
        }

        travelContract.setInactive();
    }
}
