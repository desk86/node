package com.sufnom.node;

import com.sufnom.node.ob.Editor;
import com.sufnom.node.ob.Node;
import com.sufnom.node.ob.Synapse;
import com.sufnom.node.page.NidListHolderPage;
import com.sufnom.node.page.ZedDetailPage;
import com.sufnom.node.page.ZedPage;
import com.sufnom.stack.StackProvider;
import com.sun.istack.internal.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class NodeFactory {

    public Editor registerNew(String email, String password, String content){
        try {
            return Editor.createNew(email, password, content);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Node insertNode(long adminId, long parentId, String content) throws Exception{
        Node node = getNode(parentId);
        return node.createNewChild(adminId, content);
    }

    public Synapse insertSynapse(long adminId, long nodeId, String content) throws Exception{
        return Synapse.createNew(adminId, nodeId, content);
    }

    public Editor getEditor(String email, String password){
        try {
            Editor editor = Editor.findEditor(email);
            if (editor.isPasswordMatches(password))
                return editor;
        }
        catch (Exception e){e.printStackTrace();}
        return null;
    }

    public Editor getEditor(long editorId) throws Exception{
        return new Editor(editorId, StackProvider.getSession()
        .getExtended(StackProvider.NAMESPACE_EDITOR,editorId ));
    }

    public Node getNode(long parentId) throws Exception{
        return new Node(parentId, StackProvider.getSession()
                .getFixed(StackProvider.NAMESPACE_NODE, parentId));
    }

    public JSONArray getNodeList(long parentId){
        JSONArray array = new JSONArray();
        try {
            Node node = getNode(parentId);
            Node childNode;
            JSONArray nodeArray = node.getChildNodes();
            for (int i = 0; i < nodeArray.length(); i++){
                childNode = getNode(nodeArray.getLong(i));
                array.put(childNode.getJSON());
            }
        }
        catch (Exception e){e.printStackTrace();}
        return array;
    }

    public Synapse getSynapse(long synapseId) throws Exception{
        return new Synapse(synapseId, StackProvider.getSession()
                .getFixed(StackProvider.NAMESPACE_SYNAPSE, synapseId));
    }

    public JSONArray getSynapseList(long nodeId){
        JSONArray array = new JSONArray();
        try {
            Node node = getNode(nodeId);
            Synapse synapse;
            JSONArray synapseArray = node.getSynapseList();
            for (int i =0 ; i< synapseArray.length(); i++){
                synapse = getSynapse(synapseArray.getLong(i));
                array.put(synapse.getJSON());
            }
        }
        catch (Exception e){e.printStackTrace();}
        return array;
    }
}
