package br.ufc.great.syssu.ubicentre.controllers;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;

import br.ufc.great.syssu.jsonrpc2.JSONRPC2Message;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2MethodNotFoundException;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2Response;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2Subscription;
import br.ufc.great.syssu.servicemanagement.IService;
import br.ufc.great.syssu.servicemanagement.InvalidParamsException;
import br.ufc.great.syssu.servicemanagement.OperationException;
import br.ufc.great.syssu.servicemanagement.ServiceManager;

public class SubscriptionController implements IController {

    private static SubscriptionController instance;
    private InetAddress address;

    private SubscriptionController() {
    }

    public static SubscriptionController getInstance() {
        if (instance == null) {
            instance = new SubscriptionController();
        }
        return instance;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    @Override
    public String process(JSONRPC2Message message)
            throws JSONRPC2MethodNotFoundException, InvalidParamsException, OperationException {

    	String response = null;
    	
        if(address == null) {
            throw new IllegalStateException("A address is necessary");
        }

        JSONRPC2Subscription subscription = (JSONRPC2Subscription) message;

        IService service = ServiceManager.getInstance().getEventService(subscription.getEvent());

        if (service == null) {
            throw new JSONRPC2MethodNotFoundException("Invalid method: " + subscription.getEvent());
        }

        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("address", address);
        params.put("port", subscription.getPort());
        params.put("params",subscription.getParams());
        
        Object result = service.doService(params);
        
        JSONRPC2Response r = new JSONRPC2Response(result, subscription.getID());
        response = r.toJSON().toJSONString();

        return response;
    }
}
