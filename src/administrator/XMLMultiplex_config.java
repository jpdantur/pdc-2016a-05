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

    @XmlElement(name = "Multiplexing-config")
    public void setMultiplexConfig(List<XMLMultiplex> multiplexList) {
        this.multiplexList = multiplexList;
    }
}
