package com.sufnom.node.ob;

import com.sufnom.node.NodeTerminal;
import com.sufnom.node.page.ZedDetailPage;
import com.sufnom.node.page.ZedPage;
import com.sufnom.stack.StackProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.sql.BatchUpdateException;
import java.sql.ResultSet;

public class Editor {
    public final long editorId;

    private byte[] rawEditorData;

    private long rootNodeId;
    private long authPageId;
    private long detailPageId;

    public Editor(long editorId, byte[] rawEditorData){
        this.editorId = editorId;
        this.rawEditorData = rawEditorData;
        iterateRawData();
    }

    private void iterateRawData(){
        byte[] header = new byte[24];
        ByteBuffer buffer = ByteBuffer.allocate(24);
        System.arraycopy(rawEditorData, 0, header, 0, header.length);
        buffer.put(header);
        rootNodeId = buffer.getLong(0);
        authPageId = buffer.getLong(8);
        detailPageId = buffer.getLong(16);
        buffer.clear();
    }

    public boolean isPasswordMatches(String pass){
        try {
            ZedDetailPage page = getAuthPage();
            JSONObject ob = new JSONObject(page.getDetail());
            return ob.getString("pass").equals(pass);
        }
        catch (Exception e){e.printStackTrace();}
        return false;
    }

    public ZedDetailPage getAuthPage() throws Exception{
        return (ZedDetailPage)NodeTerminal.getSession()
                .getPage(authPageId);
    }

    public Node getRootNode() throws Exception{
        return new Node(rootNodeId, StackProvider.getSession()
            .getFixed(StackProvider.NAMESPACE_NODE, rootNodeId));
    }

    public String getName() throws Exception{
        return getDetail().getString("name");
    }

    public JSONObject getDetail() throws Exception{
        ZedDetailPage page = (ZedDetailPage)NodeTerminal.getSession()
                .getPage(detailPageId);
        return new JSONObject(page.getDetail());
    }

    public JSONObject getOb(){
        JSONObject object = new JSONObject();
        try {
            object.put("id", editorId);
            object.put("node", rootNodeId);

            object.put("name", getName());
        }
        catch (Exception e){e.printStackTrace();}
        return object;
    }

    @Override
    public String toString() { return getOb().toString(); }

    public static JSONObject getEditorInfo(long editorId) throws Exception{
        Editor editor = NodeTerminal.getSession().getFactory()
                .getEditor(editorId);
        if (editor != null)
            return editor.getOb();
        return new JSONObject();
    }

    public static JSONArray getDetailedInfo(JSONArray editorIdArray){
        JSONArray array = new JSONArray();
        long editorId;
        try {
            for (int i=0; i < editorIdArray.length(); i++){
                editorId = editorIdArray.getLong(i);
                array.put(getEditorInfo(editorId));
            }
        }
        catch (Exception e){e.printStackTrace();}
        return array;
    }

    public static Editor findEditor(String email) throws Exception{
        byte[] rawData = StackProvider.getSession()
                .getExtended(StackProvider.NAMESPACE_EDITOR, email);
        if (rawData.length != StackProvider.MAX_FSI_SIZE)
            throw new Exception("Invalid Response");
        byte[] rawBlockId = new byte[8];
        System.arraycopy(rawData, 0, rawBlockId, 0, rawBlockId.length);
        long blockId = StackProvider.getBlockId(rawBlockId);
        return new Editor(blockId, rawData);
    }

    public static Editor createNew(String email, String pass,
                   String content) throws Exception{
        byte[] rawData = new byte[StackProvider.MAX_FSI_SIZE];
        //Insert Blank Page and get admin Id
        String msg = StackProvider.getSession()
                .insertExtended(StackProvider.NAMESPACE_EDITOR,
                        email, rawData);
        if (!msg.equals("ok"))
            throw new Exception("Invalid Response From Stack : " + msg);
        rawData = StackProvider.getSession()
                .getExtended(StackProvider.NAMESPACE_EDITOR, email);
        byte[] rawBlockId = new byte[8];
        System.arraycopy(rawData, 0, rawBlockId, 0, rawBlockId.length);
        long blockId = StackProvider.getBlockId(rawBlockId);

        //Prepare Auth Page
        ZedDetailPage authPage = (ZedDetailPage)ZedPage
                .createNew(ZedPage.PAGE_TYPE_DETAIL);
        authPage.addDetail("pass", pass);

        //Prepare Detail Page
        ZedDetailPage detailPage = (ZedDetailPage) ZedPage
                .createNew(ZedPage.PAGE_TYPE_DETAIL);
        detailPage.setDetail(content);

        //Prepare Root Node
        JSONObject obEditor = new JSONObject(content);
        JSONObject ob = new JSONObject();
        ob.put("title", obEditor.getString("name") + "'s Node");
        Node node = Node.createNew(blockId, ob.toString());

        ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.putLong(0, node.nodeId);
        buffer.putLong(8, authPage.pageId);
        buffer.putLong(16, detailPage.pageId);

        byte[] header = buffer.array();

        buffer.clear();
        System.arraycopy(header, 0, rawData, 8, header.length);
        StackProvider.getSession().updateExtended(
                StackProvider.NAMESPACE_EDITOR, email, rawData);

        return new Editor(blockId, rawData);
    }
}
