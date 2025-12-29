package service;

import model.Transaction;
import util.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 服务: 精度引擎 (PrecisionEngine) [cite: 48]
 * 核心计算引擎,确保金额的计算和汇总精度,满足“必须准确到分”的非功能需求 [cite: 94]
 */
public class PrecisionEngine {
    
    private static final int SCALE = 2; // 两位小数 [cite: 25]
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public BigDecimal format(BigDecimal amount) {
        return amount.setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * 对应序列图中的 [validateAmount(金额)] [cite: 165]
     * 规则: 必填 > 0, 两位小数 [cite: 25, 166, 182]
     */
    public void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ValidationException("金额不能为空");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) { // 检查 > 0
            throw new ValidationException("金额必须大于 0 (传入: " + amount + ")");
        }
        if (amount.scale() > SCALE) { // 检查精度
            throw new ValidationException("金额精度不能超过 " + SCALE + " 位小数 (传入: " + amount + ")");
        }
    }

    /**
     * 对应UML中的 calculateSum(transactions) (计算与汇总必须准确到分) [cite: 49]
     */
    public BigDecimal calculateSum(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return format(BigDecimal.ZERO);
        }
        
        BigDecimal sum = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        return format(sum); // 返回格式化后的总和
    }
}