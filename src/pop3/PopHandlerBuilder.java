package pop3;

import proxy.handler.HandlerBuilder;

/**
 * Created by root on 5/27/16.
 */
public class PopHandlerBuilder implements HandlerBuilder {
    @Override
    public PopHandler build() {
        return new PopHandler();
    }
}
