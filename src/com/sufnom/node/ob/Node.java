package com.sufnom.node.ob;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;

public class Node {
    private static final String KEY_NODE_ID = "id";
    private static final String KEY_NODE_CONTENT = "content";

    public final long nodeId;
    private long adminId;
    private JSONObject content;
    private JSONArray editors;

    public Node(long nodeId){
        this.nodeId = nodeId;
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

    public static Node getFrom(ResultSet rs){
        try {
            Node node = new Node(rs.getLong(1));
            node.setAdminId(rs.getLong(2));
            node.setEditors(new JSONArray(rs.getString(3)));
            node.setContent(new JSONObject(rs.getString(4)));
            return node;
        }
        catch (Exception e){e.printStackTrace();}
        return null;
    }
}
