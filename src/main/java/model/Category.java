package model;

import util.TransactionType;
import java.util.UUID;

/**
 * 实体: 类别 (Category) [cite: 13]
 * 用于对收支记录进行分类管理 [cite: 83]
 */
public class Category {
    private String categoryId;
    private String name; // (内置/自定义) [cite: 20]
    private TransactionType type; // (收入/支出) [cite: 21]

    public Category(String name, TransactionType type) {
        this.categoryId = "CAT_" + UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.type = type;
    }

    // Getters and Setters...
    public String getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public TransactionType getType() { return type; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "Category[id='" + categoryId + "', name='" + name + "', type=" + type + ']';
    }
}