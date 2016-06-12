package pop3;

import administrator.Configuration;
import proxy.handler.ConcurrentProxyHandler;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.*;

/**
 * Created by root on 5/27/16.
 */
public class PopHandler extends ConcurrentProxyHandler {

    public enum TYPE {SAME, MODIFY, ML_SAME, UNKOWN}
    private TYPE type;
    private Queue<TYPE> typeQueue;
    private Pattern userPattern = Pattern.compile("[U|u][S|s][E|e][R|r] ?(.*)\r?");
    private Pattern capaPattern = Pattern.compile("CAPA*\r?");
    private Pattern passPattern = Pattern.compile("[P|p][A|a][S|s][S|s] ?(.*)\r?");
    private Pattern quitPattern = Pattern.compile("[Q|q][U|u][I|i][T|t] ?(.*)\r?");
    private static Configuration config = Configuration.getInstance();

    private int serverResponses = 0;

    private int attempts = 0;
    private final int MAX_ATTEMPTS = 10;

    private static final int MAX_COMMAND_LENGTH = 255;
    private boolean halfEnd;
    private PopParser popParser = new PopParser();

    public PopHandler() {
        super();
        this.type = TYPE.UNKOWN;
        this.typeQueue = new LinkedList<>();
        this.halfEnd = false;
    }

    @Override
    public boolean analizeData() {

        int index = this.getStringBuffer().indexOf("\n");
        if(index != -1){
            if(isClient()){
                identifyType(index);
                return false;
            }
            else if(((PopHandler)this.getOtherKey().attachment()).getOtherKey() == null){
                serverResponses++;
                if(this.getStringBuffer().charAt(0)=='-'){
                    this.setWrongPass(true);
                    ByteBuffer bb = ByteBuffer.wrap((this.getStringBuffer().substring(0, index) + "\n").getBytes());
                    bb.compact();
                    ((PopHandler)this.getOtherHandler()).setWriteBuffer(bb);
                }
                else if(this.getStringBuffer().charAt(0)=='+' && serverResponses == 3){
                    this.setFinishConnect(true);
                }
            }

            if(this.type == TYPE.UNKOWN){
                TYPE aux = typeQueue.poll();
                if(aux == null) {
                    this.type = TYPE.SAME;
                }
                else {
                    this.type = aux;
                    if (this.getStringBuffer().charAt(0) == '-') {
                        this.type = TYPE.SAME;
                    }
                }
            }
        }else{
            if(isClient() && this.getStringBuffer().length() > MAX_COMMAND_LENGTH)
                return false;
            return true;
        }

        return type != TYPE.SAME;

    }

    public boolean transformBufferDone(){
        if(this.getStringBuffer().indexOf("\n")==-1)
            return false;

        //Si ya tengo todo para modificar, lo hago, y return true
        //Si no, return false
        //this.type = TYPE.UNKOWN;
        //System.out.println("Aca empieza");
        transformData();
        //System.out.println("Len: "+this.getStringBuffer().length());
        return this.getStringBuffer().length()!=0;
    }

    public void resetHandler(){
        //System.out.println("TIPO ACTUAL " + this.type);
        if(isClient()) return;
        if(this.type == TYPE.SAME){
            this.type = TYPE.UNKOWN;
            return;
        }
        int length = this.getStringBuffer().length();

        if(this.getStringBuffer().indexOf("\r\n.\r\n")!=-1){
            this.popParser.resetFlags();
            System.out.println("SETEO UNKOWN - RESET HANDLER");
            this.type = TYPE.UNKOWN;
        }else if(halfEnd && length < 4 && this.getStringBuffer().toString().equals(".\r\n")){
            this.popParser.resetFlags();
            popParser.setSubjectEnabled(config.getConfiguration().getLeet());
            popParser.setImageEnabled(config.getConfiguration().getRotation());
            System.out.println("SETEO UNKOWN - RESET HANDLER");
            this.type = TYPE.UNKOWN;
        }
        this.halfEnd = length >=2 && this.getStringBuffer().substring(length-2, length).equals("\r\n");
        /*
        System.out.println("*** " + this.getStringBuffer().substring(length-2, length));
        System.out.println("++++++++++++++++++");
        System.out.println(this.getStringBuffer().toString());
        System.out.println("++++++++++++++++++");
        if(this.halfEnd){

            System.out.println("ES VERDADEROOOO");
        }*/

    }

