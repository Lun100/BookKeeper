package integration;
import org.junit.jupiter.api.*;
import model.*;
import repository.InMemoryDataRepository;
import service.*;
import util.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("集成测试：交易-报表统计流转")
class ReportingIntegrationTest {
    private TransactionService transactionService;
    private ReportingService reportingService;
    private InMemoryDataRepository repository;

    @BeforeEach
    void init() {
        repository = new InMemoryDataRepository();
        PrecisionEngine precisionEngine = new PrecisionEngine();

        transactionService = new TransactionService(
                repository.getTransactionRepository(),
                repository.getAccountRepository(),
                repository.getCategoryRepository(),
                precisionEngine
        );

        reportingService = new ReportingService(
                repository.getTransactionRepository(),
                repository.getBudgetRepository(),
                precisionEngine
        );

        // 初始化环境
        Account acc = new Account("通用账户", new BigDecimal("5000.00"));
        repository.getAccountRepository().save(acc);
        Category c1 = new Category("工资", TransactionType.INCOME);
        Category c2 = new Category("购物", TransactionType.EXPENSE);
        repository.getCategoryRepository().save(c1);
        repository.getCategoryRepository().save(c2);
    }

    @Test
    @DisplayName("录入多笔收支后，月度概览应准确反映净收益")
    void testTransactionToReportingIntegration() {
        String accId = repository.getAccountRepository().findAll().get(0).getAccountId();
        String catIncId = repository.getCategoryRepository().findAll().stream()
                .filter(c -> c.getType() == TransactionType.INCOME).findFirst().get().getCategoryId();
        String catExpId = repository.getCategoryRepository().findAll().stream()
                .filter(c -> c.getType() == TransactionType.EXPENSE).findFirst().get().getCategoryId();

        // 1. 模拟本月多次交易记录录入
        transactionService.recordTransaction(new Transaction(new BigDecimal("2000.00"), TransactionType.INCOME, LocalDateTime.now(), catIncId, accId));
        transactionService.recordTransaction(new Transaction(new BigDecimal("500.00"), TransactionType.EXPENSE, LocalDateTime.now(), catExpId, accId));
        transactionService.recordTransaction(new Transaction(new BigDecimal("300.00"), TransactionType.EXPENSE, LocalDateTime.now(), catExpId, accId));

        // 2. 调用报表服务获取当月总览
        Map<String, BigDecimal> overview = reportingService.getMonthlyOverview(YearMonth.now());

        // 3. 验证统计结果
        assertEquals(new BigDecimal("2000.00"), overview.get("totalIncome"), "总收入统计错误");
        assertEquals(new BigDecimal("800.00"), overview.get("totalExpense"), "总支出统计错误");
        assertEquals(new BigDecimal("1200.00"), overview.get("netIncome"), "净收入计算错误");
    }
}