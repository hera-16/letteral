package com.chatapp.exception;

/**
 * ビジネスロジックエラーの例外
 * 業務ルール違反などで処理を継続できない場合に使用します
 */
public class BusinessException extends RuntimeException {

    private String errorCode;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
