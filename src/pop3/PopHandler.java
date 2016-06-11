package pop3;

import proxy.handler.ConcurrentProxyHandler;

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
    private Pattern userPattern = Pattern.compile("USER ([^ \t]*)\r");

    private String user;

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
            if(this.type == TYPE.UNKOWN){
                TYPE aux = typeQueue.poll();
                if(aux == null) {
                    this.type = TYPE.SAME;
                }
                else {
                    this.type = aux;
                    if(this.getStringBuffer().charAt(0)=='-'){
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
        System.out.println("Aca empieza");
        transformData();
        System.out.println("Len: "+this.getStringBuffer().length());
        return this.getStringBuffer().length()!=0;
    }

    public void resetHandler(){
        System.out.println("TIPO ACTUAL " + this.type);
        if(isClient()) return;
        if(this.type == TYPE.SAME){
            this.type = TYPE.UNKOWN;
            return;
        }
        int length = this.getStringBuffer().length();

        if(this.getStringBuffer().indexOf("\r\n.\r\n")!=-1){
            System.out.println("SETEO UNKOWN - RESET HANDLER");
            this.type = TYPE.UNKOWN;
        }else if(halfEnd && length < 4 && this.getStringBuffer().toString().equals(".\r\n")){
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
        String buffer = this.getStringBuffer().substring(0, index);
        buffer = buffer.toUpperCase();
        Matcher userMatcher = userPattern.matcher(buffer);

        System.out.println("|"+buffer +"|");

        if(getOtherKey() == null) {
            if(userMatcher.matches()){
                attempts = 0;
                if (getOtherKey() == null) {
                    user = userMatcher.group(1);
                    System.out.println("el usuario es: |" + user + "|");

                }
            }else{
                attempts++;
                if(attempts == MAX_ATTEMPTS){
                    System.out.println("ahora hay que cerrar la conexion");
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
        System.out.println("---SAME---");
        this.typeQueue.offer(TYPE.SAME);
    }

    public void setModify(){
        System.out.println("---MODIFY---");
        this.typeQueue.offer(TYPE.MODIFY);
    }

    public void setMlSame(){
        System.out.println("---ML_SAME---");
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
