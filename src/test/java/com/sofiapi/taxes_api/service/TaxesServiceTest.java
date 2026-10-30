package com.sofiapi.taxes_api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaxesServiceTest {

    private TaxesService taxesService;

    @BeforeEach
    void setUp() {
        taxesService = new TaxesService();
    }

    @Test
    @DisplayName("УСН 6% считается только от дохода")
    void shouldCountSixPercentFromIncome() {
        assertEquals(6000, taxesService.taxEarnings(100_000));
    }

    @Test
    @DisplayName("УСН 6% с нулевого дохода — ноль")
    void shouldReturnZeroForZeroIncome() {
        assertEquals(0, taxesService.taxEarnings(0));
    }

    @Test
    @DisplayName("УСН 6% округляет в меньшую сторону (целочисленное деление)")
    void shouldTruncateFraction() {
        // 100 001 * 6 / 100 = 6000,06 → 6000
        assertEquals(6000, taxesService.taxEarnings(100_001));
    }

    @Test
    @DisplayName("УСН 15% считается с разницы")
    void shouldCountFifteenPercentFromDifference() {
        assertEquals(9000, taxesService.taxEarningsMinusSpendings(100_000, 40_000));
    }

    @Test
    @DisplayName("Если расходы больше доходов — налог ноль, а не минус")
    void shouldReturnZeroWhenChargeExceedsIncome() {
        assertEquals(0, taxesService.taxEarningsMinusSpendings(50_000, 70_000));
    }

    @Test
    @DisplayName("Доходы равны расходам — налог ноль")
    void shouldReturnZeroOnBreakEven() {
        assertEquals(0, taxesService.taxEarningsMinusSpendings(70_000, 70_000));
    }

    @ParameterizedTest
    @CsvSource({
            "100000, 40000, 9000",
            "200000,      0, 30000",
            " 70000,  70000,     0",
            " 50000,  70000,     0",
            "      0,      0,     0"
    })
    void shouldCountFifteenPercent(int income, int charge, int expected) {
        assertEquals(expected, taxesService.taxEarningsMinusSpendings(income, charge));
    }
}