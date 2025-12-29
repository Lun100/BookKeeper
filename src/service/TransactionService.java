package service;

import model.*;
import repository.IDataRepository;
import util.DataNotFoundException;
import util.InsufficientFundsException;
import util.TransactionType;
import util.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

/**
 * 服务: 交易服务 (TransactionService) 
 * 负责协调收支记录的录入、修改...以及账户间的转账操作 [cite: 90]
 */
public class TransactionService {

    // 依赖项
    private final IDataRepository<Transaction, String> transactionRepo;
    private final IDataRepository<Account, String> accountRepo;
    private final IDataRepository<Category, String> categoryRepo;
    private final PrecisionEngine precisionEngine;

    // 构造函数注入依赖
    public TransactionService(IDataRepository<Transaction, String> transactionRepo,
                              IDataRepository<Account, String> accountRepo,
                              IDataRepository<Category, String> categoryRepo,
                              PrecisionEngine precisionEngine) {
        this.transactionRepo = transactionRepo;
        this.accountRepo = accountRepo;
        this.categoryRepo = categoryRepo;
        this.precisionEngine = precisionEngine;
    }

    /**
     * 核心功能: 记录一笔收支 [cite: 136]
     * 完整实现了序列图中的流程 [cite: 179]
     * 对应UML中的 recordTransaction(data) [cite: 37]
     */
    public Transaction recordTransaction(Transaction data) 
            throws ValidationException, DataNotFoundException {
        
        // 1. (序列图) validateAmount(金额) [cite: 165, 182]
        precisionEngine.validateAmount(data.getAmount());
        
        // 2. (序列图) 校验必填字段 (类别/日期) [cite: 167, 183]
        if (data.getCategoryId() == null || data.getCategoryId().isBlank()) {
            throw new ValidationException("类别 (categoryId) 是必填项");
        }
        if (data.getDateTime() == null) {
            throw new ValidationException("日期 (dateTime) 是必填项");
        }
        if (data.getAccountId() == null || data.getAccountId().isBlank()) {
            throw new ValidationException("账户 (accountId) 是必填项");
        }

        // 2.1 校验关联实体是否存在
        Account account = accountRepo.findById(data.getAccountId())
                .orElseThrow(() -> new DataNotFoundException("账户不存在: " + data.getAccountId()));
        
        categoryRepo.findById(data.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException("类别不存在: " + data.getCategoryId()));

        // 3. (序列图) updateBalance(金额, 类型) [cite: 168, 184]
        account.updateBalance(data.getAmount(), data.getType());

        // 4. (序列图) 持久化更新后的账户
        accountRepo.save(account);

        // 5. (序列图) save(Transaction实体) [cite: 170, 187]
        Transaction txToSave = new Transaction(
            precisionEngine.format(data.getAmount()), 
            data.getType(), data.getDateTime(),
            data.getCategoryId(), data.getAccountId()
        );
        txToSave.setMemo(data.getMemo());
        txToSave.setTags(data.getTags());
        
        // 6. (序列图) 存储成功
        return transactionRepo.save(txToSave);
    }
    
    /**
     * 对应UML中的 findTransactions(filters) (组合筛选) [cite: 38]
     * 对应UML用例: 查找与筛选收支记录 [cite: 140]
     */
    public List<Transaction> findTransactions(String categoryId, LocalDateTime start, LocalDateTime end) {
        Predicate<Transaction> predicate = tx -> true;

        if (categoryId != null && !categoryId.isBlank()) {
            predicate = predicate.and(tx -> tx.getCategoryId().equals(categoryId));
        }
        if (start != null) {
            predicate = predicate.and(tx -> !tx.getDateTime().isBefore(start));
        }
        if (end != null) {
            predicate = predicate.and(tx -> !tx.getDateTime().isAfter(end));
        }
        
        return transactionRepo.query(predicate); // [cite: 41]
    }

    /**
     * 对应UML中的 transferFunds(from, to, amount) (SHOULD) [cite: 39]
     * 对应UML用例: 转账 [cite: 139]
     */
    public void transferFunds(String fromAccountId, String toAccountId, BigDecimal amount)
            throws ValidationException, DataNotFoundException, InsufficientFundsException {
        
        precisionEngine.validateAmount(amount);
        
        Account fromAccount = accountRepo.findById(fromAccountId)
                .orElseThrow(() -> new DataNotFoundException("转出账户不存在: " + fromAccountId));
        
        Account toAccount = accountRepo.findById(toAccountId)
                .orElseThrow(() -> new DataNotFoundException("转入账户不存在: " + toAccountId));

        // 执行转账
        fromAccount.updateBalance(amount, TransactionType.EXPENSE);
        toAccount.updateBalance(amount, TransactionType.INCOME);

        // 持久化
        accountRepo.save(fromAccount);
        accountRepo.save(toAccount);
    }
}