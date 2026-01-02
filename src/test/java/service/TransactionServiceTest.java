package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.*;
import repository.InMemoryDataRepository;
import service.PrecisionEngine;
import service.TransactionService;
import util.DataNotFoundException;
import util.TransactionType;
import util.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {
    private TransactionService service;
    private InMemoryDataRepository repository;
    private PrecisionEngine engine;

    private String validAccId = "ACC_001";
    private String validCatId = "CAT_001";

    @BeforeEach
    void setUp() {
        repository = new InMemoryDataRepository();
        engine = new PrecisionEngine();
        service = new TransactionService(
                repository.getTransactionRepository(),
                repository.getAccountRepository(),
                repository.getCategoryRepository(),
                engine
        );

        // 初始化基础数据
        repository.getAccountRepository().save(new Account("测试账户", new BigDecimal("100.00")));
        // 注意：构造函数生成的ID是随机的，为了测试方便，我们需要获取实际存入的ID
        Account acc = repository.getAccountRepository().findAll().get(0);
        validAccId = acc.getAccountId();

        Category cat = new Category("工资", TransactionType.INCOME);
        repository.getCategoryRepository().save(cat);
        validCatId = cat.getCategoryId();
    }

    // 1. 正常记录收入
    @Test
    void testRecordTransaction_Success_Income() {
        Transaction tx = new Transaction(new BigDecimal("50.00"), TransactionType.INCOME, LocalDateTime.now(), validCatId, validAccId);
        Transaction saved = service.recordTransaction(tx);

        assertNotNull(saved);
        assertEquals(new BigDecimal("150.00"), repository.getAccountRepository().findById(validAccId).get().getBalance());
    }

    // 2. 正常记录支出
    @Test
    void testRecordTransaction_Success_Expense() {
        Transaction tx = new Transaction(new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDateTime.now(), validCatId, validAccId);
        service.recordTransaction(tx);
        assertEquals(new BigDecimal("70.00"), repository.getAccountRepository().findById(validAccId).get().getBalance());
    }

    // 3. 边界测试：金额为 0 (触发 PrecisionEngine 校验)
    @Test
    void testRecordTransaction_ZeroAmount_ThrowsException() {
        Transaction tx = new Transaction(BigDecimal.ZERO, TransactionType.INCOME, LocalDateTime.now(), validCatId, validAccId);
        assertThrows(ValidationException.class, () -> service.recordTransaction(tx));
    }

    // 4. 边界测试：金额为负数
    @Test
    void testRecordTransaction_NegativeAmount_ThrowsException() {
        Transaction tx = new Transaction(new BigDecimal("-1.00"), TransactionType.INCOME, LocalDateTime.now(), validCatId, validAccId);
        assertThrows(ValidationException.class, () -> service.recordTransaction(tx));
    }

    // 5. 异常测试：账户不存在
    @Test
    void testRecordTransaction_AccountNotFound_ThrowsException() {
        Transaction tx = new Transaction(new BigDecimal("10.00"), TransactionType.INCOME, LocalDateTime.now(), validCatId, "NON_EXISTENT");
        assertThrows(DataNotFoundException.class, () -> service.recordTransaction(tx));
    }

    // 6. 异常测试：类别不存在
    @Test
    void testRecordTransaction_CategoryNotFound_ThrowsException() {
        Transaction tx = new Transaction(new BigDecimal("10.00"), TransactionType.INCOME, LocalDateTime.now(), "NON_EXISTENT", validAccId);
        assertThrows(DataNotFoundException.class, () -> service.recordTransaction(tx));
    }

    // 7. 异常测试：类别 ID 为空
    @Test
    void testRecordTransaction_EmptyCategoryId_ThrowsException() {
        Transaction tx = new Transaction(new BigDecimal("10.00"), TransactionType.INCOME, LocalDateTime.now(), " ", validAccId);
        assertThrows(ValidationException.class, () -> service.recordTransaction(tx));
    }

    // 8. 异常测试：日期为空
    @Test
    void testRecordTransaction_NullDate_ThrowsException() {
        // 使用特殊构造或模拟数据
        Transaction tx = new Transaction(new BigDecimal("10.00"), TransactionType.INCOME, null, validCatId, validAccId);
        assertThrows(ValidationException.class, () -> service.recordTransaction(tx));
    }

    // 9. 精度测试：金额超过两位小数
    @Test
    void testRecordTransaction_InvalidPrecision_ThrowsException() {
        Transaction tx = new Transaction(new BigDecimal("10.123"), TransactionType.INCOME, LocalDateTime.now(), validCatId, validAccId);
        assertThrows(ValidationException.class, () -> service.recordTransaction(tx));
    }

    // 10. 逻辑完整性测试：验证 memo 是否正确保存
    @Test
    void testRecordTransaction_MemoSaved() {
        Transaction tx = new Transaction(new BigDecimal("10.00"), TransactionType.INCOME, LocalDateTime.now(), validCatId, validAccId);
        tx.setMemo("Lunch money");
        Transaction saved = service.recordTransaction(tx);
        assertEquals("Lunch money", saved.getMemo());
    }
}