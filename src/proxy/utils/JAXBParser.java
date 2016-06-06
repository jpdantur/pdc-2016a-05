package proxy.utils;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Created by matias on 6/5/16.
 */
public class JAXBParser {
    public static Properties getProperties() {
        Properties prop = null;

        try {
            File file = new File("./properties.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Properties.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            prop = (Properties) jaxbUnmarshaller.unmarshal(file);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return prop;
    }
}