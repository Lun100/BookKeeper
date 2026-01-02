package util;
/**
 * 自定义校验异常
 * 用于处理如 "金额必须 > 0" 或 "必填字段为空" 的情况 [cite: 25, 167]
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
