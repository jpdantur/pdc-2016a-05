package pop3;


import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by root on 5/27/16.
 */
public class PopParser {
    private StringBuffer temp = new StringBuffer();
    private Queue<StringBuffer> lineQueue = new LinkedList<>();
    private boolean subjectReady = false;
    public void parseData(StringBuffer stringBuffer){
        for (int i=0; i<stringBuffer.length(); i++)
        {
            temp.append(stringBuffer.charAt(i));
            if (stringBuffer.charAt(i)=='\n') // si estoy en una imagen la validacion va a ser otra
            {
                lineQueue.offer(temp);
                temp=new StringBuffer();
            }
        }
        StringBuffer curLine = lineQueue.poll();
        //proceso la linea (o la foto)
        stringBuffer.replace(0, stringBuffer.length()-1,curLine.toString());
        return;
    }
}
