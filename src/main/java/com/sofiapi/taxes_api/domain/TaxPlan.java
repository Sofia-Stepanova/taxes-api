package com.sofiapi.taxes_api.domain;

public record TaxPlan(TaxResult earningsMinusSpendings, TaxResult earnings) {
    public TaxResult recommended() {
        return earnings.amount() <= earningsMinusSpendings.amount() ? earnings : earningsMinusSpendings;
    }
}