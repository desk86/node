package com.sufnom.stack;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class StackProvider {
    // DO NOT CHANGE THIS VALUE, KEEP IT TO 50 ALWAYS
    // This value relates the max size of the key, that ZedIndexer can hold
    // If you need to change this value, you must recompile stack giving desired max length
    // See ZedIndexer docs of ExtendedStackInterface
    public static final int MAX_INDEX_LENGTH = 50;

    public static final String NAMESPACE_NODE = "node";
    public static final String NAMESPACE_SYNAPSE = "synapse";
    public static final String NAMESPACE_EDITOR = "editor";

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
    }

    private HashMap<String, String> stackDefinitions;

    private long insertFixed(String namespace, byte[] rawData){
        return getBlockId(forward(namespace, "insert", rawData));
    }

    private String insertExtended(String namsespace, String key, byte[] rawData){

        return "ok/error";
    }

    private long getBlockId(byte[] rawData){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(rawData);
        long blockId = buffer.getLong(0);
        buffer.clear();
        return blockId;
    }


    private byte[] forward(String target, String command, byte[] rawData){

        return new byte[0];
    }
}
