package be.ugent.vop.backend;

/**
 * Created by Lars on 21/03/15.
 */
public class ResultBean {
    private String message;
    private int code;
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
