package model;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import java.math.BigDecimal;
import util.TransactionType; //
import util.ValidationException; //

class AccountFuzzTest {

    // Jazzer 执行引擎会自动寻找并调用这个名为 fuzzerTestOneInput 的静态方法
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        try {
            // 1. 生成随机初始余额
            double initialVal = data.consumeDouble();
            Account account = new Account("FuzzAcc", BigDecimal.valueOf(initialVal)); //

            // 2. 生成随机变动金额
            double changeVal = data.consumeDouble();
            BigDecimal amount = BigDecimal.valueOf(changeVal);

            // 3. 随机选择交易类型
            TransactionType type = data.pickValue(TransactionType.values()); //

            // 4. 执行业务逻辑
            account.updateBalance(amount, type); //

        } catch (ValidationException e) {
            // 预期的业务校验异常，不属于 Bug，允许模糊测试继续
        } catch (Throwable t) {
            // 如果出现 NullPointerException 或其它 RuntimeException，Jazzer 会捕获并报告
            throw t;
        }
    }
}