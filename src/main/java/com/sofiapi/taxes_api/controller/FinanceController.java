package com.sofiapi.taxes_api.controller;

import com.sofiapi.taxes_api.domain.TaxSystem;
import com.sofiapi.taxes_api.dto.*;
import com.sofiapi.taxes_api.service.FinanceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/incomes")
    @ResponseStatus(HttpStatus.CREATED)
    public BalanceResponse addIncome(@RequestBody MoneyRequest request) {
        return BalanceResponse.from(financeService.addIncome(request.amount()));
    }

    @PostMapping("/charges")
    @ResponseStatus(HttpStatus.CREATED)
    public BalanceResponse addCharge(@RequestBody MoneyRequest request) {
        return BalanceResponse.from(financeService.addCharge(request.amount()));
    }

    @GetMapping("/balance")
    public BalanceResponse balance() {
        return BalanceResponse.from(financeService.currentBalance());
    }

    @DeleteMapping("/balance")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset() {
        financeService.reset();
    }

    @GetMapping("/taxes")
    public TaxResultResponse getTax(@RequestParam TaxSystem system) {
        var result = financeService.calculate(system);
        return TaxResultResponse.from(result); // превращаем доменный объект в DTO
    }

    @GetMapping("/taxes/comparison")
    public ComparisonResponse comparison() {
        return ComparisonResponse.from(financeService.compare());
    }
}
