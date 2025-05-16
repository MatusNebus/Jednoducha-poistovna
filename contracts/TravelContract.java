package contracts;

import company.InsuranceCompany;
import objects.Person;
import payment.ContractPaymentData;
import objects.LegalForm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TravelContract extends AbstractContract {
    private final Set<Person> insuredPersons;

    //Konštruktor:
    public TravelContract(String contractNumber, InsuranceCompany insurer, Person policyHolder,
                ContractPaymentData contractPaymentData, int coverageAmount, Set<Person> personsToInsure) {
            super(contractNumber, insurer, policyHolder, contractPaymentData, coverageAmount);

        if (contractPaymentData == null) {
            throw new IllegalArgumentException("contractPaymentData nesmie byť null.");
        }   

        if (personsToInsure == null || personsToInsure.isEmpty()) {
            throw new IllegalArgumentException("Množina nesmie byt null a musi byt neprazdna.");
        }

        for (Person p : personsToInsure) { //musia to byt fyzicke osoby vsetko
            if (p.getLegalForm() != LegalForm.NATURAL) {
                throw new IllegalArgumentException("Všetky poistené osoby musia byť fyzické osoby.");
            }
        }

        // z personsToInsure vytvorím kópiu s nazvom insuredPersons,
        // aby nikto zvonku nemohol meniť insuredPersons
        this.insuredPersons = Collections.unmodifiableSet(new HashSet<>(personsToInsure));
    }

    public Set<Person> getInsuredPersons() {
        return insuredPersons;
    }
}
