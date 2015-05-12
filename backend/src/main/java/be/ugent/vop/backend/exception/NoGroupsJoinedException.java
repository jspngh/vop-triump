package be.ugent.vop.backend.exception;

import com.google.api.server.spi.ServiceException;

public class NoGroupsJoinedException extends ServiceException{
    public NoGroupsJoinedException(){
        super(409, "User is not a member of any group");
    }
}
