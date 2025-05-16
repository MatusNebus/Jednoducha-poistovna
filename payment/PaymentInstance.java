package payment;

import java.time.LocalDateTime;

public class PaymentInstance implements Comparable<PaymentInstance> {
    private final LocalDateTime paymentTime;
    private final int paymentAmount;

    //Konštruktor:
    public PaymentInstance(LocalDateTime paymentTime, int paymentAmount) {
        if (paymentTime == null) {
            throw new IllegalArgumentException("Čas platby paymentTime nesmie byť null.");
        }
        if (paymentAmount <= 0) {
            throw new IllegalArgumentException("Výška platby musí byť kladná.");
        }
        this.paymentTime = paymentTime;
        this.paymentAmount = paymentAmount;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public int getPaymentAmount() {
        return paymentAmount;
    }

    @Override
    public int compareTo(PaymentInstance other) {
        return this.paymentTime.compareTo(other.paymentTime);
    }
}
