package com.sufnom.sys;

import org.json.JSONObject;

public class Config {
    public static final String FILE_NAME = "node-config.json";

    public static final String KEY_NODE_PORT = "stack_port";
    public static final String KEY_DEBUG = "debug";

    private static final Config session = new Config();
    public static Config getSession() { return session; }

    private JSONObject config;
    private Config(){
        config = new JSONObject(Common.readFile(FILE_NAME));
    }

    public String getValue(String key){
        return config.getString(key);
    }
}
