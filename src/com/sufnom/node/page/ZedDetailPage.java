package com.sufnom.node.page;

import org.json.JSONObject;

public class ZedDetailPage extends ZedPage {
    public ZedDetailPage(long pageId) {
        super(pageId);
    }

    @Override
    public void setRawBytes(byte[] rawBytes) {
        super.setRawBytes(rawBytes);
        byte[] bytes = new byte[4000];
        System.arraycopy(rawBytes, 96, bytes, 0, bytes.length);
        String json = new String(bytes).trim();
    }

    public void addDetail(String key, String value){
        try {
            JSONObject object = new JSONObject(getDetail());
            object.put(key, value);
            setDetail(object.toString());
        }
        catch (Exception e){e.printStackTrace();}
    }

    public boolean setDetail(String jsonDetail){
        try {
            byte[] bytes = jsonDetail.getBytes();
            if (jsonDetail.length() > 4000){
                System.out.println("Detail Size Overflowed");
                return false;
            }

            //Check for object constructor errors
            @SuppressWarnings("unused")
            JSONObject object = new JSONObject(jsonDetail);

            System.arraycopy(bytes, 0,
                    rawBytes, 96, bytes.length);
            saveToStack();
            return true;
        }
        catch (Exception e){e.printStackTrace();}
        return false;
    }

    public String getDetail(){
        byte[] bytes = new byte[4000];
        System.arraycopy(rawBytes, 96, bytes, 0, bytes.length);
        return new String(bytes);
    }
}
