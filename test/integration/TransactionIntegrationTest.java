package integration;
import org.junit.jupiter.api.*;
import model.*;
import repository.*;
import service.*;
import util.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("集成测试：交易录入全链路")
class TransactionIntegrationTest {
    private TransactionService transactionService;
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

        // 预置基础数据：账户和类别
        Account account = new Account("工资账户", new BigDecimal("1000.00")); //
        repository.getAccountRepository().save(account);

        Category category = new Category("餐饮", TransactionType.EXPENSE); //
        repository.getCategoryRepository().save(category);
    }

    @Test
    @DisplayName("录入支出交易应同步更新账户余额并持久化记录")
    void testRecordTransactionIntegration() {
        // 1. 获取预置的 ID
        String accId = repository.getAccountRepository().findAll().get(0).getAccountId();
        String catId = repository.getCategoryRepository().findAll().get(0).getCategoryId();

        // 2. 创建交易数据
        Transaction txData = new Transaction(
                new BigDecimal("250.50"),
                TransactionType.EXPENSE,
                LocalDateTime.now(),
                catId,
                accId
        );

        // 3. 执行录入
        Transaction savedTx = transactionService.recordTransaction(txData);

        // 4. 验证集成结果
        // 验证交易记录是否存入存储库
        assertNotNull(repository.getTransactionRepository().findById(savedTx.getTransactionId()));

        // 验证账户余额是否从 1000.00 扣除 250.50 变为 749.50
        Account updatedAccount = repository.getAccountRepository().findById(accId).get();
        assertEquals(new BigDecimal("749.50"), updatedAccount.getBalance());
    }
}
