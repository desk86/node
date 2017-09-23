package com.sufnom;

import com.sufnom.sys.Config;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Gate {
    public static final int PORT =
            Integer.parseInt(Config.getSession().getValue(Config.KEY_NODE_PORT));
    public static final boolean debug =
            Config.getSession().getValue(Config.KEY_DEBUG).toLowerCase()
            .equals(String.valueOf(true));
    private static final String CONTEXT_AUTH = "/auth";
    private static final String CONTEXT_NODE = "/node";
    private static final String CONTEXT_SYNAPSE = "/synapse";

    private static final String REQUEST = "request";
    private static final String REQUEST_LIST = "list";
    private static final String REQUEST_INSERT = "insert";

    public static void main(String[] args) {
	    Gate gate = new Gate();
	    try { gate.open(); }
        catch (Exception e){e.printStackTrace();}
    }

    @SuppressWarnings("unused")
    private void open() throws Exception{
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        HttpContext context = server.createContext("/", new DefaultHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Listen to : " + PORT);
    }

    private static class DefaultHandler implements HttpHandler{
        public void handle(HttpExchange t){

        }

        private void sendResponse(HttpExchange t, int status, String response){
            try {
                if (response == null){
                    JSONObject ob = new JSONObject();
                    ob.put("Status", "Server OK");
                    response = ob.toString();
                }
                byte[] rawResponse = response.getBytes();
                System.out.println("Response : " + response);
                t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                t.getResponseHeaders().set("Content-Type", "text/plain");
                t.sendResponseHeaders(status, rawResponse.length);
                OutputStream os = t.getResponseBody();
                os.write(rawResponse);
                os.close();
            }
            catch (Exception e){e.printStackTrace();}
        }
    }
}
