package com.sufnom.node.ob;

import com.sufnom.node.NodeTerminal;
import com.sufnom.node.page.NidListHolderPage;
import com.sufnom.node.page.ZedDetailPage;
import com.sufnom.node.page.ZedPage;
import com.sufnom.stack.StackProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;

public class Node {
    private static final String KEY_NODE_ID = "id";
    private static final String KEY_NODE_CONTENT = "content";

    public static final int HEADER_SIZE = 46;

    public final long nodeId;

    private long adminId;
    private long contentPageId;

    private short editorCount;
    private short nodeChildCount;
    private short synapseCount;

    private long editorPageId;
    private long childPageId;
    private long synapsePageId;

    private byte[] rawNodeData;

    public Node(long nodeId, byte[] rawNodeData){
        this.nodeId = nodeId;
        this.rawNodeData = rawNodeData;
        bindHeaders();
    }

    private void bindHeaders(){
        try {
            byte[] header = new byte[HEADER_SIZE];
            ByteBuffer buffer = ByteBuffer.allocate(header.length);
            System.arraycopy(rawNodeData, 0, header, 0, header.length);
            buffer.put(header);

            //get hashes
            editorCount = buffer.getShort(0);
            nodeChildCount = buffer.getShort(10);
            synapseCount = buffer.getShort(30);

            //get pageIds
            editorPageId = buffer.getLong(2);
            childPageId = buffer.getLong(12);
            synapsePageId = buffer.getLong(22);
            contentPageId = buffer.getLong(38);

            //get auth
            adminId = buffer.getLong(30);

            buffer.clear();
        }
        catch (Exception e){e.printStackTrace();}
    }

    public long getAdminId() { return adminId; }

    public JSONArray getEditors() throws Exception{
        NidListHolderPage page = (NidListHolderPage)NodeTerminal.getSession()
                .getPage(editorPageId);
        return page.getIdArray();
    }

    public JSONObject getContent() throws Exception{
        ZedDetailPage page = (ZedDetailPage)NodeTerminal.getSession()
                .getPage(contentPageId);
        return new JSONObject(page.getDetail());
    }
    public void setContent(JSONObject content) throws Exception {
        ZedDetailPage page = (ZedDetailPage) NodeTerminal.getSession()
                .getPage(contentPageId);
        page.setDetail(content.toString());
    }

    public JSONArray getChildNodes() throws Exception{
        NidListHolderPage page = (NidListHolderPage)NodeTerminal.getSession()
                .getPage(childPageId);
        return page.getIdArray();
    }

    public JSONArray getSynapseList() throws Exception{
        NidListHolderPage page = (NidListHolderPage)NodeTerminal.getSession()
                .getPage(synapsePageId);
        return page.getIdArray();
    }

    public JSONObject getJSON(){
        JSONObject ob = new JSONObject();
        try {
            ob.put(KEY_NODE_ID, nodeId);
            ob.put("admin", adminId);
            ob.put("editors", Editor.getDetailedInfo(getEditors()));
            ob.put(KEY_NODE_CONTENT, getContent());
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
    public Node createNewChild(long adminId, String content) throws Exception{
        Node childNode = createNew(adminId, content);
        NidListHolderPage page = (NidListHolderPage)NodeTerminal.getSession()
                .getPage(childPageId);
        page.add(childNode.childPageId, nodeChildCount);
        nodeChildCount++;
        saveHeaders();
        saveToStack();
        return childNode;
    }

    public Synapse createSynapse(String content) throws Exception{
        Synapse synapse = Synapse.createNew(adminId, nodeId, content);
        NidListHolderPage page = (NidListHolderPage)NodeTerminal.getSession()
                .getPage(synapsePageId);
        page.add(synapse.synapseId, synapseCount);
        synapseCount++;
        saveHeaders();
        saveToStack();
        return synapse;
    }

    public void addEditor(Editor editor) throws Exception{
        NidListHolderPage page = (NidListHolderPage)NodeTerminal.getSession()
                .getPage(editorPageId);
        page.add(editor.editorId, editorCount);
        editorCount++;
        saveHeaders();
        saveToStack();
    }

    private void saveHeaders(){
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);

        //put hashes
        buffer.putShort(0, editorCount);
        buffer.putShort(10, nodeChildCount);
        buffer.putShort(20, synapseCount);

        //put pageIds
        buffer.putLong(2, editorPageId);
        buffer.putLong(12, childPageId);
        buffer.putLong(22, synapsePageId);

        buffer.putLong(30, adminId);
        buffer.putLong(38, contentPageId);
        byte[] header = buffer.array();
        buffer.clear();

        System.arraycopy(header, 0, rawNodeData, 0, header.length);
    }

    private void saveToStack() throws Exception{
        StackProvider.getSession()
                .updateFixed(StackProvider.NAMESPACE_NODE,
                        nodeId, rawNodeData);
    }

    // Create a new node without node dependencies
    public static Node createNew(long adminId, String content) throws Exception{
        //Prepare Editor NidList
        ZedPage editorListPage = ZedPage.createNew(ZedPage.PAGE_TYPE_NID_LIST);

        //Prepare Child NidList
        ZedPage childListPage = ZedPage.createNew(ZedPage.PAGE_TYPE_NID_LIST);

        //Prepare Synapse NidList
        ZedPage synapseListPage = ZedPage.createNew(ZedPage.PAGE_TYPE_NID_LIST);

        //Prepare content page
        ZedDetailPage contentPage = (ZedDetailPage)ZedPage.createNew(ZedPage.PAGE_TYPE_DETAIL);
        contentPage.setDetail(content);

        //Create Header
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);

        //Put Nid(s)
        buffer.putLong(2, editorListPage.pageId);
        buffer.putLong(12, childListPage.pageId);
        buffer.putLong(22, synapseListPage.pageId);

        buffer.putLong(38, contentPage.pageId);

        //Put default hashes
        buffer.putShort(0, (short)0);
        buffer.putShort(10, (short)0);
        buffer.putShort(20, (short)0);

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
