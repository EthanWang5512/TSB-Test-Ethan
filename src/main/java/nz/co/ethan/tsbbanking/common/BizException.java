package nz.co.ethan.tsbbanking.common;


public class BizException extends RuntimeException {
    private final String code;

    public BizException(String code) {
        super(code);
        this.code = code;
    }
    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }
    public String getCode() { return code; }

    public static BizException error(String code) { return new BizException(code); }
    public static BizException error(String code, String message) { return new BizException(code, message); }
}
