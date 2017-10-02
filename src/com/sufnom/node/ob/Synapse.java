package com.sufnom.node.ob;

import com.sufnom.stack.StackProvider;
import org.json.JSONObject;

import java.nio.ByteBuffer;

public class Synapse {
    private static final String KEY_SYNAPSE_ID = "id";
    private static final String KEY_CONTENT = "content";

    public static final int HEADER_SIZE = 32;

    public final long synapseId;

    private JSONObject content;

    private long adminId;
    private long nodeId;
    private long timeStamp;

    private byte[] rawSynapseData;

    public Synapse(long synapseId, byte[] rawSynapseData){
        this.synapseId = synapseId;
        this.rawSynapseData = rawSynapseData;
    }

    private void iterateRawData(){
        byte[] header = new byte[HEADER_SIZE];
        System.arraycopy(rawSynapseData, 0, header, 0, header.length);
        ByteBuffer buffer = ByteBuffer.allocate(header.length);
        buffer.put(header);
        nodeId = buffer.getLong(0);
        adminId = buffer.getLong(16);
        timeStamp = buffer.getLong(24);
        buffer.clear();
    }

    public long getAdminId() { return adminId; }

    public long getTimeStamp() { return timeStamp; }

    public JSONObject getContent() { return content; }
    public void setContent(JSONObject content) { this.content = content; }

    public JSONObject getJSON(){
        JSONObject ob = new JSONObject();
        try {
            ob.put(KEY_SYNAPSE_ID, synapseId);
            ob.put(KEY_CONTENT, content);
            ob.put("admin", Editor.getEditorInfo(adminId));
            ob.put("parent", nodeId);
            ob.put("timestamp", timeStamp);
        }
        catch (Exception e){e.printStackTrace();}
        return ob;
    }

    @Override
    public String toString() {
        try {
            return getJSON().toString();
        }
        catch (Exception e){e.printStackTrace();}
        return super.toString();
    }

    public static Synapse createNew(long adminId,
                        long nodeId, String content) throws Exception{
        byte[] contentBytes = content.getBytes();
        int size = 24 + contentBytes.length;

        byte[] rawData = new byte[size];
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);

        buffer.putLong(0, nodeId);
        buffer.putLong(16, adminId);
        buffer.putLong(24, System.currentTimeMillis());

        byte[] synapseHeader = buffer.array();
        buffer.clear();

        System.arraycopy(synapseHeader, 0, rawData, 0, synapseHeader.length);
        System.arraycopy(contentBytes, 0, rawData, synapseHeader.length, contentBytes.length);
        long blockId = StackProvider.getSession().insertFixed(StackProvider.NAMESPACE_SYNAPSE, rawData);
        return new Synapse(blockId, rawData);
    }
}
