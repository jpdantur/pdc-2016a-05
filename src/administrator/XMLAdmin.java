package administrator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by matias on 6/5/16.
 */
@XmlRootElement(name="XMLAdmin")
public class XMLAdmin {
    String user;
    String pass;
    int port;

    public String getUser() {
        return this.user;
    }

    @XmlElement
    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return this.pass;
    }

    @XmlElement
    public void setPass(String pass) {
        this.pass = pass;
    }

    public int getPort() {
        return this.port;
    }

    @XmlElement
    public void setPort(int port) {
        this.port = port;
    }

}
