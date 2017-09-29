package com.sufnom;

import com.sufnom.node.NodeTerminal;
import com.sufnom.node.ob.Node;
import com.sufnom.node.ob.Synapse;
import com.sufnom.sys.Config;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

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
            try {
                String path = t.getRequestURI().getPath();
                Map postMap = (Map)t.getAttribute("parameters");
                switch (path){
                    case CONTEXT_NODE:
                        handleNodeResponse(t, postMap);
                        break;
                    case CONTEXT_SYNAPSE:
                        handleSynapseResponse(t, postMap);
                        break;
                    case CONTEXT_AUTH:
                        handleAuthResponse(t, postMap);
                        break;
                    default: sendResponse(t, 200, null);
                }
            }
            catch (Exception e){
                e.printStackTrace();
                sendResponse(t, 200, "error");
            }
        }

        private void handleAuthResponse(HttpExchange t, Map postMap) throws Exception{
            try {
                String email = (String) postMap.get("email");
                String password = (String)postMap.get("password");
                sendResponse(t, 200, NodeTerminal.getSession().signIn(email, password));
            }
            catch (Exception e){
                e.printStackTrace();
                sendResponse(t,500, "error");
            }
        }

        private void handleNodeResponse(HttpExchange t, Map postMap) throws Exception{
            String request = (String)postMap.get(REQUEST);
            if (request == null){
                sendResponse(t, 200, null);
                return;
            }
            getSessionAdmin(postMap);
            switch (request){
                case REQUEST_LIST:
                    sendResponse(t, 200,
                            NodeTerminal.getSession().getFactory()
                                    .getNodeList(Long.parseLong((String)postMap
                                            .get("parent"))).toString());
                    break;
                case REQUEST_INSERT:
                    JSONObject ob = new JSONObject((String)postMap.get("node"));
                    JSONObject content = ob.getJSONObject("content");
                    Node node = NodeTerminal.getSession().getFactory()
                            .insertNode(
                                    getSessionAdmin(postMap),
                                    Long.parseLong(
                                            (String)postMap.get("parent")),
                                    content.toString());
                    if (node != null)
                        sendResponse(t, 200, node.toString());
                    else sendResponse(t, 500, "");
                    break;
            }
        }
        private void handleSynapseResponse(HttpExchange t, Map postMap) throws Exception{
            String request = (String)postMap.get(REQUEST);
            getSessionAdmin(postMap);
            switch (request){
                case REQUEST_LIST:
                    sendResponse(t, 200,
                            NodeTerminal.getSession().getFactory()
                                    .getSynapseList(Long.parseLong((String)postMap
                                            .get("node"))).toString());
                    break;
                case REQUEST_INSERT:
                    JSONObject ob = new JSONObject((String)postMap.get("synapse"));
                    Synapse synapse = NodeTerminal.getSession().getFactory()
                            .insertSynapse(
                                    getSessionAdmin(postMap),
                                    Long.parseLong((String)postMap.get("node")),
                                    ob.getJSONObject("content").toString());
                    if (synapse != null)
                        sendResponse(t, 200, synapse.toString());
                    else sendResponse(t, 500, "");
                    break;
            }
        }

        private long getSessionAdmin(Map postMap) throws Exception{
            return NodeTerminal.getSession().getAdmin((String)postMap.get("session"));
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
