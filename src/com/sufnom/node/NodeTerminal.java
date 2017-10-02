package com.sufnom.node;

import com.sufnom.lib.LRUCache;
import com.sufnom.node.ob.Editor;
import com.sufnom.node.page.NidListHolderPage;
import com.sufnom.node.page.ZedDetailPage;
import com.sufnom.node.page.ZedPage;
import com.sufnom.stack.StackProvider;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class NodeTerminal {
    private static NodeTerminal session = new NodeTerminal();
    public static NodeTerminal getSession() {
        return session;
    }

    private LRUCache<String, Long> sessionCache = new LRUCache<>(500);
    private LRUCache<Long, ZedPage> zedPageCache = new LRUCache<>(500);
    private NodeFactory factory = new NodeFactory();

    public NodeFactory getFactory() {
        return factory;
    }
    public String signIn(String email, String password){
        Editor editor = factory.getEditor(email, password);
        if (editor == null) return "null";
        else return pushSession(editor.editorId);
    }

    public String pushSession(long userId){
        String uid = UUID.randomUUID().toString();
        sessionCache.put(uid, userId);
        return uid;
    }

    public long getAdmin(String sessionId) throws Exception{
        long id = sessionCache.get(sessionId);
        if (id == 0) throw new Exception("Invalid Session");
        return id;
    }

    public ZedPage getPage(long pageId) throws Exception{
        ZedPage page = zedPageCache.get(pageId);
        if (page != null)
            return page;

        byte[] rawResponse = StackProvider.getSession()
                .getFixed(StackProvider.NAMESPACE_PAGE, pageId);
        String response = new String(rawResponse).trim();
        if (response.equals("error"))
            return null;
        //First Read Page Header and determine page type
        byte[] rawPageAttr = new byte[2];
        System.arraycopy(rawResponse, 0,
                rawPageAttr, 0, 2);
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put(rawPageAttr);
        buffer.position(0);
        short pageAttr = buffer.getShort();
        buffer.clear();
        switch (pageAttr){
            case ZedPage.PAGE_TYPE_NID_LIST:
                page = new NidListHolderPage(pageId);
                break;
            default:
                page = new ZedDetailPage(pageId);
                break;
        }
        page.setRawBytes(rawResponse);
        zedPageCache.put(pageId, page);

        return page;
    }

    public static String readFile(String filePath){
        try {
            InputStream stream = new FileInputStream(new File(filePath));
            return new String(IOUtils.toByteArray(stream));
        }
        catch (Exception e){e.printStackTrace();}
        return "";
    }
}
