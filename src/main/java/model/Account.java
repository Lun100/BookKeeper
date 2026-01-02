package model;
import util.TransactionType;
import util.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * 实体: 账户 (Account) [cite: 45]
 * 存储资金账户信息,如余额。负责在交易或转账发生时更新余额 [cite: 86]
 */
public class Account {
    private String accountId;
    private String name;
    private BigDecimal balance;

    public Account(String name, BigDecimal initialBalance) {
        this.accountId = "ACC_" + UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.balance = initialBalance.setScale(2, RoundingMode.HALF_UP);
    }

    // Getters and Setters...
    public String getAccountId() { return accountId; }
    public String getName() { return name; }
    public BigDecimal getBalance() { return balance; }
    public void setName(String name) { this.name = name; }

    /**
     * 对应UML中的 updateBalance() [cite: 47]
     * 序列图中显示此方法需要 (金额, 类型) [cite: 168]
     */
    public void updateBalance(BigDecimal amount, TransactionType type) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("更新余额的金额必须为正数");
        }
        
        BigDecimal formattedAmount = amount.setScale(2, RoundingMode.HALF_UP);

        if (type == TransactionType.INCOME) {
            this.balance = this.balance.add(formattedAmount);
        } else if (type == TransactionType.EXPENSE) {
            BigDecimal newBalance = this.balance.subtract(formattedAmount);
            // 余额可以为负 (例如信用卡), 此处仅打印警告
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("警告: 账户 " + name + " 余额已为负: " + newBalance);
            }
            this.balance = newBalance;
        }
    }

    @Override
    public String toString() {
        return "Account[id='" + accountId + "', name='" + name + "', balance=" + balance + ']';
    }
}