package com.sofiapi.taxes_api.service;

import com.sofiapi.taxes_api.domain.Balance;
import com.sofiapi.taxes_api.domain.TaxPlan;
import com.sofiapi.taxes_api.domain.TaxResult;
import com.sofiapi.taxes_api.domain.TaxSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private TaxesService taxesService;

    private FinanceService financeService;

    @BeforeEach
    void setUp() {
        // Income и Charge настоящие: в них нет логики, которую стоит подменять
        financeService = new FinanceService(taxesService);
    }

    // ---------- addIncome / addCharge ----------

    @Test
    @DisplayName("Добавление дохода возвращает обновлённый баланс")
    void shouldReturnBalanceAfterAddingIncome() {
        Balance balance = financeService.addIncome(100_000);

        assertEquals(100_000, balance.income());
        assertEquals(0, balance.charge());
        assertEquals(100_000, balance.profit());
    }

    @Test
    @DisplayName("Доходы накапливаются, а не перезаписываются")
    void shouldAccumulateIncomes() {
        financeService.addIncome(100_000);
        Balance balance = financeService.addIncome(50_000);

        assertEquals(150_000, balance.income());
    }

    @Test
    @DisplayName("Расходы накапливаются и уменьшают прибыль")
    void shouldAccumulateCharges() {
        financeService.addIncome(100_000);
        financeService.addCharge(40_000);
        Balance balance = financeService.addCharge(10_000);

        assertEquals(50_000, balance.charge());
        assertEquals(50_000, balance.profit());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -50_000})
    @DisplayName("Нулевая и отрицательная сумма отклоняются")
    void shouldRejectNonPositiveAmount(int amount) {
        assertThrows(IllegalArgumentException.class, () -> financeService.addIncome(amount));
        assertThrows(IllegalArgumentException.class, () -> financeService.addCharge(amount));

        assertEquals(0, financeService.currentBalance().income());
        verifyNoInteractions(taxesService);
    }

    @Test
    @DisplayName("Неверная сумма не должна «съедать» уже накопленное")
    void shouldNotChangeStateOnInvalidInput() {
        financeService.addIncome(100_000);

        assertThrows(IllegalArgumentException.class, () -> financeService.addCharge(-5));

        assertEquals(100_000, financeService.currentBalance().income());
    }

    // ---------- calculate ----------

    @Test
    @DisplayName("УСН 6% делегируется в TaxesService с текущим доходом")
    void shouldDelegateSixPercentCalculation() {
        financeService.addIncome(100_000);
        when(taxesService.taxEarnings(100_000)).thenReturn(6000);

        TaxResult result = financeService.calculate(TaxSystem.EARNINGS);

        assertEquals(TaxSystem.EARNINGS, result.system());
        assertEquals(6000, result.amount());
        verify(taxesService).taxEarnings(100_000);
    }

    @Test
    @DisplayName("УСН 15% передаёт в TaxesService и доход, и расход")
    void shouldDelegateFifteenPercentCalculation() {
        financeService.addIncome(100_000);
        financeService.addCharge(40_000);
        when(taxesService.taxEarningsMinusSpendings(100_000, 40_000)).thenReturn(9000);

        TaxResult result = financeService.calculate(TaxSystem.EARNINGS_MINUS_SPENDINGS);

        assertEquals(TaxSystem.EARNINGS_MINUS_SPENDINGS, result.system());
        assertEquals(9000, result.amount());
        verify(taxesService).taxEarningsMinusSpendings(100_000, 40_000);
    }

    @Test
    @DisplayName("При расчёте по одной системе вторая не вызывается")
    void shouldNotCallAnotherSystem() {
        financeService.addIncome(100_000);
        when(taxesService.taxEarnings(anyInt())).thenReturn(6000);

        financeService.calculate(TaxSystem.EARNINGS);

        verify(taxesService, never()).taxEarningsMinusSpendings(anyInt(), anyInt());
    }

    // ---------- compare ----------

    @Test
    @DisplayName("Сравнение считает обе системы и предлагает меньшую")
    void shouldCompareBothSystems() {
        financeService.addIncome(100_000);
        financeService.addCharge(40_000);
        when(taxesService.taxEarningsMinusSpendings(100_000, 40_000)).thenReturn(9000);
        when(taxesService.taxEarnings(100_000)).thenReturn(6000);

        TaxPlan plan = financeService.compare();

        assertEquals(9000, plan.earningsMinusSpendings().amount());
        assertEquals(6000, plan.earnings().amount());
        assertEquals(TaxSystem.EARNINGS, plan.recommended().system());
    }

    @Test
    @DisplayName("Когда разница велика, выгоднее система 15%")
    void shouldRecommendFifteenPercentWhenChargesAreBig() {
        financeService.addIncome(100_000);
        financeService.addCharge(95_000);
        when(taxesService.taxEarningsMinusSpendings(100_000, 95_000)).thenReturn(750);
        when(taxesService.taxEarnings(100_000)).thenReturn(6000);

        assertEquals(TaxSystem.EARNINGS_MINUS_SPENDINGS, financeService.compare().recommended().system());
    }

    // ---------- reset ----------

    @Test
    @DisplayName("Сброс обнуляет накопленное")
    void shouldResetState() {
        financeService.addIncome(100_000);
        financeService.addCharge(40_000);

        financeService.reset();

        assertEquals(0, financeService.currentBalance().income());
        assertEquals(0, financeService.currentBalance().charge());
    }

    @Test
    @DisplayName("После сброса расчёт идёт от нуля")
    void shouldCalculateFromZeroAfterReset() {
        financeService.addIncome(100_000);
        financeService.reset();
        when(taxesService.taxEarnings(0)).thenReturn(0);

        financeService.calculate(TaxSystem.EARNINGS);

        verify(taxesService).taxEarnings(0);
    }
}
