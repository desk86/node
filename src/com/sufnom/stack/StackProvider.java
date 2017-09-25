package com.sufnom.stack;

import com.sufnom.sys.Config;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;

public class StackProvider {
    // DO NOT CHANGE THIS VALUE, KEEP IT TO 50 ALWAYS
    // This value relates the max size of the key, that ZedIndexer can hold
    // If you need to change this value, you must recompile stack giving desired max length
    // See ZedIndexer docs of ExtendedStackInterface
    public static final int MAX_INDEX_LENGTH = 50;
    public static final int STACK_HEADER_LENGTH = 64;
    public static final int STACK_NAMESPACE_LENGTH = 54;
    public static final int STACK_DEFINITION_LENGTH = 4;
    public static final int STACK_COMMAND_LENGTH = 6;

    public static final String STACK_URL =
            Config.getSession().getValue(Config.KEY_STACK_URL);

    public static final String NAMESPACE_NODE = "node";
    public static final String NAMESPACE_SYNAPSE = "synapse";
    public static final String NAMESPACE_EDITOR = "editor";
    public static final String NAMESPACE_PAGE = "page";

    public static final String STACK_TYPE_FIXED = "fsi";
    public static final String STACK_TYPE_DYNAMIC = "dsi";
    public static final String STACK_TYPE_EXTENDED = "esi";

    private static StackProvider session;

    public static StackProvider getSession() {
        if (session == null)
            initSession();
        return session;
    }

    private static void initSession(){
        session = new StackProvider();
        session.stackDefinitions = new HashMap<>();
        session.stackDefinitions.put(NAMESPACE_NODE, STACK_TYPE_FIXED);
        session.stackDefinitions.put(NAMESPACE_SYNAPSE, STACK_TYPE_DYNAMIC);
        session.stackDefinitions.put(NAMESPACE_EDITOR, STACK_TYPE_EXTENDED);
        session.stackDefinitions.put(NAMESPACE_PAGE, STACK_TYPE_FIXED);
    }

    private HashMap<String, String> stackDefinitions;

    public long insertFixed(String namespace, byte[] rawData) throws Exception{
        return getBlockId(forward(namespace, "insert", rawData));
    }

    public String insertExtended(String namespace, String key, byte[] rawData) throws Exception{
        if (key.length() > MAX_INDEX_LENGTH)
            return "Error : Key Size Exceeds";
        byte[] uidRaw = new byte[MAX_INDEX_LENGTH];
        byte[] dataToPass= new byte[rawData.length + uidRaw.length];
        byte[] keyRaw = key.getBytes();
        System.arraycopy(keyRaw, 0, uidRaw, 0, keyRaw.length);
        System.arraycopy(uidRaw, 0, dataToPass, 0, uidRaw.length);
        System.arraycopy(rawData, 0, dataToPass, uidRaw.length, rawData.length);
        return new String(forward(namespace, "insert", dataToPass));
    }

    public byte[] getFixed(String namespace, long blockId) throws Exception{
        return forward(namespace, "get", getRawBlockId(blockId));
    }

    public byte[] getExtended(String namespace, String key) throws Exception{
        if (key.getBytes().length > MAX_INDEX_LENGTH)
            throw new Exception("Key Length Overflow");
        return forward(namespace, "get", key.getBytes());
    }

    public String updateFixed(String namespace, long blockId, byte[] rawData) throws Exception{
        byte[] blockIdRaw = getRawBlockId(blockId);
        byte[] dataToPass= new byte[rawData.length + blockIdRaw.length];
        System.arraycopy(blockIdRaw, 0, dataToPass, 0, blockIdRaw.length);
        System.arraycopy(rawData, 0, dataToPass, blockIdRaw.length, rawData.length);
        return new String(forward(namespace, "update", dataToPass));
    }
    public String updateExtended(String namespace, String key, byte[] rawData) throws Exception{
        if (key.length() > MAX_INDEX_LENGTH)
            return "Error : Key Size Exceeds";
        byte[] uidRaw = new byte[MAX_INDEX_LENGTH];
        byte[] dataToPass = new byte[rawData.length + uidRaw.length];
        byte[] keyRaw = key.getBytes();
        System.arraycopy(keyRaw, 0, uidRaw, 0, keyRaw.length);
        System.arraycopy(uidRaw, 0, dataToPass, 0, uidRaw.length);
        System.arraycopy(rawData, 0, dataToPass, uidRaw.length, rawData.length);
        return new String(forward(namespace, "update", dataToPass));
    }

    public static long getBlockId(byte[] rawData){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(rawData);
        long blockId = buffer.getLong(0);
        buffer.clear();
        return blockId;
    }

    private byte[] getRawBlockId(long blockId){
        byte[] rawBlockId;
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, blockId);
        rawBlockId = buffer.array();
        buffer.clear();
        return rawBlockId;
    }

    private byte[] forward(String target, String command, byte[] rawData) throws Exception{
        byte[] rawTarget = target.getBytes();
        if (rawTarget.length > STACK_NAMESPACE_LENGTH)
            throw new Exception("Namespace size overflow. max(" + STACK_NAMESPACE_LENGTH + ")");
        byte[] request = new byte[rawData.length + STACK_HEADER_LENGTH];
        byte[] rawDes = stackDefinitions.get(target).getBytes();
        if (rawDes.length > STACK_DEFINITION_LENGTH)
            throw new Exception("Definition size overflow. max(" + STACK_DEFINITION_LENGTH + ")");
        byte[] rawCommand = command.getBytes();
        if (rawCommand.length > STACK_COMMAND_LENGTH)
            throw new Exception("Namespace size overflow. max(" + STACK_COMMAND_LENGTH + ")");
        System.arraycopy(rawTarget,0, request,0, rawTarget.length);
        System.arraycopy(rawCommand, 0, request, STACK_NAMESPACE_LENGTH, rawCommand.length);
        System.arraycopy(rawDes, 0, request,
                STACK_NAMESPACE_LENGTH + STACK_COMMAND_LENGTH, rawDes.length);
        String requestString = Base64.getEncoder().encodeToString(request);
        String path = URLEncoder.encode(requestString, "utf-8");
        URL url  = new URL(STACK_URL + path);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setDoOutput(false);
        byte[] response = IOUtils.toByteArray(con.getInputStream());
        con.getInputStream().close();
        con.disconnect();
        return response;
    }
}
