package administrator;

/**
 * Created by matias on 6/5/16.
 */
import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface TCPProtocol {
    void handleAccept(SelectionKey key, Administrator ad) throws IOException;
    void handleRead(SelectionKey key, Administrator ad) throws IOException;
    void handleWrite(SelectionKey key, Administrator ad) throws IOException;
}