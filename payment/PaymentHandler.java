package payment;

import company.InsuranceCompany;
import contracts.AbstractContract;
import contracts.MasterVehicleContract;
import contracts.SingleVehicleContract;
import contracts.InvalidContractException;
import java.util.*;

public class PaymentHandler {
    private final Map<AbstractContract, Set<PaymentInstance>> paymentHistory;
    private final InsuranceCompany insurer;

    //konštruktor:
    public PaymentHandler(InsuranceCompany insurer) {
        if (insurer == null) {
            throw new IllegalArgumentException("Poistovna insurer nesmie byť null.");
        }
        this.insurer = insurer;
        this.paymentHistory = new HashMap<>();
    }

    public Map<AbstractContract, Set<PaymentInstance>> getPaymentHistory() {
        return Collections.unmodifiableMap(paymentHistory);
    }

    public void pay(AbstractContract contract, int amount) {
        if (contract == null || amount <= 0) {
            throw new IllegalArgumentException("Zmluva nesmie byť null a amount musí byť kladný (nesmie byt nekladny...)");
        }
        if (!contract.isActive() || !contract.getInsurer().equals(insurer)) {
            throw new InvalidContractException("Neplatná zmluva.");
        }
        //znizim outstandingBalance o amount
        int newBalance = contract.getContractPaymentData().getOutstandingBalance() - amount;
        contract.getContractPaymentData().setOutstandingBalance(newBalance);

        //
        PaymentInstance instance = new PaymentInstance(insurer.getCurrentTime(), amount);
        Set<PaymentInstance> payments = paymentHistory.get(contract);
        if (payments == null) {
            payments = new TreeSet<>();
            paymentHistory.put(contract, payments);
        }
        payments.add(instance);

    }

    public void pay(MasterVehicleContract contract, int amount) {
        if (contract == null || amount <= 0) {
            throw new IllegalArgumentException("Zmluva nesmie byť null a amount nesmie byť nekladny.");
        }
        if (!contract.isActive() || !contract.getInsurer().equals(insurer) || contract.getChildContracts().isEmpty()) {
            throw new contracts.InvalidContractException("Neplatná zmluva.");
        }

        Set<SingleVehicleContract> childContracts = contract.getChildContracts();
        int usedAmount = 0;

        //vynulovanie nedoplatku
        for (SingleVehicleContract c : childContracts) {
            if (!c.isActive()) continue;
            int debt = c.getContractPaymentData().getOutstandingBalance();
            if (debt > 0) {
                if (amount >= debt) {
                    amount -= debt;
                    usedAmount += debt;
                    c.getContractPaymentData().setOutstandingBalance(0);
                } else {
                    c.getContractPaymentData().setOutstandingBalance(debt - amount);
                    usedAmount += amount;
                    amount = 0;
                    break;
                }
            }
        }

        //vytvorenie preplatku
        while (amount > 0) {
            boolean somethingPaid = false;
            for (SingleVehicleContract c : childContracts) {
                if (!c.isActive()) continue;
                int premium = c.getContractPaymentData().getPremium();
                if (amount >= premium) {
                    c.getContractPaymentData().setOutstandingBalance(
                            c.getContractPaymentData().getOutstandingBalance() - premium
                    );
                    amount -= premium;
                    usedAmount += premium;
                    somethingPaid = true;
                } else {
                    c.getContractPaymentData().setOutstandingBalance(
                            c.getContractPaymentData().getOutstandingBalance() - amount
                    );
                    usedAmount += amount;
                    amount = 0;
                    somethingPaid = true;
                    break;
                }
            }
            if (!somethingPaid) break;
        }

        // len ak sa niečo reálne zaplatilo
        if (usedAmount > 0) {
            PaymentInstance instance = new PaymentInstance(insurer.getCurrentTime(), usedAmount);
            paymentHistory.computeIfAbsent(contract, k -> new TreeSet<>()).add(instance);
        }
    }
}
