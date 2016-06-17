package administrator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matias on 6/11/16.
 */
public class Configuration {
    private static XMLConfiguration XMLConfiguration;
    private static Configuration instance = null;

    protected Configuration() {
        XMLConfiguration = JAXBParser.getConfiguration();
    }

    public static Configuration getInstance() {
        if(instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public XMLConfiguration getConfiguration() {
        return Configuration.XMLConfiguration;
    }


    public void insertUser(XMLMultiplex u){
        List<XMLMultiplex> l = XMLConfiguration.getMultiplexConfig().get(0).getMultiplexConfig();
        //este caso de null es para cuando el archivo no tiene
        //ningun <multiplex>, o sea, no tiene usuarios en el archivo.
        if(l == null) {
            l = new ArrayList<>();
            XMLConfiguration.getMultiplexConfig().get(0).setMultiplexConfig(l);
        }
        l.add(u);
        JAXBParser.updateConfiguration(XMLConfiguration);
    }

    public void updateUser() {
        JAXBParser.updateConfiguration(XMLConfiguration);
    }
}
