package be.ugent.vop.backend.exception;

import com.google.api.server.spi.ServiceException;

public class CheckinDelayException extends ServiceException {
    public CheckinDelayException(){
        super(409, "Checkin delay not passed yet");
    }
}
