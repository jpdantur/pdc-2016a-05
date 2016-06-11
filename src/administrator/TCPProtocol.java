package administrator;

/**
 * Created by matias on 6/5/16.
 */
import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface TCPProtocol {
    void handleAccept(SelectionKey key) throws IOException;
    void handleRead(SelectionKey key) throws IOException;
    void handleWrite(SelectionKey key) throws IOException;
}