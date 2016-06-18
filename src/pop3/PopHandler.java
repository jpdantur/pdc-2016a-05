package pop3;

import administrator.Configuration;
import proxy.handler.SimpleProxyHandler;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.*;

/**
 * Created by root on 5/27/16.
 */
public class PopHandler extends SimpleProxyHandler {

    public enum TYPE {SAME, MODIFY, ML_SAME, UNKOWN}
    private TYPE type;
    private Queue<TYPE> typeQueue;
    private Pattern userPattern = Pattern.compile("[U|u][S|s][E|e][R|r] (.+)\r?");
    private Pattern capaPattern = Pattern.compile("CAPA([ \t]*)\r?");
    private Pattern passPattern = Pattern.compile("[P|p][A|a][S|s][S|s] (.+)\r?");
    private Pattern quitPattern = Pattern.compile("[Q|q][U|u][I|i][T|t]\r?");
    private Pattern listPattern = Pattern.compile("[L|l][I|i][S|s][T|t].*\r?");
    private Pattern retrPattern = Pattern.compile("[R|r][E|e][T|t][R|r].*\r?");
    private Pattern topPattern = Pattern.compile("[T|t][O|o][P|p].*\r?");
    private Pattern uidlPattern = Pattern.compile("[U|u][I|i][D|d][L|l].*\r?");

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

        TYPE firstType = null;

        StringBuffer stringBuffer = this.getStringBuffer();

        int index = stringBuffer.indexOf("\n");
        if(index != -1){

            String line;
            int lineStart = 0;

            for(int i = 0; i < stringBuffer.length(); i++){
                if(stringBuffer.charAt(i) == '\n'){
                    line = stringBuffer.substring(lineStart, i);
                    lineStart = i+1;
                    boolean ans = analizeCommand(line);
                    if(firstType == null){
                        firstType = this.type;
                    }
                }
            }

            this.setFirstLine(stringBuffer.substring(lineStart, stringBuffer.length()));
            stringBuffer.setLength(lineStart);


            if(this.getOtherKey() == null || (((PopHandler)this.getOtherKey().attachment()).getOtherKey() == null && !this.getFinishConnect())) {
                getStringBuffer().setLength(0);
            }

        }else{
            if(isClient() && this.getStringBuffer().length() > MAX_COMMAND_LENGTH)
                return false;
            return true;
        }

        this.type = firstType;

        if(isClient()){
            return false;
        }

        return type != TYPE.SAME && type != TYPE.ML_SAME;

    }

    private boolean analizeCommand(String s){
        if(isClient()){
            identifyType(s);
            return false;
        }
        else if(((PopHandler)this.getOtherKey().attachment()).getOtherKey() == null){
            serverResponses++;
            if(s.charAt(0)=='-'){
                this.setWrongPass(true);
                ByteBuffer bb = ByteBuffer.wrap((s + "\n").getBytes());
                bb.compact();
                ((PopHandler)this.getOtherHandler()).setWriteBuffer(bb);
            }
            else if(serverResponses == 1 && s.charAt(0) == '+'){
                String user = ((PopHandler)this.getOtherHandler()).getUser();
                ByteBuffer bbUser = ByteBuffer.wrap(("user " + user + "\r\n").getBytes());
                bbUser.compact();
                this.setWriteBuffer(bbUser);
            }
            else if(serverResponses == 2 && s.charAt(0) == '+'){
                String pass = ((PopHandler)this.getOtherHandler()).getPass();
                ByteBuffer bbPass = ByteBuffer.wrap(("pass " + pass + "\r\n").getBytes());
                bbPass.compact();
                this.setWriteBuffer(bbPass);
            }
            else if(s.charAt(0)=='+' && serverResponses == 3){
                this.setFinishConnect(true);
            }
            this.type = TYPE.SAME;
        }

        if(this.type == TYPE.UNKOWN){
            TYPE aux = typeQueue.poll();
            if(aux == null) {
                this.type = TYPE.SAME;
            }
            else {
                this.type = aux;
                if (s.charAt(0) == '-') {
                    this.type = TYPE.SAME;
                }
            }
        }
        return type != TYPE.SAME && type != TYPE.ML_SAME;
    }




    public boolean bufferDone(){
        if(this.getStringBuffer().indexOf("\n")==-1) {
            if (this.getStringBuffer().length()<=1000)
                return false;
            this.type = TYPE.ML_SAME;
            return true;
        }
        transformData();
        return this.getStringBuffer().length()!=0;
    }

    public void resetHandler(){
        if(isClient()) return;
        if(this.type == TYPE.SAME){
            this.type = TYPE.UNKOWN;
            return;
        }
        int length = this.getStringBuffer().length();

        if(this.getStringBuffer().indexOf("\r\n.\r\n")!=-1){
            this.popParser.resetFlags();
            this.type = TYPE.UNKOWN;
        }else if(halfEnd && length < 4 && this.getStringBuffer().toString().equals(".\r\n")){
            this.popParser.resetFlags();
            popParser.setSubjectEnabled(this.config.getConfiguration().getLeet());
            popParser.setImageEnabled(this.config.getConfiguration().getRotation());
            this.type = TYPE.UNKOWN;
        }
        this.halfEnd = length >=2 && this.getStringBuffer().substring(length-2, length).equals("\r\n");
    }

    private void identifyType(String s) {
        String originalBuffer = s.toString();
        String buffer = originalBuffer.toUpperCase();
        Matcher userMatcher = userPattern.matcher(originalBuffer);
        Matcher capaMatcher = capaPattern.matcher(buffer);
        Matcher passMatcher = passPattern.matcher(originalBuffer);
        Matcher quitMatcher = quitPattern.matcher(originalBuffer);
        Matcher listMatcher = listPattern.matcher(originalBuffer);
        Matcher retrMatcher = retrPattern.matcher(originalBuffer);
        Matcher topMatcher = topPattern.matcher(originalBuffer);
        Matcher uidlMatcher = uidlPattern.matcher(originalBuffer);


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

            }
            else if(passMatcher.matches()){
                attempts = 0;
                if(this.getUser() != null){
                    if (passMatcher.groupCount() > 0) {
                        this.setPass(passMatcher.group(1));
                    } else {
                        this.setPass("");
                    }
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

            this.type = TYPE.SAME;
            return;
        }
        if(retrMatcher.matches() || topMatcher.matches()){
            ((PopHandler)this.getOtherHandler()).setModify();
        }else if(listMatcher.matches() || uidlMatcher.matches() || capaMatcher.matches()){
            ((PopHandler)this.getOtherHandler()).setMlSame();
        }else{
            ((PopHandler)this.getOtherHandler()).setSame();
        }

        this.type = TYPE.SAME;

    }




    public void setSame(){
        this.typeQueue.offer(TYPE.SAME);
    }

    public void setModify(){
        this.typeQueue.offer(TYPE.MODIFY);

        popParser.setSubjectEnabled(this.config.getConfiguration().getLeet());
        popParser.setImageEnabled(this.config.getConfiguration().getRotation());
    }

    public void setMlSame(){
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
