package com.sofiapi.taxes_api.dto;

import com.sofiapi.taxes_api.domain.TaxPlan;
import com.sofiapi.taxes_api.domain.TaxResult;

public record ComparisonResponse(int taxEarningsMinusSpendings, int taxEarnings,
                                 String recommendedSystem, String message) {
    public static ComparisonResponse from(TaxPlan plan) {
        TaxResult best = plan.recommended();
        return new ComparisonResponse(
                plan.earningsMinusSpendings().amount(),
                plan.earnings().amount(),
                best.system().name(),
                "Выгоднее: " + best.system().label() + ", налог " + best.amount() + " руб.");
    }
}