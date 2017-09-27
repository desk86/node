package com.sufnom.node.ob;

import com.sufnom.node.page.NidListHolderPage;
import com.sufnom.node.page.ZedPage;
import com.sufnom.stack.StackProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.sql.ResultSet;

public class Node {
    private static final String KEY_NODE_ID = "id";
    private static final String KEY_NODE_CONTENT = "content";

    public final long nodeId;
    private long adminId;
    private JSONObject content;
    private JSONArray editors;

    private int nodeChildCount;
    private int synapseCount;
    private long childPageId;
    private long synapsePageId;

    private byte[] rawNodeData;

    public Node(long nodeId, byte[] rawNodeData){
        this.nodeId = nodeId;
        this.rawNodeData = rawNodeData;
        iterateRawData();
    }

    private void iterateRawData(){

    }

    private NidListHolderPage getChildListPage(){
        return null;
    }

    private NidListHolderPage getSynapseListPage(){
        return null;
    }

    public long getAdminId() { return adminId; }
    public void setAdminId(long adminId) { this.adminId = adminId; }

    public JSONArray getEditors() { return editors; }
    public void setEditors(JSONArray editors) { this.editors = editors; }

    public JSONObject getContent() { return content; }
    public void setContent(JSONObject content) { this.content = content; }

    public JSONObject getJSON(){
        JSONObject ob = new JSONObject();
        try {
            ob.put(KEY_NODE_ID, nodeId);
            ob.put("admin", adminId);
            ob.put("editors", Editor.getDetailedInfo(editors));
            ob.put(KEY_NODE_CONTENT, content);
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

    // Add a new child node to this node object
    public Node createNewChild(){
        return null;
    }

    // Create a new node without node dependencies
    public static Node createNew(long adminId) throws Exception{
        //Prepare Editor NidList
        ZedPage editorListPage = ZedPage.createNew(ZedPage.PAGE_TYPE_NID_LIST);

        //Prepare Child NidList
        ZedPage childListPage = ZedPage.createNew(ZedPage.PAGE_TYPE_NID_LIST);

        //Prepare Synapse NidList
        ZedPage synapseListPage = ZedPage.createNew(ZedPage.PAGE_TYPE_NID_LIST);

        //Create Header
        ByteBuffer buffer = ByteBuffer.allocate(38);

        //Put Nid(s)
        buffer.putLong(2, editorListPage.pageId);
        buffer.putLong(18, childListPage.pageId);
        buffer.putLong(22, synapseListPage.pageId);

        //Put default hashes
        buffer.putLong(0,0);
        buffer.putLong(10,0);
        buffer.putLong(20, 0);

        //Put Authorization Vars
        buffer.putLong(30, adminId);

        //Contact Stack to create a new node
        long blockId = StackProvider.getSession()
                .insertFixed(StackProvider.NAMESPACE_NODE, buffer.array());
        buffer.clear();
        byte[] rawDataBytes = StackProvider.getSession()
                .getFixed(StackProvider.NAMESPACE_NODE, blockId);
        return new Node(blockId, rawDataBytes);
    }
}
