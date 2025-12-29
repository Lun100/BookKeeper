package service;

import model.Budget;
import model.Transaction;
import repository.IDataRepository;
import util.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务: 报告服务 (ReportingService) 
 * 负责协调数据的查询和统计,提供月度总览、分类占比、趋势等视图 [cite: 92]
 * 对应UML用例: 查看统计报表 [cite: 145]
 */
public class ReportingService {
    
    private final IDataRepository<Transaction, String> transactionRepo;
    private final IDataRepository<Budget, String> budgetRepo;
    private final PrecisionEngine precisionEngine;

    public ReportingService(IDataRepository<Transaction, String> transactionRepo,
                            IDataRepository<Budget, String> budgetRepo,
                            PrecisionEngine precisionEngine) {
        this.transactionRepo = transactionRepo;
        this.budgetRepo = budgetRepo;
        this.precisionEngine = precisionEngine;
    }

    /**
     * 对应UML中的 getMonthlyOverview() [cite: 54]
     * 对应UML用例: 查看月度总览 [cite: 148]
     */
    public Map<String, BigDecimal> getMonthlyOverview(YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);

        // 依赖 IDataRepository::query [cite: 59]
        List<Transaction> monthTransactions = transactionRepo.query(
            tx -> !tx.getDateTime().isBefore(start) && !tx.getDateTime().isAfter(end)
        );

        // 依赖 PrecisionEngine::calculateSum [cite: 52]
        BigDecimal totalIncome = precisionEngine.calculateSum(
            monthTransactions.stream()
                .filter(tx -> tx.getType() == TransactionType.INCOME)
                .collect(Collectors.toList())
        );

        BigDecimal totalExpense = precisionEngine.calculateSum(
            monthTransactions.stream()
                .filter(tx -> tx.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toList())
        );
        
        BigDecimal netIncome = totalIncome.subtract(totalExpense);

        return Map.of(
            "totalIncome", totalIncome,
            "totalExpense", totalExpense,
            "netIncome", netIncome
        );
    }
    
    /**
     * 对应UML中的 getCategoryBreakdown() [cite: 56]
     * 对应UML用例: 查看分类占比与趋势 [cite: 144]
     */
    public Map<String, BigDecimal> getCategoryBreakdown(YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> monthExpenses = transactionRepo.query(
            tx -> tx.getType() == TransactionType.EXPENSE &&
                  !tx.getDateTime().isBefore(start) && 
                  !tx.getDateTime().isAfter(end)
        );
        
        Map<String, BigDecimal> breakdown = monthExpenses.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCategoryId,
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
            ));
            
        breakdown.forEach((key, value) -> breakdown.put(key, precisionEngine.format(value)));
        return breakdown;
    }
    
    /**
     * 对应UML中的 getTrendAnalysis() (SHOULD) [cite: 58]
     */
    public Map<YearMonth, BigDecimal> getTrendAnalysis(TransactionType type, int months) {
        System.out.println("趋势分析 ( " + type + ", " + months + "个月 ) 正在执行...");
        return Map.of();
    }
    
    /**
     * 对应UML中的 checkBudgetAlerts() (SHOULD) [cite: 61]
     * 对应UML用例: 设置预算与提醒 [cite: 151]
     */
    public List<Budget> checkBudgetAlerts(YearMonth month) {
        List<Budget> allBudgets = budgetRepo.findAll();
        Map<String, BigDecimal> categorySpending = getCategoryBreakdown(month);
        
        List<Budget> overspentBudgets = allBudgets.stream()
            .filter(budget -> {
                String categoryId = budget.getCategoryId();
                if (categoryId == null) return false; // 跳过总预算
                
                BigDecimal spent = categorySpending.getOrDefault(categoryId, BigDecimal.ZERO);
                return budget.checkOverspend(spent); // [cite: 19]
            })
            .collect(Collectors.toList());
            
        return overspentBudgets;
    }
}