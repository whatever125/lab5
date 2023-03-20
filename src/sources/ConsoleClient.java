package sources;

import sources.IOHandlers.client.BasicReader;
import sources.IOHandlers.client.CustomConsoleReader;
import sources.IOHandlers.client.CustomFileReader;
import sources.commands.*;
import sources.exceptions.client.FileRecursionError;
import sources.exceptions.client.InvalidCommandException;
import sources.exceptions.client.InvalidScriptException;
import sources.exceptions.client.WrongNumberOfArgumentsException;
import sources.exceptions.io.EndOfInputException;
import sources.exceptions.io.FilePermissionException;
import sources.exceptions.io.InvalidFileDataException;
import sources.exceptions.io.WrongArgumentException;
import sources.exceptions.receiver.CollectionKeyException;
import sources.models.Movie;
import sources.models.MovieGenre;
import sources.models.MpaaRating;
import sources.models.helpers.MovieArgumentChecker;
import sources.models.helpers.PersonArgumentChecker;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class ConsoleClient implements Client {
    Invoker invoker;
    Receiver receiver;
    private final Stack<String> pathStack = new Stack<>();
    private boolean canExit = false;

    public void main() {
        try {
            // Initialize Invoker, Receiver and Reader
            invoker = new Invoker();
            receiver = new Receiver();
            BasicReader consoleReader = new CustomConsoleReader();

            System.out.println("Data loaded successfully. You are now in interactive mode\nType 'help' to see the list of commands\n");

            while (!canExit) {
                try {
                    readAndExecuteCommand(consoleReader);
                } catch (InvalidCommandException | CollectionKeyException | WrongNumberOfArgumentsException |
                         WrongArgumentException | InvalidScriptException e) {
                    System.out.println(e.getMessage());
                }
            }

        } catch (NullPointerException e) {
            // Handle NullPointerException thrown when LAB5 environment variable is not set
            System.out.println(e.getMessage());
            System.out.println("! path variable is null !");
            System.exit(0);
        } catch (InvalidFileDataException | EndOfInputException | FilePermissionException | FileNotFoundException e) {
            // Handle exceptions thrown when there is a problem with the data file or the user input
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    private void readAndExecuteCommand(BasicReader basicReader) throws InvalidCommandException, CollectionKeyException,
            WrongNumberOfArgumentsException, WrongArgumentException, InvalidScriptException {
        String input = basicReader.readLine().trim();
        if (input.startsWith("//") || input.equals("")) {
            return;
        }
        String[] inputArray = input.split("\s+");
        String commandName = inputArray[0].toLowerCase();

        String[] args = new String[inputArray.length - 1];
        System.arraycopy(inputArray, 1, args, 0, inputArray.length - 1);

        switch (commandName) {
            case "help" -> {
                if (args.length != 0)
                    throw new WrongNumberOfArgumentsException();
                invoker.execute(new Help(this, receiver));
            }
            case "print" -> {
                if (args.length != 0)
                    throw new WrongNumberOfArgumentsException();
                invoker.printCommandHistory();
            }
            case "info" -> {
                if (args.length != 0)
                    throw new WrongNumberOfArgumentsException();
                String result = invoker.executeAndReturn(new Info(this, receiver));
                System.out.println(result);
            }
            case "show" -> {
                if (args.length != 0)
                    throw new WrongNumberOfArgumentsException();
                String result = invoker.executeAndReturn(new Show(this, receiver));
                System.out.println(result);
            }
            case "insert" -> {
                if (args.length != 1)
                    throw new WrongNumberOfArgumentsException();
                try {
                    Integer key = Integer.parseInt(args[0]);
                    String movieName = readMovieName(basicReader);
                    Integer x = readX(basicReader);
                    Integer y = readY(basicReader);
                    long oscarsCount = readOscrasCount(basicReader);
                    MovieGenre movieGenre = readMovieGenre(basicReader);
                    MpaaRating mpaaRating = readMpaaRating(basicReader);
                    String directorName = readDirectorName(basicReader);
                    LocalDateTime birthday = readBirthday(basicReader);
                    Integer weight = readWeight(basicReader);
                    String passportID = readPassportID(basicReader);
                    invoker.execute(new Insert(this, receiver, key, movieName, x, y, oscarsCount,
                            movieGenre, mpaaRating, directorName, birthday, weight, passportID));
                } catch (NumberFormatException e) {
                    String errorMessage = "! not an integer !";
                    if (inScriptMode()) {
                        throw new InvalidScriptException(errorMessage);
                    } else {
                        System.out.println(errorMessage);
                    }
                }
            }
            case "update" -> {
                if (args.length != 1)
                    throw new WrongNumberOfArgumentsException();
                try {
                    Integer id = Integer.parseInt(args[0]);
                    String movieName = readMovieName(basicReader);
                    Integer x = readX(basicReader);
                    Integer y = readY(basicReader);
                    long oscarsCount = readOscrasCount(basicReader);
                    MovieGenre movieGenre = readMovieGenre(basicReader);
                    MpaaRating mpaaRating = readMpaaRating(basicReader);
                    String directorName = readDirectorName(basicReader);
                    LocalDateTime birthday = readBirthday(basicReader);
                    Integer weight = readWeight(basicReader);
                    String passportID = readPassportID(basicReader);
                    invoker.execute(new Update(this, receiver, id, movieName, x, y, oscarsCount,
                            movieGenre, mpaaRating, directorName, birthday, weight, passportID));
                } catch (NumberFormatException e) {
                    String errorMessage = "! not an integer !";
                    if (inScriptMode()) {
                        throw new InvalidScriptException(errorMessage);
                    } else {
                        System.out.println(errorMessage);
                    }
                }
            }
            case "remove_key" -> {
                if (args.length != 1)
                    throw new WrongNumberOfArgumentsException();
                try {
                    Integer key = Integer.parseInt(args[0]);
                    invoker.execute(new RemoveKey(this, receiver, key));
                } catch (NumberFormatException e) {
                    String errorMessage = "! not an integer !";
                    if (inScriptMode()) {
                        throw new InvalidScriptException(errorMessage);
                    } else {
                        System.out.println(errorMessage);
                    }
                }
            }
            case "clear" -> {
                if (args.length != 0)
                    throw new WrongNumberOfArgumentsException();
                invoker.execute(new Clear(this, receiver));
            }
            case "save" -> {
                if (args.length != 0)
                    throw new WrongNumberOfArgumentsException();
                invoker.execute(new Save(this, receiver));
            }
            case "execute_script" -> {
                if (args.length != 1)
                    throw new WrongNumberOfArgumentsException();
                String path = args[0];
                invoker.execute(new ExecuteScript(this, receiver, path));
            }
            case "exit" -> {
                if (args.length != 0)
                    throw new WrongNumberOfArgumentsException();
                invoker.execute(new Exit(this, receiver));
            }
            case "remove_greater" -> {
                if (args.length != 0)
                    throw new WrongNumberOfArgumentsException();
                String movieName = readMovieName(basicReader);
                Integer x = readX(basicReader);
                Integer y = readY(basicReader);
                long oscarsCount = readOscrasCount(basicReader);
                MovieGenre movieGenre = readMovieGenre(basicReader);
                MpaaRating mpaaRating = readMpaaRating(basicReader);
                String directorName = readDirectorName(basicReader);
                LocalDateTime birthday = readBirthday(basicReader);
                Integer weight = readWeight(basicReader);
                String passportID = readPassportID(basicReader);
                invoker.execute(new RemoveGreater(this, receiver, movieName, x, y,
                        oscarsCount, movieGenre, mpaaRating, directorName, birthday, weight, passportID));
            }
            case "replace_if_lowe" -> {
                if (args.length != 1)
                    throw new WrongNumberOfArgumentsException();
                try {
                    Integer key = Integer.parseInt(args[0]);
                    String movieName = readMovieName(basicReader);
                    Integer x = readX(basicReader);
                    Integer y = readY(basicReader);
                    long oscarsCount = readOscrasCount(basicReader);
                    MovieGenre movieGenre = readMovieGenre(basicReader);
                    MpaaRating mpaaRating = readMpaaRating(basicReader);
                    String directorName = readDirectorName(basicReader);
                    LocalDateTime birthday = readBirthday(basicReader);
                    Integer weight = readWeight(basicReader);
                    String passportID = readPassportID(basicReader);
                    invoker.execute(new ReplaceIfLowe(this, receiver, key, movieName, x, y,
                            oscarsCount, movieGenre, mpaaRating, directorName, birthday, weight, passportID));
                } catch (NumberFormatException e) {
                    String errorMessage = "! not an integer !";
                    if (inScriptMode()) {
                        throw new InvalidScriptException(errorMessage);
                    } else {
                        System.out.println(errorMessage);
                    }
                }
            }
            case "remove_lower_key" -> {
                if (args.length != 1)
                    throw new WrongNumberOfArgumentsException();
                try {
                    Integer key = Integer.parseInt(args[0]);
                    invoker.execute(new RemoveLowerKey(this, receiver, key));
                } catch (NumberFormatException e) {
                    String errorMessage = "! not an integer !";
                    if (inScriptMode()) {
                        throw new InvalidScriptException(errorMessage);
                    } else {
                        System.out.println(errorMessage);
                    }
                }
            }
            case "print_ascending" -> {
                if (args.length != 0)
                    throw new WrongNumberOfArgumentsException();
                List<Movie> movieList = invoker.executeAndReturn(new PrintAscending(this, receiver));
                for (Movie movie : movieList) {
                    System.out.println(movie);
                }
            }
            case "print_descending" -> {
                if (args.length != 0)
                    throw new WrongNumberOfArgumentsException();
                List<Movie> movieList = invoker.executeAndReturn(new PrintDescending(this, receiver));
                for (Movie movie : movieList) {
                    System.out.println(movie);
                }
            }
            case "print_field_descending_oscars_count" -> {
                if (args.length != 0)
                    throw new WrongNumberOfArgumentsException();
                List<Movie> movieList = invoker.executeAndReturn(new PrintFieldDescendingOscarsCount(this, receiver));
                System.out.println("Oscars count - Movie name");
                for (Movie movie : movieList) {
                    System.out.println(movie.getOscarsCount() + " - " + movie.getName());
                }
            }

            default -> throw new InvalidCommandException(commandName);
        }
    }

    private String readMovieName(BasicReader basicReader) throws InvalidScriptException {
        String movieName = null;
        while (movieName == null) {
            try {
                String input = basicReader.readLine("Enter movie name");
                movieName = input.trim();
                MovieArgumentChecker.checkName(movieName);
            } catch (WrongArgumentException e) {
                movieName = null;
                if (inScriptMode()) {
                    throw new InvalidScriptException(e.getMessage());
                } else {
                    System.out.println(e.getMessage());
                }
            }
        }
        return movieName;
    }

    private Integer readX(BasicReader basicReader) throws InvalidScriptException {
        Integer x = null;
        boolean xSuccess = false;
        while (!xSuccess) {
            try {
                String input = basicReader.readLine("Enter X coordinate");
                String xInput = input.trim();
                x = Integer.parseInt(xInput);
                xSuccess = true;
            } catch (NumberFormatException e) {
                String errorMessage = "! not an integer !";
                if (inScriptMode()) {
                    throw new InvalidScriptException(errorMessage);
                } else {
                    System.out.println(errorMessage);
                }
            }
        }
        return x;
    }

    private Integer readY(BasicReader basicReader) throws InvalidScriptException {
        Integer y = null;
        boolean ySuccess = false;
        while (!ySuccess) {
            try {
                String input = basicReader.readLine("Enter Y coordinate");
                String xInput = input.trim();
                y = Integer.parseInt(xInput);
                ySuccess = true;
            } catch (NumberFormatException e) {
                String errorMessage = "! not an integer !";
                if (inScriptMode()) {
                    throw new InvalidScriptException(errorMessage);
                } else {
                    System.out.println(errorMessage);
                }
            }
        }
        return y;
    }

    private long readOscrasCount(BasicReader basicReader) throws InvalidScriptException {
        long oscarsCount = 0;
        boolean oscarsCountSuccess = false;
        while (!oscarsCountSuccess) {
            try {
                String input = basicReader.readLine("Enter oscars count");
                String oscarsCountInput = input.trim();
                oscarsCount = Long.parseLong(oscarsCountInput);
                MovieArgumentChecker.checkOscarsCount(oscarsCount);
                oscarsCountSuccess = true;
            } catch (NumberFormatException e) {
                String errorMessage = "! not an integer !";
                if (inScriptMode()) {
                    throw new InvalidScriptException(errorMessage);
                } else {
                    System.out.println(errorMessage);
                }
            } catch (WrongArgumentException e) {
                String errorMessage = e.getMessage();
                if (inScriptMode()) {
                    throw new InvalidScriptException(errorMessage);
                } else {
                    System.out.println(errorMessage);
                }
            }
        }
        return oscarsCount;
    }

    private MovieGenre readMovieGenre(BasicReader basicReader) throws InvalidScriptException {
        MovieGenre movieGenre = null;

        StringBuilder message = new StringBuilder("Enter movie genre (");
        for (MovieGenre genre : MovieGenre.values()) {
            message.append(genre.toString());
            message.append("; ");
        }
        message.delete(message.length() - 2, message.length());
        message.append(")");

        while (movieGenre == null) {
            String input = basicReader.readLine(String.valueOf(message));
            String movieGenreInput = input.trim();
            try {
                movieGenre = MovieGenre.valueOf(movieGenreInput.toUpperCase());
            } catch (IllegalArgumentException e) {
                String errorMessage = "! wrong movie genre !";
                if (inScriptMode()) {
                    throw new InvalidScriptException(errorMessage);
                } else {
                    System.out.println(errorMessage);
                }
            }
        }
        return movieGenre;
    }

    private MpaaRating readMpaaRating(BasicReader basicReader) throws InvalidScriptException {
        MpaaRating mpaaRating = null;

        StringBuilder message = new StringBuilder("Enter MPAA rating (");
        for (MpaaRating rating : MpaaRating.values()) {
            message.append(rating.toString());
            message.append("; ");
        }
        message.delete(message.length() - 2, message.length());
        message.append(")");

        while (mpaaRating == null) {
            String input = basicReader.readLine(String.valueOf(message));
            String mpaaRatingInput = input.trim();
            try {
                mpaaRating = MpaaRating.valueOf(mpaaRatingInput.toUpperCase());
            } catch (IllegalArgumentException e) {
                String errorMessage = "! wrong MPAA rating !";
                if (inScriptMode()) {
                    throw new InvalidScriptException(errorMessage);
                } else {
                    System.out.println(errorMessage);
                }
            }
        }
        return mpaaRating;
    }

    private String readDirectorName(BasicReader basicReader) throws InvalidScriptException {
        String directorName = null;
        while (directorName == null) {
            try {
                String input = basicReader.readLine("Enter director name");
                directorName = input.trim();
                PersonArgumentChecker.checkName(directorName);
            } catch (WrongArgumentException e) {
                directorName = null;
                if (inScriptMode()) {
                    throw new InvalidScriptException(e.getMessage());
                } else {
                    System.out.println(e.getMessage());
                }
            }
        }
        return directorName;
    }

    private LocalDateTime readBirthday(BasicReader basicReader) throws InvalidScriptException {
        LocalDateTime birthday = null;
        while (birthday == null) {
            try {
                String input = basicReader.readLine("Enter director birthday in DD.MM.YYYY format");
                String birthdayInput = input.trim();
                birthday = LocalDate.parse(birthdayInput, DateTimeFormatter.ofPattern("dd.MM.yyyy")).atStartOfDay();
            } catch (DateTimeParseException e) {
                String errorMessage = "! wrong date format !";
                if (inScriptMode()) {
                    throw new InvalidScriptException(errorMessage);
                } else {
                    System.out.println(errorMessage);
                }
            }
        }
        return birthday;
    }

    private Integer readWeight(BasicReader basicReader) throws InvalidScriptException {
        Integer weight = null;
        boolean weightSuccess = false;
        while (!weightSuccess) {
            try {
                String input = basicReader.readLine("Enter director weight");
                String weightInput = input.trim();
                if (!weightInput.equals("")) {
                    weight = Integer.parseInt(weightInput);
                }
                PersonArgumentChecker.checkWeight(weight);
                weightSuccess = true;
            } catch (NumberFormatException e) {
                String errorMessage = "! not an integer !";
                if (inScriptMode()) {
                    throw new InvalidScriptException(errorMessage);
                } else {
                    System.out.println(errorMessage);
                }
            } catch (WrongArgumentException e) {
                if (inScriptMode()) {
                    throw new InvalidScriptException(e.getMessage());
                } else {
                    System.out.println(e.getMessage());
                }
            }
        }
        return weight;
    }

    private String readPassportID(BasicReader basicReader) throws InvalidScriptException {
        boolean passportIDSuccess = false;
        String passportID = null;
        while (!passportIDSuccess) {
            try {
                String input = basicReader.readLine("Enter director passport ID");
                passportID = input.trim();
                if (Objects.equals(passportID, "")) {
                    passportID = null;
                }
                PersonArgumentChecker.checkPassportID(passportID); //todo check unique
                passportIDSuccess = true;
            } catch (WrongArgumentException e) {
                if (inScriptMode()) {
                    throw new InvalidScriptException(e.getMessage());
                } else {
                    System.out.println(e.getMessage());
                }
            }
        }
        return passportID;
    }

    @Override
    public void help() {
        System.out.println("""
                *list of commands*
                help : вывести справку по доступным командам
                info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)
                show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении
                insert null {element} : добавить новый элемент с заданным ключом
                update id {element} : обновить значение элемента коллекции, id которого равен заданному
                remove_key null : удалить элемент из коллекции по его ключу
                clear : очистить коллекцию
                save : сохранить коллекцию в файл
                execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.
                exit : завершить программу (без сохранения в файл)
                remove_greater {element} : удалить из коллекции все элементы, превышающие заданный
                replace_if_lowe null {element} : заменить значение по ключу, если новое значение меньше старого
                remove_lower_key null : удалить из коллекции все элементы, ключ которых меньше, чем заданный
                print_ascending : вывести элементы коллекции в порядке возрастания
                print_descending : вывести элементы коллекции в порядке убывания
                print_field_descending_oscars_count : вывести значения поля oscarsCount всех элементов в порядке убывания""");
    }

    @Override
    public void exit() {
        canExit = true;
    }

    @Override
    public void executeScript(String path) {
        try {
            if (pathStack.contains(path))
                // todo check with paths
                throw new FileRecursionError(path);

            BasicReader basicReader = new CustomFileReader(path);
            pathStack.push(path);
            int lineCounter = 0;
            while (basicReader.hasNextLine()) {
                try {
                    lineCounter += 1;
                    readAndExecuteCommand(basicReader);
                } catch (InvalidCommandException | CollectionKeyException | WrongNumberOfArgumentsException |
                         WrongArgumentException | InvalidScriptException e) {
                    System.out.println(printPathStack() + ":" + lineCounter + ": " + e.getMessage());
                }
            }
            pathStack.pop();
        } catch (FileRecursionError | FileNotFoundException | FilePermissionException e) {
            System.out.println(e.getMessage());
        }
    }

    private String printPathStack() {
        StringBuilder returnString = new StringBuilder();
        for (String path : pathStack) {
            returnString.append(path).append(":");
        }
        returnString.deleteCharAt(returnString.length() - 1);
        return returnString.toString();
    }

    private boolean inScriptMode() {
        return !pathStack.empty();
    }
}
