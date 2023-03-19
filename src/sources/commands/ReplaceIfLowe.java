package sources.commands;

import sources.Client;
import sources.Receiver;
import sources.exceptions.receiver.CollectionKeyException;
import sources.exceptions.io.WrongArgumentException;
import sources.models.MovieGenre;
import sources.models.MpaaRating;

import java.time.LocalDateTime;

public class ReplaceIfLowe extends AbstractCommand {
    private final Integer key;
    private final String movieName;
    private final Integer x;
    private final Integer y;
    private final long oscarsCount;
    private final MovieGenre movieGenre;
    private final MpaaRating mpaaRating;
    private final String directorName;
    private final LocalDateTime birthday;
    private final Integer weight;
    private final String passportID;

    public ReplaceIfLowe(Client client, Receiver receiver, Integer key, String movieName, Integer x,
                         Integer y, long oscarsCount, MovieGenre movieGenre, MpaaRating mpaaRating, String directorName,
                         LocalDateTime birthday, Integer weight, String passportID) {
        super("replace_if_lowe", client, receiver);
        this.key = key;
        this.movieName = movieName;
        this.x = x;
        this.y = y;
        this.oscarsCount = oscarsCount;
        this.movieGenre = movieGenre;
        this.mpaaRating = mpaaRating;
        this.directorName = directorName;
        this.birthday = birthday;
        this.weight = weight;
        this.passportID = passportID;
    }

    @Override
    public void execute() throws CollectionKeyException, WrongArgumentException {
        receiver.replaceIfLowe(key, movieName, x, y, oscarsCount, movieGenre,
                mpaaRating, directorName, birthday, weight, passportID);
    }

    @Override
    public String toString() {
        return name + " {" +
                "key=" + key +
                ", movieName='" + movieName + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", oscarsCount=" + oscarsCount +
                ", movieGenre=" + movieGenre +
                ", mpaaRating=" + mpaaRating +
                ", directorName='" + directorName + '\'' +
                ", birthday=" + birthday +
                ", weight=" + weight +
                ", passportID='" + passportID + '\'' +
                '}';
    }
}
