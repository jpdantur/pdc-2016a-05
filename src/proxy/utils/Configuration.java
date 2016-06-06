package proxy.utils;

/**
 * Created by matias on 6/5/16.
 */
public class Configuration {
    private static Configuration instance = null;
    private static Properties prop;

    protected Configuration() {
        this.prop = JAXBParser.getProperties();
    }
    public static Configuration getInstance() {
        if(instance == null) {
            instance = new Configuration();
        }

        return instance;
    }

    public Properties getProp() {
        return this.prop;
    }
}
