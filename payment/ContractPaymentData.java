package payment;

import java.time.LocalDateTime;

public class ContractPaymentData {
    private int premium;
    private PremiumPaymentFrequency premiumPaymentFrequency;
    private LocalDateTime nextPaymentTime;
    private int outstandingBalance;

    //Konštruktor:
    public ContractPaymentData(int premium, PremiumPaymentFrequency premiumPaymentFrequency,
                               LocalDateTime nextPaymentTime, int outstandingBalance) {
        if (premium <= 0) {
            throw new IllegalArgumentException("premium musí byť kladne.");
        }
        if (premiumPaymentFrequency == null) {
            throw new IllegalArgumentException("Frekvencia platby premiumPaymentFrequency nesmie byť null.");
        }
        if (nextPaymentTime == null) {
            throw new IllegalArgumentException("Čas nasledujúcej platby nextPaymentTime nesmie byť null.");
        }

        this.premium = premium;
        this.premiumPaymentFrequency = premiumPaymentFrequency;
        this.nextPaymentTime = nextPaymentTime;
        this.outstandingBalance = outstandingBalance;
    }

    public int getPremium() {
        return premium;
    }

    public void setPremium(int premium) {
        if (premium <= 0) {
            throw new IllegalArgumentException("premium musí byť kladne.");
        }
        this.premium = premium;
    }

    public void setOutstandingBalance(int outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public int getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setPremiumPaymentFrequency(PremiumPaymentFrequency premiumPaymentFrequency) {
        if (premiumPaymentFrequency == null) {
            throw new IllegalArgumentException("Frekvencia platby premiumPaymentFrequency nesmie byť null.");
        }
        this.premiumPaymentFrequency = premiumPaymentFrequency;
    }

    public PremiumPaymentFrequency getPremiumPaymentFrequency() {
        return premiumPaymentFrequency;
    }

    public LocalDateTime getNextPaymentTime() {
        return nextPaymentTime;
    }

    public void updateNextPaymentTime() {
        int mesiace = premiumPaymentFrequency.getValueInMonths();
        this.nextPaymentTime = this.nextPaymentTime.plusMonths(mesiace);
    }
}
