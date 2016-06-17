package administrator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by matias on 6/10/16.
 */
@XmlRootElement(name="Multiplex")
public class XMLMultiplex {
    String user;
    String host;
    int port;

    @XmlElement(name="User")
    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return this.user;
    }

    @XmlElement(name="Port")
    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    @XmlElement(name="Host")
    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() { return this.host; }

    public XMLMultiplex(){

    }

    public XMLMultiplex(String user, String host, String port) {
        this.user = user;
        this.host = host;
        this.port = Integer.valueOf(port);
    }
}
