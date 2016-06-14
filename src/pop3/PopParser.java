package pop3;


import java.util.LinkedList;
import java.util.Queue;
import java.util.Base64;
import java.util.Base64.*;
import java.util.regex.*;
import java.util.Map;
import java.util.HashMap;
import javaxt.io.Image;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.*;
/**
 * Created by root on 5/27/16.
 */
public class PopParser {
    private BCodec bCodec = new BCodec();
    private QCodec qCodec = new QCodec();

    public StringBuilder getCommandTemp() {
        return commandTemp;
    }

    private StringBuilder commandTemp = new StringBuilder();
    private StringBuilder temp = new StringBuilder();
    private StringBuilder image = new StringBuilder();
    private String codedSubjectRegex = "([ \t]*)(=\\?(.+?)\\?(.+?)\\?(.+?)\\?=)\r\n";
    private Pattern codedSubjectPattern = Pattern.compile(codedSubjectRegex);
    private String imageHeaderRegex = "^content-type:[ \\t]*image/(.*?);.*\r\n";
    private Pattern imageHeaderPattern = Pattern.compile(imageHeaderRegex, Pattern.CASE_INSENSITIVE);
    private Queue<StringBuilder> lineQueue = new LinkedList<>();
    private Queue<StringBuilder> commandQueue = new LinkedList<>();
    private String imageFormat;
    private boolean imageEnabled;
    private boolean subjectEnabled;
    private enum State {HEADER, SUBJECT, POST_SUBJECT, BODY, IMAGE_HEADER, IMAGE}
    private State state = State.HEADER;
    private static final int MAX_IMAGE_SIZE = 500000;


    public void setSubjectEnabled(boolean subjectEnabled) {
        this.subjectEnabled = subjectEnabled;
    }

    public void setImageEnabled(boolean imageEnabled) {this.imageEnabled = imageEnabled; }

    public void resetFlags()
    {
        state = State.HEADER;

    }

    public void parseData(StringBuffer stringBuffer){
        for (int i=0; i<stringBuffer.length(); i++)
        {
            temp.append(stringBuffer.charAt(i));
            if (stringBuffer.charAt(i)=='\n')
            {
                lineQueue.offer(temp);
                temp=new StringBuilder();
            }
        }
        stringBuffer.setLength(0);
        while (!lineQueue.isEmpty()) {//lo que viene ahora podria hacer que lo haga con un while hasta que se vacie la cola
            String curLine = lineQueue.poll().toString();
            //stringBuffer.setLength(0);
            if (curLine.toLowerCase().startsWith("Subject:".toLowerCase()) && state==State.HEADER && subjectEnabled) {
                state = State.SUBJECT;
                curLine = "Subject:" + processSubject(curLine.substring(8));
            } else if (state == State.SUBJECT && (curLine.startsWith(" ") || curLine.startsWith("\t"))) {
                curLine = processSubject(curLine) + "\r\n";
            } else if (state == State.SUBJECT) {
                state = State.POST_SUBJECT;
            } else if (curLine.equals("\r\n") && state == State.POST_SUBJECT) {
                state = State.BODY;
            } else if (isImageHeader(curLine) && state == State.BODY && imageEnabled) {
                state = State.IMAGE_HEADER;
            } else if (state == State.IMAGE_HEADER && curLine.equals("\r\n")) {
               state = State.IMAGE;
            } else if (state == State.IMAGE && !curLine.startsWith("--") && image.length()<=MAX_IMAGE_SIZE) {
                image.append(curLine);
                curLine = "";
            } else if (state == State.IMAGE && !curLine.startsWith("--") && image.length()>MAX_IMAGE_SIZE) {
                state = State.BODY;
                stringBuffer.append(image);
                image.setLength(0);

            } else if (state == State.IMAGE && curLine.startsWith("--")) {
                state = State.BODY;
                stringBuffer.append(processImage());
                stringBuffer.append("\r\n"); //este capaz sobra
                image.setLength(0);
            }
            stringBuffer.append(curLine);
        }
    }

    private boolean isImageHeader(String curLine) {
        Matcher m = imageHeaderPattern.matcher(curLine);
        if (m.matches())
        {
            imageFormat = m.group(1);
        }
        return m.matches();
    }

    private String processImage() {
        Decoder decoder = Base64.getMimeDecoder();
        Encoder encoder = Base64.getMimeEncoder();
        try {
            Image imageXT = new Image(decoder.decode(image.toString()));
            imageXT.rotate(180);
            return new String(encoder.encode(imageXT.getByteArray(mimeToImage(imageFormat))));
        } catch (IllegalArgumentException i)
        {
            i.printStackTrace();
            return image.toString();
        }
    }

    private String processSubject(String data) {
        Matcher m = codedSubjectPattern.matcher(data);
        if (m.matches())
        {
            return m.group(1)+toEncodedLeet(m.group(3),m.group(4),m.group(2));
        }
        return toLeet(data);
    }

    private String toEncodedLeet(String format, String language, String text) {
        String decodedText;
        String ret="";
        try {
            if (language.equals("B")) {
                decodedText = bCodec.decode(text);
                ret = bCodec.encode(toLeet(decodedText),format);
            } else {
                decodedText = qCodec.decode(text);
                ret =  qCodec.encode(toLeet(decodedText),format);
            }
        }
        catch (DecoderException|EncoderException e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    private String toLeet(String data) {
        return data.replace('a','4').replace('e','3').replace('i','1').replace('o','0').replace('c','<');
    }

    private String mimeToImage(String format)
    {
        Map<String,String> list = new HashMap<String,String>();
        list.put("bmp","BMP");
        list.put("gif","GIF");
        list.put("jpeg","JPG");
        list.put("png","PNG");
        list.put("vnd.wap.wbmp","WBMP");
        return list.get(format);
    }

    public Queue<StringBuilder> parseCommands(StringBuffer stringBuffer)
    {
        for (int i = 0; i<stringBuffer.length();i++)
        {
            commandTemp.append(stringBuffer.charAt(i));
            if (stringBuffer.charAt(i)=='\n')
            {
                commandQueue.offer(commandTemp);
                commandTemp = new StringBuilder();
            }
        }
        //stringBuffer.setLength(0);

        return commandQueue;
        /*while (!commandQueue.isEmpty())
        {
            String curCommand = commandQueue.poll().toString();
            //procesarComando
            stringBuffer.append(curCommand);
        }*/
    }
}
