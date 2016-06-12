package administrator;

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

}
