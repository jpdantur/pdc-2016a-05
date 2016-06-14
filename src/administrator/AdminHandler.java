package administrator;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * Created by matias on 6/13/16.
 */
public class AdminHandler {
    private ByteBuffer buffer;
    private boolean rcvdWellcome;
    private SelectionKey key;
    private String user;
    private String pass;

    public AdminHandler(int bufsize, SelectionKey key){
        this.buffer = ByteBuffer.allocate(bufsize);
        this.key = key;
        this.rcvdWellcome = true;
    }

    public ByteBuffer getBuffer() {
        return  this.buffer;
    }

    public SelectionKey getKey() {
        return  this.key;
    }

    public boolean getRcvdWellcome() {
        return this.rcvdWellcome;
    }

    public void setRcvdWellcome(boolean value) {
        this.rcvdWellcome = value;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return this.user;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getPass() {
        return this.pass;
    }
}
