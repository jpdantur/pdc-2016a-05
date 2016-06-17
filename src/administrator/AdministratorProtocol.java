package administrator;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DecimalFormat;
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

    private List<XMLMultiplex> multiplexList = new ArrayList<XMLMultiplex>();

    private final static Logger logger = Logger.getLogger(AdministratorProtocol.class);

    public AdministratorProtocol(int bufSize, XMLAdmin admin) {
        this.bufSize = bufSize;
        this.readBuffer = ByteBuffer.allocate(this.bufSize);
        this.stringBuffer = new StringBuffer();
        this.admin = admin;
        this.config = Configuration.getInstance();
        this.stat = Statistics.getInstance();
    }

    public void handleAccept(SelectionKey key, Administrator ad) throws IOException {
        SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
        clntChan.configureBlocking(false);
        stringBuffer.append(config.getConfiguration().getWellcome() + "\n");
        clntChan.register(key.selector(), SelectionKey.OP_WRITE, new AdminHandler(bufSize, key));
    }

    public void handleRead(SelectionKey key, Administrator ad) throws IOException {
        // Client socket channel has pending data
        SocketChannel clntChan = (SocketChannel) key.channel();

        readBuffer = ((AdminHandler) key.attachment()).getBuffer();
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

    public void handleWrite(SelectionKey key, Administrator ad) throws IOException {
        AdminHandler ah = ((AdminHandler) key.attachment());
        String str = stringBuffer.toString();
        String sendResp;
        //limpio el stringbuffer
        stringBuffer.setLength(0);

        if(str.contains("\n")){

            if(ah.getRcvdWellcome()) {
                ah.setRcvdWellcome(false);
                sendResp = getwellCome(str);
            } else {
                sendResp = getResponde(str, ah);
            }

            ByteBuffer buf = ((AdminHandler) key.attachment()).getBuffer();
            buf.flip(); // Prepare buffer for writing
            SocketChannel clntChan = (SocketChannel) key.channel();
            buf = ByteBuffer.wrap(sendResp.getBytes());
            clntChan.write(buf);

            if(sendResp.contains(config.getConfiguration().getGbyeMsg())) {
                this.showWellcomeMsg = true;
                ad.setAdminHandler(null);
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

    private String getResponde(String input, AdminHandler ah) {
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
                    ah.setUser(input.replace(command+" ", ""));
                    break;

                case "pass":
                    ah.setPass(input.replace(command+" ", ""));
                    this.isLogin = checkUserPassAdmin(ah);
                    if(isLogin) {
                        logger.info("Administrator logged in.");
                        return login;
                    } else {
                        deleteUserPass(ah);
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
                case "add":
                    if(this.isLogin) {
                        int exit = AddMultiplexUser(input);
                        if(exit == 0) {
                            return OKresp + "\r\n";
                        } else if(exit == 1) {
                            return ERRresp+" Not enough params.";
                        }
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

    private String getwellCome(String input) {
            return OKresp + input;
    }

    private boolean checkUserPassAdmin(AdminHandler ah){
        if(admin.getUser().equals(ah.getUser()) &&
                admin.getPass().equals(ah.getPass())){
            logger.info("Admin " + ah.getUser() + " is login correctly.");
            return true;
        }
        return false;
    }

    private void deleteUserPass(AdminHandler ah) {
        ah.setUser("");
        ah.setPass("");
    }

    private int setL33t(String input) {
        String value = input.toLowerCase();
        if(value.equals("yes")){
            logger.info("LEET activated.");
            this.config.getConfiguration().setLeet(true);
            return 0;
        } else if(value.equals("no")){
            logger.info("LEET deactivated.");
            this.config.getConfiguration().setLeet(false);
            return 0;
        }
        return 1;
    }

    private int setRotation(String input) {
        String value = input.toLowerCase();
        if(value.equals("yes")){
            logger.info("ROTATION activated.");
            this.config.getConfiguration().setRotation(true);
            return 0;
        } else if(value.equals("no")){
            logger.info("ROTATION deactivated.");
            this.config.getConfiguration().setRotation(false);
            return 0;
        }
        return 1;
    }

    private String getStat() {
        return "\nBUFFER SIZE: " + this.config.getConfiguration().getBufferSize()+"bytes\r\n" +
                "LEET: " + (this.config.getConfiguration().getLeet() ? "yes" : "no" ) + "\r\n" +
                "ROTATION: " + (this.config.getConfiguration().getRotation() ? "yes" : "no" ) + "\r\n" +
                "ADMINISTRATOR PORT: " + this.config.getConfiguration().getAdmin().get(0).getPort() + "\r\n" +
                "PROXY PORT: " + this.config.getConfiguration().getServerPort() + "\r\n" +
                "BYTES TRANSFERRED: " + readableFileSize(stat.getBytesTransferred()) + "\r\n" +
                "ACCESS: " + stat.getAccesses() + "\r\n" +
                "USERS ADDED:\r\n" + multiplexListToString() +
                "\r\n.\r\n";
    }

    private String getQuit() {
        this.isLogin = false;
        return OKresp + config.getConfiguration().getGbyeMsg() + "\r\n";
    }

    private String getCapa() {
        if(!isLogin)
            return "COMMANDS:\r\n" +
                    "USER\r\n.\r\n";
        else
            return "COMMANDS:\r\n" +
                    "LEET\r\n" +
                    "ROTATION\r\n" +
                    "STAT\r\n.\r\n";
    }

//    private String Histogram() {
//        return "algun histograma de todos los tipos de errores";
//    }

    //convtierte el tamano de los bytes en un formato leible
    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    //Agrega un nuevo usuario. Este usuario se almacena en memoria.
    public int AddMultiplexUser(String input) {
        String pattern = "[A-Z|a-z|0-9]+(\\s|\\t)+[A-Z|a-z|0-9|\\.]+(\\s|\\t)+\\d+";
        Pattern r = Pattern.compile(pattern);
        String command = "add ";
        input = input.replace("\r\n", "").replace("\n", "").replace(command,"");
        Matcher m = r.matcher(input);

        String user;
        String host;
        String port;

        if(m.find()) {
            String[] values = input.replaceAll("(\\s|\\t)+"," ").split(" ");
            user = values[0];
            host = values[1];
            port = values[2];
            XMLMultiplex mx = new XMLMultiplex(user,host,port);
            multiplexList.add(mx);
            config.insertUser(mx);
            logger.info("[USER: "+ user +" HOST:"+ host +" PORT: "+ port + "] added.");
            return 0;
        } else {
          return 1;
        }
    }

    public List<XMLMultiplex> getMultiplexList() {
        return multiplexList;
    }

    public String multiplexListToString() {
        String out = "";
        int i = 0;

        if(multiplexList.size() == 0)
            out = "No users added";
        else {
            for(; i < multiplexList.size() - 1; i++) {
                out += multiplexList.get(i).getUser()+"\r\n";
            }

            out += multiplexList.get(i).getUser();
        }

        return out;
    }
}