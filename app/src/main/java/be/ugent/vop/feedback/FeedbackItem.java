package be.ugent.vop.feedback;

/**
 * Created by vincent on 11/04/15.
 */
public class FeedbackItem {
    public static final String TYPE_BUG = "Bug report";
    public static final String TYPE_OTHER = "Other";
    public static final String TYPE_IDEA = "Idea";

    private String message;
    private String type;

    public FeedbackItem(){};

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }
}
