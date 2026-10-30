package com.sofiapi.taxes_api.service;

import com.sofiapi.taxes_api.domain.*;
import org.springframework.stereotype.Service;

@Service
public class FinanceService {

    private final Income income = new Income();
    private final Charge charge = new Charge();
    private final TaxesService taxes;

    public FinanceService(TaxesService taxes) {
        this.taxes = taxes;
    }

    public synchronized Balance addIncome(int amount) {
        requirePositive(amount, "Сумма дохода");
        income.sumIncome(amount);
        return currentBalance();
    }

    public synchronized Balance addCharge(int amount) {
        requirePositive(amount, "Сумма расхода");
        charge.sumCharge(amount);
        return currentBalance();
    }

    public synchronized Balance currentBalance() {
        return new Balance(income.getIncome(), charge.getCharge());
    }

    //обнуляем накопленное, приложение при этом живет
    public synchronized void reset() {
        income.setIncome(0);
        charge.setCharge(0);
    }

    public synchronized TaxResult calculate(TaxSystem system) {
        int incomeValue = income.getIncome();
        int chargeValue = charge.getCharge();

        return switch (system) {
            case EARNINGS_MINUS_SPENDINGS ->
                    new TaxResult(system, taxes.taxEarningsMinusSpendings(incomeValue, chargeValue));
            case EARNINGS -> new TaxResult(system, taxes.taxEarnings(incomeValue));
        };
    }

    public synchronized TaxPlan compare() {
        return new TaxPlan(calculate(TaxSystem.EARNINGS_MINUS_SPENDINGS), calculate(TaxSystem.EARNINGS));
    }

    private static void requirePositive(int amount, String subject) {
        if (amount <= 0) {
            throw new IllegalArgumentException(subject + " должен быть больше нуля, получено: " + amount);
        }
    }
}
