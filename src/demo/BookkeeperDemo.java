package demo;

import model.*;
import repository.InMemoryDataRepository;
import service.PrecisionEngine;
import service.ReportingService;
import service.SystemService;
import service.TransactionService;
import util.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 主演示类 (Main Class)
 * 用于提供一个命令行交互界面，让用户可以记录收支、查询信息。
 */
public class BookkeeperDemo {

    // "依赖注入" 容器
    private final InMemoryDataRepository repository = new InMemoryDataRepository();
    private final PrecisionEngine precisionEngine = new PrecisionEngine();
    private final UserConfiguration userConfig = new UserConfiguration();
    
    private final TransactionService transactionService;
    private final ReportingService reportingService;
    private final SystemService systemService;

    // 预先设置的实体
    private Account accCash;
    private Account accBank;
    private Category catSalary;
    private Category catRent;
    private Category catFood;

    public BookkeeperDemo() {
        // 手动 "注入" 依赖
        this.transactionService = new TransactionService(
            repository.getTransactionRepository(),
            repository.getAccountRepository(),
            repository.getCategoryRepository(),
            precisionEngine
        );
        this.reportingService = new ReportingService(
            repository.getTransactionRepository(),
            repository.getBudgetRepository(),
            precisionEngine
        );
        this.systemService = new SystemService(
            repository.getCategoryRepository(),
            repository.getTransactionRepository(),
            userConfig
        );
    }

    public static void main(String[] args) {
        BookkeeperDemo demo = new BookkeeperDemo();
        System.out.println("========== 1. 系统初始化 ==========");
        demo.setupInitialData();
        System.out.println("\n========== 2. 进入命令行交互模式 ==========");
        demo.runCli();
    }

    public void setupInitialData() {
        System.out.println("创建默认账户和类别...");
        accCash = new Account("现金账户", new BigDecimal("1500.00"));
        accBank = new Account("银行储蓄", new BigDecimal("10000.00"));
        repository.getAccountRepository().save(accCash);
        repository.getAccountRepository().save(accBank);

        catSalary = systemService.createCategory("工资", TransactionType.INCOME);
        catRent = systemService.createCategory("住房", TransactionType.EXPENSE);
        catFood = systemService.createCategory("餐饮", TransactionType.EXPENSE);
        
        System.out.println("初始化完成。");
    }

    public void runCli() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("欢迎使用命令行记账本！输入 'help' 查看可用命令。");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+", 5);
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "help":
                        printHelp();
                        break;
                    case "accounts":
                        listAccounts();
                        break;
                    case "categories":
                        listCategories();
                        break;
                    case "income":
                    case "expense":
                        recordTransaction(parts, command.equals("income") ? TransactionType.INCOME : TransactionType.EXPENSE);
                        break;
                    case "report":
                        showReport();
                        break;
                    case "exit":
                        System.out.println("感谢使用，再见！");
                        scanner.close();
                        return;
                    default:
                        System.out.println("未知命令。输入 'help' 查看可用命令。");
                        break;
                }
            } catch (Exception e) {
                System.err.println("操作失败: " + e.getMessage());
            }
        }
    }

    private void printHelp() {
        System.out.println("可用命令:");
        System.out.println("  help                               - 显示此帮助信息");
        System.out.println("  accounts                           - 列出所有账户");
        System.out.println("  categories                         - 列出所有类别");
        System.out.println("  income <金额> <账户ID> <类别ID> <描述> - 记录一笔收入");
        System.out.println("  expense <金额> <账户ID> <类别ID> <描述> - 记录一笔支出");
        System.out.println("  report                             - 显示所有交易和月度总览");
        System.out.println("  exit                               - 退出程序");
    }

    private void listAccounts() {
        System.out.println("可用账户:");
        List<Account> accounts = repository.getAccountRepository().findAll();
        if (accounts.isEmpty()) {
            System.out.println("  没有找到账户。");
        } else {
            accounts.forEach(System.out::println);
        }
    }

    private void listCategories() {
        System.out.println("可用类别:");
        List<Category> categories = repository.getCategoryRepository().findAll();
        if (categories.isEmpty()) {
            System.out.println("  没有找到类别。");
        } else {
            categories.forEach(System.out::println);
        }
    }

    private void recordTransaction(String[] parts, TransactionType type) {
        if (parts.length < 5) {
            System.out.println("参数不足。用法: " + type.name().toLowerCase() + " <金额> <账户ID> <类别ID> <描述>");
            return;
        }

        BigDecimal amount = new BigDecimal(parts[1]);
        String accountId = parts[2];
        String categoryId = parts[3];
        String memo = parts[4];

        Transaction tx = new Transaction(
            amount,
            type,
            LocalDateTime.now(),
            categoryId,
            accountId
        );
        tx.setMemo(memo);

        Transaction savedTx = transactionService.recordTransaction(tx);
        System.out.println("记录成功: " + savedTx);
        
        Account updatedAccount = repository.getAccountRepository().findById(accountId).get();
        System.out.println("账户 " + updatedAccount.getName() + " 更新后余额: " + updatedAccount.getBalance());
    }

    private void showReport() {
        System.out.println("--- 所有交易记录 ---");
        List<Transaction> transactions = repository.getTransactionRepository().findAll();
        if (transactions.isEmpty()) {
            System.out.println("  暂无交易记录。");
        } else {
            transactions.forEach(System.out::println);
        }

        System.out.println("\n--- " + YearMonth.now() + " 月度总览 ---");
        Map<String, BigDecimal> overview = reportingService.getMonthlyOverview(YearMonth.now());
        System.out.println("  总收入: " + overview.getOrDefault("totalIncome", BigDecimal.ZERO));
        System.out.println("  总支出: " + overview.getOrDefault("totalExpense", BigDecimal.ZERO));
        System.out.println("  净收入: " + overview.getOrDefault("netIncome", BigDecimal.ZERO));
    }
}
