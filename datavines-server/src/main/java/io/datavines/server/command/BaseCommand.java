package io.datavines.server.command;

import java.io.Serializable;

public class BaseCommand implements Serializable {

    protected CommandCode commandCode = CommandCode.DEFAULT;

    public CommandCode getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(CommandCode commandCode) {
        this.commandCode = commandCode;
    }
}
