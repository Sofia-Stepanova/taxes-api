package com.sofiapi.taxes_api.service;

import org.springframework.stereotype.Service;

@Service
public class TaxesService {

  //  private Integer tax;

    public int taxEarningsMinusSpendings(int income, int charge) {
        int base = income - charge;
        return base <= 0 ? 0 : base * 15 / 100;
    }

    public int taxEarnings(int income) {
        return income * 6 / 100;
     //   return tax;
    }
}