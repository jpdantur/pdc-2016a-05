package administrator;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by matias on 6/5/16.
 */

public class AdministratorProtocol implements TCPProtocol {
    private int bufSize; // Size of I/O buffer
    private ByteBuffer readBuffer;
    private StringBuffer stringBuffer;
    private XMLAdmin admin;

    private Configuration config;
    private Statistics stat;

    private static String OKresp = "+OK ";
    private static String ERRresp = "-ERR";
    private static String login = "+Logged in\r\n";
    private boolean showWellcomeMsg = true;
    private boolean isLogin = false;
    private String adminPass;
    private String adminUser;

    private final static Logger logger = Logger.getLogger(AdministratorProtocol.class);

    public AdministratorProtocol(int bufSize, XMLAdmin admin) {
        this.bufSize = bufSize;
        this.readBuffer = ByteBuffer.allocate(this.bufSize);
        this.stringBuffer = new StringBuffer();
        this.admin = admin;
        this.config = Configuration.getInstance();
        this.stat = Statistics.getInstance();
    }

    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
        clntChan.configureBlocking(false);
        stringBuffer.append(config.getConfiguration().getWellcome() + "\n");
        clntChan.register(key.selector(), SelectionKey.OP_WRITE, ByteBuffer.allocate(bufSize));
    }

    public void handleRead(SelectionKey key) throws IOException {
        // Client socket channel has pending data
        SocketChannel clntChan = (SocketChannel) key.channel();
        readBuffer = (ByteBuffer) key.attachment();
        long bytesRead = clntChan.read(readBuffer);

        boolean end = false;
        char data;

        readBuffer.flip();

        while(readBuffer.hasRemaining()){
            data = (char) readBuffer.get();
            if( data == '\n')
                end = true;
            stringBuffer.append(data);
        }

        readBuffer.clear();

        if (bytesRead == -1) { // Did the other end close?
            clntChan.close();
        } else if (bytesRead > 0) {
            // Indicate via key that reading/writing are both of interest now.
            if(end)
                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            else
                key.interestOps(SelectionKey.OP_READ);
        }
    }

    public void handleWrite(SelectionKey key) throws IOException {
        String str = stringBuffer.toString();
        String sendResp;
        //limpio el stringbuffer
        stringBuffer.setLength(0);

        if(str.contains("\n")){
            sendResp = getResponde(str);

            ByteBuffer buf = (ByteBuffer) key.attachment();
            buf.flip(); // Prepare buffer for writing
            SocketChannel clntChan = (SocketChannel) key.channel();
            buf = ByteBuffer.wrap(sendResp.getBytes());
            clntChan.write(buf);

            if(sendResp.contains(config.getConfiguration().getGbyeMsg())) {
                this.showWellcomeMsg = true;
                logger.info("Close administrator.");
                clntChan.close();
                return;
            }

            if (!buf.hasRemaining()) {
                key.interestOps(SelectionKey.OP_READ);
            }
            buf.compact(); // Make room for more data to be read in
        } else {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void setUser(String user) {
        this.adminUser = user;
    }

    private String getUser() {
        return this.adminUser;
    }

    private void setPass(String pass) {
        this.adminPass = pass;
    }

    private String getPass() {
        return this.adminPass;
    }

    private String getResponde(String input) {
        if(showWellcomeMsg) {
            showWellcomeMsg = false;
            return OKresp + input;
        }

        if(input.toLowerCase().equals("quit\r\n") || input.toLowerCase().equals("quit\n"))
            return getQuit();

        if(input.toLowerCase().equals("capa\r\n") || input.toLowerCase().equals("capa\n"))
            return OKresp+getCapa();

        if(input.toLowerCase().equals("stat\r\n") || input.toLowerCase().equals("stat\n"))
            if(isLogin){
                return OKresp+getStat();
            }
            else
                return ERRresp+" Login first\r\n";

        String pattern = "([A-Za-z]+?)\\s";
        Pattern r = Pattern.compile(pattern);
        String command;
        Matcher m = r.matcher(input);
        input = input.replace("\r\n","").replace("\n","");

        if (m.find()) {
            command = m.group().replace(" ", "");

            switch (command.toLowerCase()) {
                case "user":
                    setUser(input.replace(command+" ", ""));
                    break;

                case "pass":
                    setPass(input.replace(command+" ", ""));
                    this.isLogin = checkUserPassAdmin();
                    if(isLogin) {
                        logger.info("Administrator logged in.");
                        return login;
                    } else {
                        deleteUserPass();
                        return ERRresp+" USER or PASS incorrect.\r\n";
                    }
                case "leet":
                    if(this.isLogin) {
                        int exit = setL33t(input.replace(command+" ", ""));
                        if(exit == 0) {
                            return OKresp + "\r\n";
                        } else {
                            return ERRresp+" Input not valid.\r\n";
                        }
                    }else {
                        return ERRresp+" Login first.\r\n";
                    }
                case "rotation":
                    if(this.isLogin) {
                        int exit = setRotation(input.replace(command+" ", ""));
                        if(exit == 0) {
                            return OKresp + "\r\n";
                        } else {
                            return ERRresp+" Input not valid.\r\n";
                        }
                    }else {
                        return ERRresp+" Login first.\r\n";
                    }
                default:
                    return ERRresp+" Command not valid.\r\n";
            }
            return OKresp +"\n";
        } else {
            return ERRresp+" Command not found.\r\n";
        }
    }

    private boolean checkUserPassAdmin(){
        if(admin.getUser().equals(getUser()) &&
                admin.getPass().equals(getPass()))
            return true;
        return false;
    }

    private void deleteUserPass() {
        setUser("");
        setPass("");
    }

    private int setL33t(String input) {
        String value = input.toLowerCase();
        if(value.equals("yes")){
            this.config.getConfiguration().setLeet(true);
            return 0;
        } else if(value.equals("no")){
            this.config.getConfiguration().setLeet(false);
            return 0;
        }
        return 1;
    }

    private int setRotation(String input) {
        String value = input.toLowerCase();
        if(value.equals("yes")){
            this.config.getConfiguration().setRotation(true);
            return 0;
        } else if(value.equals("no")){
            this.config.getConfiguration().setRotation(false);
            return 0;
        }
        return 1;
    }

    private String getStat() {
        return "BUFFER SIZE: " + this.config.getConfiguration().getBufferSize()+"bytes\n" +
                "LEET: " + (this.config.getConfiguration().getLeet() ? "yes" : "no" ) + "\n" +
                "ROTATION: " + (this.config.getConfiguration().getRotation() ? "yes" : "no" ) + "\n" +
                "ADMINISTRATOR PORT: " + this.config.getConfiguration().getAdmin().get(0).getPort() + "\n" +
                "PROXY PORT: " + this.config.getConfiguration().getServerPort() + "\n" +
                "BYTES TRANSFERRED: " + humanReadableByteCount(stat.getBytesTransferred(), false) + "\n" +
                "ACCESS: " + stat.getAccesses() +
                "\r\n.\r\n";
    }

    private String getQuit() {
        this.isLogin = false;
        return OKresp + config.getConfiguration().getGbyeMsg() + "\r\n";
    }

    private String getCapa() {
        if(!isLogin)
            return "COMMANDS:\n" +
                    "USER\n" +
                    "PASS\r\n.\r\n";
        else
            return "COMMANDS:\n" +
                    "LEET\n" +
                    "ROTATION\n" +
                    "STAT\r\n.\r\n";
    }

//    private String Histogram() {
//        return "algun histograma de todos los tipos de errores";
//    }

    //convtierte el tamano de los bytes en un formato leible
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}