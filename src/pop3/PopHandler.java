package pop3;

import proxy.handler.ConcurrentProxyHandler;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by root on 5/27/16.
 */
public class PopHandler extends ConcurrentProxyHandler {

    public enum TYPE {SAME, MODIFY, ML_SAME, UNKOWN}
    private TYPE type;
    private Queue<TYPE> typeQueue;

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
        return true;
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
        this.halfEnd = this.getStringBuffer().substring(length-2, length).equals("\r\n");
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
