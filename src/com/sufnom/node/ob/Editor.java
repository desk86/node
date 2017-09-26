package com.sufnom.node.ob;

import com.sufnom.node.NodeTerminal;
import com.sufnom.stack.StackProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.sql.ResultSet;

public class Editor {
    public final long editorId;

    private byte[] rawEditorData;
    private String name;

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

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Node getRootNode(){
        return null;
    }

    public void saveSelfToStack(){

    }

    public JSONObject getOb(){
        JSONObject object = new JSONObject();
        try {
            object.put("id", editorId);
            object.put("name", name);
        }
        catch (Exception e){e.printStackTrace();}
        return object;
    }

    @Override
    public String toString() { return getOb().toString(); }

    public static JSONObject getEditorInfo(long editorId){
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

    public static Editor createNew(String email, String pass, JSONObject content){
        byte[] rawData = new byte[StackProvider.MAX_FSI_SIZE];
        try {
            //Insert Blank Page and get admin Id

            ByteBuffer buffer = ByteBuffer.allocate(24);
            //Prepare Root Node

            //
            buffer.clear();
        }
        catch (Exception e){e.printStackTrace();}
        return null;
    }
}
