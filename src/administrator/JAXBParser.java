package administrator;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Created by matias on 6/5/16.
 */

//link de refenrencia: http://www.mkyong.com/java/jaxb-hello-world-example/
public class JAXBParser {

    public static XMLConfiguration getConfiguration() {
         XMLConfiguration c = null;

        try {
            File file = new File("./XMLConfiguration.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLConfiguration.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            c = (XMLConfiguration) jaxbUnmarshaller.unmarshal(file);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return c;
    }
}