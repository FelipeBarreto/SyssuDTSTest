package br.ufc.great.syssu.ubicentre.controllers;

import br.ufc.great.syssu.jsonrpc2.JSONRPC2Message;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2MethodNotFoundException;
import br.ufc.great.syssu.servicemanagement.InvalidParamsException;
import br.ufc.great.syssu.servicemanagement.OperationException;

public interface IController {
    public String process(JSONRPC2Message message) 
            throws JSONRPC2MethodNotFoundException, InvalidParamsException, OperationException;
}
