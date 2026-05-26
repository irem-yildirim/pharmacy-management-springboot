package com.pharmacy.controller.ui;

import com.pharmacy.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UIController {

    private final FinanceService financeService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/index";
    }

    @GetMapping("/pos")
    public String pos() {
        return "pos/index";
    }

    @GetMapping("/inventory")
    public String inventory() {
        return "inventory/list";
    }

    @GetMapping("/purchase")
    public String purchase() {
        return "purchase/index";
    }

    @GetMapping("/customer")
    public String customer() {
        return "customer/index";
    }

    @GetMapping("/customers/list")
    public String customerList() {
        return "customers/list";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings/index";
    }

    @GetMapping("/account")
    public String account() {
        return "account/index";
    }

    @GetMapping("/login")
    public String login() {
        return "login/index";
    }

    @GetMapping("/finance")
    public String finance(Model model) {
        BigDecimal totalRevenue = financeService.calculateTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = financeService.calculateTotalCost();
        if (totalCost == null) totalCost = BigDecimal.ZERO;
        BigDecimal totalLoss = financeService.calculateExpiredLoss();
        if (totalLoss == null) totalLoss = BigDecimal.ZERO;
        BigDecimal totalProfit = totalRevenue.subtract(totalCost).subtract(totalLoss);

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("totalLoss", totalLoss);
        model.addAttribute("totalProfit", totalProfit);

        List<Map<String, Object>> ledger = financeService.getTransactionLedger();
        model.addAttribute("ledger", ledger);

        return "finance/index";
    }
}
