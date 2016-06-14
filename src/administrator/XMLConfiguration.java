package administrator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by matias on 6/11/16.
 */
@XmlRootElement(name="Configuration")
public class XMLConfiguration {
    private int bufferSize;
    private int serverPort;
    private int pop3port;
    private String servername;
    private boolean leet;
    private boolean rotation;
    private List<XMLAdmin> adminList;
    private String wellcomeMsg;
    private String gbyeMsg;
    private List<XMLMultiplex_config> mconfigList;

    @XmlElement(name = "BufferSize")
    public void setBufferSize(int bs) {
        this.bufferSize = bs;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    @XmlElement(name = "ServerPort")
    public void setServerPort(int sp) { this.serverPort = sp; }

    public int getServerPort() {
        return this.serverPort;
    }

    @XmlElement(name = "pop3Port")
    public void setPOP3port(int pop3port) { this.pop3port= pop3port; }

    public int getPOP3port() {
        return this.pop3port;
    }

    @XmlElement(name = "Servername")
    public void setServername(String servername) { this.servername = servername; }

    public String getServername() {
        return this.servername;
    }

    @XmlElement(name = "L33t")
    public void setLeet(boolean l) { this.leet = l; }

    public boolean getLeet() {
        return this.leet;
    }

    @XmlElement(name = "Rotation")
    public void setRotation(boolean r) { this.rotation = r; }

    public boolean getRotation() {
        return this.rotation;
    }

    @XmlElement(name = "Admin")
    public void setAdmin(List<XMLAdmin> admins) { this.adminList = admins; }

    public List<XMLAdmin> getAdmin() {
        return this.adminList;
    }

    @XmlElement(name = "WellcomeMessage")
    public void setWellcome(String wmsg) { this.wellcomeMsg = wmsg; }

    public String getWellcome() {
        return this.wellcomeMsg;
    }

    @XmlElement(name = "GoodByeMessage")
    public void setGbyeMsg(String gbmgs) { this.gbyeMsg = gbmgs; }

    public String getGbyeMsg() {
        return this.gbyeMsg;
    }

    @XmlElement(name = "Multiplexing-config")
    public void setMultiplexConfig(List<XMLMultiplex_config> mc) { this.mconfigList = mc; }

    public List<XMLMultiplex_config> getMultiplexConfig() {
        return this.mconfigList;
    }
}
