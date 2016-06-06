package proxy.utils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by matias on 6/5/16.
 */
@XmlRootElement(name="Properties")
public class Properties {
    int buffersize;
    int serverport;
    int pop3Port;
    List<AdminProperties> adminProperties;
    boolean leet;

    @XmlElement(name = "BufferSize")
    public void setBuffersize(int bs) {
        this.buffersize = bs;
    }

    @XmlElement(name = "ServerPort")
    public void setServerport(int sp) {
        this.serverport = sp;
    }

    @XmlElement(name = "Admin")
    public void setAdminProperties(List<AdminProperties> ad) {
        this.adminProperties = ad;
    }

    @XmlElement(name = "pop3-port")
    public void setPop3Port(int p3p) {
        this.pop3Port = p3p;
    }

    @XmlElement(name = "L33t")
    public void setLeet(boolean leet) {
        this.leet = leet;
    }

    public boolean getLeet() { return this.leet; }

    public int getBuffersize() {
        return this.buffersize;
    }

    public int getServerport() {
        return this.serverport;
    }

    public List<AdminProperties> getAdminProperties() {
        return this.adminProperties;
    }
    public int getPop3Port() {
        return this.pop3Port;
    }
}
