package com.sofiapi.taxes_api.domain;

public enum TaxSystem {
    EARNINGS_MINUS_SPENDINGS("Доходы минус расходы (15%)"),
    EARNINGS("Только доходы (6%)");

    private final String label;

    TaxSystem(String label) { this.label = label; }

    public String label() { return label; }
}
