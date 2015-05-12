package be.ugent.vop.backend.loaders;

public class AsyncResult< D > {
    private Exception exception;
    private D data;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public D getData() {
        return data;
    }

    public void setData(D data) {
        this.data = data;
    }
}
