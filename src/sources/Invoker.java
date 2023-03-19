package sources;

import sources.commands.AbstractCommand;
import sources.commands.AbstractCommandWithResult;
import sources.exceptions.receiver.CollectionKeyException;
import sources.exceptions.receiver.EmptyCollectionException;
import sources.exceptions.io.WrongArgumentException;

import java.util.Stack;

public class Invoker {
    private final Stack<AbstractCommand> commandHistory = new Stack<>();

    public void execute(AbstractCommand command) throws CollectionKeyException, WrongArgumentException,
            EmptyCollectionException {
        commandHistory.push(command);
        command.execute();
    }

    public <T> T executeAndReturn(AbstractCommandWithResult<T> command) throws EmptyCollectionException,
            WrongArgumentException, CollectionKeyException {
        commandHistory.push(command);
        command.execute();
        return command.getResult();
    }

    public Stack<AbstractCommand> getCommandHistory() {
        return commandHistory;
    }

    public void printCommandHistory() {
        for (AbstractCommand command : commandHistory) {
            System.out.println(command);
        }
    }
}
