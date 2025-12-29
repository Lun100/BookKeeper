package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * 实体: 预算 (Budget) [cite: 11]
 */
public class Budget {
    private String budgetId;
    private BigDecimal monthlyLimit;
    private String categoryId; // 针对可选的Category (通过ID关联) [cite: 16]
    
    public Budget(BigDecimal monthlyLimit, String categoryId) {
        this.budgetId = "BUD_" + UUID.randomUUID().toString().substring(0, 8);
        this.monthlyLimit = monthlyLimit.setScale(2, RoundingMode.HALF_UP);
        this.categoryId = categoryId; // categoryId为null表示总预算
    }
    
    // Getters...
    public String getBudgetId() { return budgetId; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public String getCategoryId() { return categoryId; }

    /**
     * 对应UML中的 checkOverspend() [cite: 19]
     */
    public boolean checkOverspend(BigDecimal currentSpending) {
        return currentSpending.compareTo(this.monthlyLimit) > 0;
    }

    @Override
    public String toString() {
        return "Budget[id='" + budgetId + 
               "', categoryId='" + (categoryId != null ? categoryId : "Total") + 
               "', limit=" + monthlyLimit + ']';
    }
}