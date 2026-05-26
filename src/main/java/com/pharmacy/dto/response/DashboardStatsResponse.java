package com.pharmacy.dto.response;

import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    private BigDecimal dailyRevenue;
    private BigDecimal dailyProfit;
    private BigDecimal dailyLoss;
}
