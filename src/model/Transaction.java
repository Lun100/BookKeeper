package model;
import util.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 核心实体: 交易 (Transaction) [cite: 23]
 * 系统的核心数据类,包含金额、日期、类别等收支记录的关键信息 [cite: 81]
 */
public class Transaction {
    private String transactionId;
    private BigDecimal amount; // "必填 >0, 两位小数" [cite: 25]
    private TransactionType type;
    private LocalDateTime dateTime; // "必填" [cite: 28]
    private String memo;
    private List<String> tags; // "可选" [cite: 30]
    private String status;

    // 关联关系 (通过ID)
    private String categoryId; // 归属 (必填) [cite: 22, 84]
    private String accountId; // 关联 (默认现金) [cite: 31]
    private List<String> attachmentIds; // 包含 [cite: 27]

    // 构造函数
    public Transaction(BigDecimal amount, TransactionType type, LocalDateTime dateTime, String categoryId, String accountId) {
        this.transactionId = "TX_" + UUID.randomUUID().toString().substring(0, 8);
        this.amount = amount;
        this.type = type;
        this.dateTime = dateTime;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.status = "COMPLETED";
    }

    // Getters and Setters...
    public String getTransactionId() { return transactionId; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategoryId() { return categoryId; }
    public String getAccountId() { return accountId; }
    public List<String> getAttachmentIds() { return attachmentIds; }
    public void setAttachmentIds(List<String> attachmentIds) { this.attachmentIds = attachmentIds; }

    @Override
    public String toString() {
        return "Transaction[" +
                "id='" + transactionId + '\'' +
                ", type=" + type +
                ", amount=" + amount +
                ", dateTime=" + dateTime +
                ", categoryId='" + categoryId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", memo='" + memo + '\'' +
                ']';
    }
}
