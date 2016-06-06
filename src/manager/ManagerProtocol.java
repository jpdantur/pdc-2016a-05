package manager;

import proxy.utils.AdminProperties;
import proxy.utils.Configuration;

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

public class ManagerProtocol implements TCPProtocol {
    private int bufSize; // Size of I/O buffer
    private ByteBuffer readBuffer;
    private StringBuffer stringBuffer;
    private AdminProperties admin;
    private Configuration config;

    private static String OKresp = "+OK\n";
    private static String ERRresp = "-ERR";
    private static String login = "+Login in\n";
    private boolean isLogin = false;
    private boolean leet = false;
    private String adminPass;
    private String adminUser;

    public ManagerProtocol(int bufSize, AdminProperties admin) {
        this.bufSize = bufSize;
        this.readBuffer = ByteBuffer.allocate(this.bufSize);
        this.stringBuffer = new StringBuffer();
        this.admin = admin;
        this.config = Configuration.getInstance();
    }

    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
        clntChan.configureBlocking(false); // Must be nonblocking to register
        // Register the selector with new channel for read and attach byte
        // buffer
        clntChan.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufSize));
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
            if (!buf.hasRemaining()) { // Buffer completely written?
                // Nothing left, so no longer interested in writes
                key.interestOps(SelectionKey.OP_READ);
            }
            buf.compact(); // Make room for more data to be read in
        } else {
            key.interestOps(SelectionKey.OP_READ);
        }
        /*
         * Channel is available for writing, and key is valid (i.e., client
         * channel not closed).
         */
        // Retrieve data read earlier

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

    private String getHelp() {
        return "COMMANDS\n" +
                " USER\n" +
                " PASS\n" +
                " L33T\n" +
                " BUFFERSIZE\n" +
                " STAT\n";
    }

    private String getResponde(String input) {
        if(input.toLowerCase().equals("help\n"))
            return OKresp+getHelp();

        if(input.toLowerCase().equals("stat\n"))
            if(isLogin){
                // return stats
            }
            else
                return ERRresp+" Login first";

        String pattern = "([A-Za-z]+?)\\s";
        Pattern r = Pattern.compile(pattern);
        String command;
        // Now create matcher object.
        Matcher m = r.matcher(input);
        input = input.replace("\n","");
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
                        return login;
                    } else {
                        deleteUserPass();
                        return ERRresp+" USER or PASS incorrect.\n";
                    }
                case "leet":
                    if(this.isLogin) {
                        int exit = setL33t(input.replace(command+" ", ""));
                        if(exit == 0) {
                            return OKresp;
                        } else {
                            return ERRresp+" Input not valid.\n";
                        }
                    }else {
                        return ERRresp+" Login first.\n";
                    }
                default:
                    return ERRresp+" Command not valid.\n";
            }
            return OKresp;
        } else {
            return ERRresp+" Command not found.\n";
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
            this.leet = true;
            this.config.getProp().setLeet(this.leet);
            System.out.println(this.config.getProp().getLeet());
            return 0;
        } else if(value.equals("no")){
            this.leet = false;
            this.config.getProp().setLeet(this.leet);
            System.out.println(this.config.getProp().getLeet());
            return 0;
        }
        return 1;
    }
}