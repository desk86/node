package com.sufnom.node.page;

import org.json.JSONArray;

import java.nio.ByteBuffer;

public class NidListHolderPage extends ZedPage {
    public NidListHolderPage(long pageId) {
        super(pageId);
    }

    public JSONArray getIdArray(){
        JSONArray array = new JSONArray();
        ByteBuffer buffer = ByteBuffer.allocate(8);
        long blockId;
        byte[] blockIdHolder = new byte[8];
        try {
            //Read Until blockId doesn't match 0
            //Skip 96 bytes header ie, start from 96
            for (int i = 96; i<4096; i+=8){
                System.arraycopy(rawBytes, i,
                        blockIdHolder, 0,
                        blockIdHolder.length);
                buffer.put(blockIdHolder);
                blockId = buffer.getLong(0);
                if (blockId == 0)
                    break;
                array.put(blockId);
            }
        }
        catch (Exception e){e.printStackTrace();}
        buffer.clear();
        return array;
    }

    public void add(long nid, short position){
        int rawPosition = (position * 8) + 96;
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, nid);
        buffer.clear();
        byte[] nidRawArray = buffer.array();
        System.arraycopy(nidRawArray, 0,
                rawBytes, rawPosition, nidRawArray.length);
        saveToStack();
    }
}
