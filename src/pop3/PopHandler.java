package pop3;

import proxy.handler.ConcurrentProxyHandler;

/**
 * Created by root on 5/27/16.
 */
public class PopHandler extends ConcurrentProxyHandler {

    public PopHandler() {
        super();
    }

    @Override
    public boolean analizeData() {
        return this.getStringBuffer().indexOf("\n")!=-1;
    }

    @Override
    public void transformData() {
        PopParser popParser = new PopParser();
        popParser.parseData(this.getStringBuffer());
    }
}
