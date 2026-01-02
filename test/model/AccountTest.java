package model;
import org.junit.jupiter.api.Test;
import model.Account;
import util.TransactionType;
import util.ValidationException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    // 1. 收入测试：余额增加
    @Test
    void testUpdateBalance_Income_IncreasesBalance() {
        Account account = new Account("现金", new BigDecimal("100.00"));
        account.updateBalance(new BigDecimal("50.50"), TransactionType.INCOME);
        assertEquals(new BigDecimal("150.50"), account.getBalance());
    }

    // 2. 支出测试：余额减少
    @Test
    void testUpdateBalance_Expense_DecreasesBalance() {
        Account account = new Account("银行", new BigDecimal("100.00"));
        account.updateBalance(new BigDecimal("40.00"), TransactionType.EXPENSE);
        assertEquals(new BigDecimal("60.00"), account.getBalance());
    }

    // 3. 边界测试：支出后余额正好为 0
    @Test
    void testUpdateBalance_Expense_ToZero() {
        Account account = new Account("钱包", new BigDecimal("50.00"));
        account.updateBalance(new BigDecimal("50.00"), TransactionType.EXPENSE);
        assertEquals(new BigDecimal("0.00"), account.getBalance());
    }

    // 4. 边界测试：支出后余额为负数（系统应允许，但内部打印警告）
    @Test
    void testUpdateBalance_Expense_NegativeBalance() {
        Account account = new Account("信用卡", new BigDecimal("10.00"));
        account.updateBalance(new BigDecimal("20.00"), TransactionType.EXPENSE);
        assertEquals(new BigDecimal("-10.00"), account.getBalance());
    }

    // 5. 异常路径：更新金额为 0
    @Test
    void testUpdateBalance_ZeroAmount_ThrowsException() {
        Account account = new Account("测试", new BigDecimal("100.00"));
        assertThrows(ValidationException.class, () -> account.updateBalance(BigDecimal.ZERO, TransactionType.INCOME));
    }

    // 6. 异常路径：更新金额为负数
    @Test
    void testUpdateBalance_NegativeAmount_ThrowsException() {
        Account account = new Account("测试", new BigDecimal("100.00"));
        assertThrows(ValidationException.class, () -> account.updateBalance(new BigDecimal("-5.00"), TransactionType.INCOME));
    }

    // 7. 精度处理：输入金额超过两位小数应被四舍五入
    @Test
    void testUpdateBalance_Rounding() {
        Account account = new Account("测试", new BigDecimal("100.00"));
        // 10.555 -> 10.56
        account.updateBalance(new BigDecimal("10.555"), TransactionType.INCOME);
        assertEquals(new BigDecimal("110.56"), account.getBalance());
    }

    // 8. 构造函数边界：初始余额精度处理
    @Test
    void testConstructor_InitialBalanceRounding() {
        Account account = new Account("测试", new BigDecimal("100.123"));
        assertEquals(new BigDecimal("100.12"), account.getBalance());
    }

    // 9. 连续操作测试：收入后支出
    @Test
    void testUpdateBalance_Sequence() {
        Account account = new Account("流水", new BigDecimal("0.00"));
        account.updateBalance(new BigDecimal("100.00"), TransactionType.INCOME);
        account.updateBalance(new BigDecimal("30.00"), TransactionType.EXPENSE);
        assertEquals(new BigDecimal("70.00"), account.getBalance());
    }

    // 10. 大额计算测试
    @Test
    void testUpdateBalance_LargeAmount() {
        Account account = new Account("大额", new BigDecimal("1000000.00"));
        account.updateBalance(new BigDecimal("999999.99"), TransactionType.INCOME);
        assertEquals(new BigDecimal("1999999.99"), account.getBalance());
    }
}