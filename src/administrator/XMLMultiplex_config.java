package administrator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by matias on 6/11/16.
 */
@XmlRootElement(name="Multiplexing-config")
public class XMLMultiplex_config {
    private List<XMLMultiplex> multiplexList;

    public List<XMLMultiplex> getMultiplexConfig() {
        return this.multiplexList;
    }

    @XmlElement(name = "Multiplex")
    public void setMultiplexConfig(List<XMLMultiplex> multiplexList) {
        this.multiplexList = multiplexList;
    }

    public String toString() {
        String out = "";
        int i = 0;

        if(multiplexList.size() == 0) {
            out = "No users for multiplexing from start";
        } else {
            for(; i < multiplexList.size() -1; i++) {
                out += multiplexList.get(i).getUser() + "\r\n";
            }

            out += multiplexList.get(i);
        }

        return out;
    }
}
