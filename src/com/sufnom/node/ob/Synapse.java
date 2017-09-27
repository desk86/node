package com.sufnom.node.ob;

import com.sufnom.stack.StackProvider;
import org.json.JSONObject;

import java.nio.ByteBuffer;

public class Synapse {
    private static final String KEY_SYNAPSE_ID = "id";
    private static final String KEY_CONTENT = "content";

    public final long synapseId;

    private JSONObject content;
    private long adminId;
    private long parentId;
    private long timeStamp;
    private byte[] rawSynapseData;

    public Synapse(long synapseId, byte[] rawSynapseData){
        this.synapseId = synapseId;
        this.rawSynapseData = rawSynapseData;
    }

    private void iterateRawData(){
        
    }

    public long getAdminId() { return adminId; }
    public void setAdminId(long adminId) { this.adminId = adminId; }

    public long getParentId() { return parentId; }
    public void setParentId(long parentId) { this.parentId = parentId; }

    public long getTimeStamp() { return timeStamp; }
    public void setTimeStamp(long timeStamp) { this.timeStamp = timeStamp; }

    public JSONObject getContent() { return content; }
    public void setContent(JSONObject content) { this.content = content; }

    public JSONObject getJSON(){
        JSONObject ob = new JSONObject();
        try {
            ob.put(KEY_SYNAPSE_ID, synapseId);
            ob.put(KEY_CONTENT, content);
            ob.put("admin", Editor.getEditorInfo(adminId));
            ob.put("parent", parentId);
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
        ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.putLong(0, nodeId);
        buffer.putLong(16, adminId);
        byte[] synapseHeader = buffer.array();
        buffer.clear();
        System.arraycopy(synapseHeader, 0, rawData, 0, synapseHeader.length);
        System.arraycopy(contentBytes, 0, rawData, synapseHeader.length, contentBytes.length);
        long blockId = StackProvider.getSession().insertFixed(StackProvider.NAMESPACE_SYNAPSE, rawData);
        return new Synapse(blockId, rawData);
    }
}
