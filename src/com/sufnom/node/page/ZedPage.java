package com.sufnom.node.page;

import com.sufnom.stack.StackProvider;

import java.nio.ByteBuffer;

public abstract class ZedPage {
    public static final short PAGE_TYPE_NID_LIST = 0;
    public static final short PAGE_TYPE_DETAIL = 1;
    public static ZedPage createNew(short pageType) throws Exception{
        //Insert A Blank data and get its id.
        //Always add this id immediately to related table.
        //for example when a page is created for listing
        //child table, then write this page id immediately
        //to the header, and save the header to disk.
        //Creating a page, opens a blank 4k block inside disk
        //this created block is permanent, and currently not
        //deletable

        //Stack will return pageId if everything went well0
        long pageId = StackProvider.getSession()
                .insertFixed(StackProvider.NAMESPACE_PAGE, new byte[0]);

        //byte[] pageHeader = new byte[96];
        byte[] pageAttr;
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.position(0);
        buffer.putShort(pageType);
        pageAttr = buffer.array();
        buffer.clear();
        ZedPage page;
        switch (pageType){
            case PAGE_TYPE_NID_LIST:
                page = new NidListHolderPage(pageId);
                break;
            case PAGE_TYPE_DETAIL:
                page = new ZedDetailPage(pageId);
                break;
            default: page = new ZedDetailPage(pageId);
        }
        System.arraycopy(pageAttr, 0,
                page.rawBytes, 0, pageAttr.length);
        page.saveToStack();
        return page;
    }

    public final long pageId;
    protected byte[] rawBytes;

    public ZedPage(long pageId){this.pageId = pageId;}

    public void setRawBytes(byte[] rawBytes) {
        if (rawBytes == null) return;
        if (rawBytes.length < 4096)
            System.out.println("Raw Page Data Size Low");
        this.rawBytes = rawBytes;
    }

    public void saveToStack(){
        try {
            String response = StackProvider.getSession()
                    .updateFixed(StackProvider.NAMESPACE_PAGE, pageId, rawBytes);
            if (!response.equals("ok"))
                new Exception("ZedPage : Invalid Response : " + response).printStackTrace();
        }
        catch (Exception e){e.printStackTrace();}
    }
}