    private void identifyType(int index) {
        String originalBuffer = this.getStringBuffer().substring(0, index);
        String buffer = originalBuffer.toUpperCase();
        Matcher userMatcher = userPattern.matcher(originalBuffer);
        Matcher capaMatcher = capaPattern.matcher(buffer);
        Matcher passMatcher = passPattern.matcher(originalBuffer);
        Matcher quitMatcher = quitPattern.matcher(originalBuffer);

        System.out.println("|"+buffer +"|");

        if(getOtherKey() == null) {
            if(userMatcher.matches()){
                attempts = 0;
                if(userMatcher.groupCount() > 0) {
                    this.setUser(userMatcher.group(1));
                }
                else{
                    this.setUser("");
                }
                ByteBuffer bb = ByteBuffer.wrap(("+OK\r\n").getBytes());
                bb.compact();
                this.setWriteBuffer(bb);

                System.out.println("el usuario es: |" + this.getUser() + "|");

                System.out.println("----------------  " + this.getUser() + "  --------------");

            }
            else if(passMatcher.matches()){
                attempts = 0;
                if(this.getUser() != null){
                    if (passMatcher.groupCount() > 0) {
                        this.setPass(passMatcher.group(1));
                    } else {
                        this.setPass("");
                    }
                    System.out.println("la contraseña es: |" + this.getPass() + "|");
                    this.setReadyToConnect(true);
                }else {
                    ByteBuffer bb = ByteBuffer.wrap(("-ERR No username given.\r\n").getBytes());
                    bb.compact();
                    this.setWriteBuffer(bb);
                }
            }
            else if(capaMatcher.matches()){
                attempts = 0;
                ByteBuffer bb = ByteBuffer.wrap(("+OK\r\nCAPA\r\nUSER\r\n.\r\n").getBytes());
                bb.compact();
                this.setWriteBuffer(bb);
            }
            else if(quitMatcher.matches()) {
                attempts = 0;
                ByteBuffer bb = ByteBuffer.wrap(("+OK Logging out.\r\n").getBytes());
                bb.compact();
                this.setWriteBuffer(bb);
                this.setToClose(true);
            }else{
                attempts++;
                if(attempts == MAX_ATTEMPTS){
                    ByteBuffer bb = ByteBuffer.wrap("-ERR Too many unknown commands - Closing Connection.\r\n".getBytes());
                    bb.compact();
                    this.setWriteBuffer(bb);
                    this.setToClose(true);
                }
                else{
                    ByteBuffer bb = ByteBuffer.wrap(("-ERR Unknown command: " + buffer + "\r\n").getBytes());
                    bb.compact();
                    this.setWriteBuffer(bb);
                }
            }
            getStringBuffer().setLength(0);

            this.type = TYPE.SAME;
            return;
        }
        if(buffer.contains("RETR") || buffer.contains("TOP")){
            ((PopHandler)this.getOtherHandler()).setModify();
        }else if(buffer.contains("LIST")){
            ((PopHandler)this.getOtherHandler()).setMlSame();
        }else{
            ((PopHandler)this.getOtherHandler()).setSame();
        }

        this.type = TYPE.SAME;

    }




    public void setSame(){
        //System.out.println("---SAME---");
        this.typeQueue.offer(TYPE.SAME);
    }

    public void setModify(){
        //System.out.println("---MODIFY---");
        this.typeQueue.offer(TYPE.MODIFY);

        popParser.setSubjectEnabled(config.getConfiguration().getLeet());
        popParser.setImageEnabled(config.getConfiguration().getRotation());
    }

    public void setMlSame(){
        //System.out.println("---ML_SAME---");
        this.typeQueue.offer(TYPE.ML_SAME);
    }

    public void setUnkown(){
        this.type = TYPE.UNKOWN;
    }

    @Override
    public void transformData() {
        popParser.parseData(this.getStringBuffer());
    }
}
