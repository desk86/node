package com.sufnom.node;

import com.sufnom.node.ob.Editor;
import com.sufnom.node.ob.Node;
import com.sufnom.node.ob.Synapse;
import com.sufnom.node.page.NidListHolderPage;
import com.sufnom.node.page.ZedDetailPage;
import com.sufnom.node.page.ZedPage;
import com.sufnom.stack.StackProvider;
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

    public Editor getEditor(String email, String password){
        try {
            Editor editor = Editor.findEditor(email);
            if (editor.isPasswordMatches(password))
                return editor;
        }
        catch (Exception e){e.printStackTrace();}
        return null;
    }

    public Editor getEditor(long editorId){
        return null;
    }

    public Synapse insertSynapse(long nodeId, String content, long adminId){
        return null;
    }

    public Synapse getSynapse(long synapseId){
        return null;
    }

    public Node insertNode(long parentId, String content, long adminId){
        return null;
    }

    public Node getNode(long parentId){
        try {

        }
        catch (Exception e){e.printStackTrace();}
        return null;
    }

    public List<Long> getChildList(long parentId){
        List<Long> childIdList = new ArrayList<>();
        return childIdList;
    }

    public JSONArray getNodeList(long parentId){
        JSONArray array = new JSONArray();
        return array;
    }

    public JSONArray getSynapseList(long nodeId){
        JSONArray array = new JSONArray();
        return array;
    }
}
