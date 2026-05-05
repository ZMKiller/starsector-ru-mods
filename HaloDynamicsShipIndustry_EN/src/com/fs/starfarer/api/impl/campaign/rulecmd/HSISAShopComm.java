package com.fs.starfarer.api.impl.campaign.rulecmd;

public class HSISAShopComm{
    public enum Type{
        SHIP,WEAPON,RESOURCE,FIGHTER
    }
    public String id;
    public String spec;
    public int cost;
    public int num;
    public Type type;
    public String globalFlag = "NAN";
    public HSISAShopComm(String id,String spec,Type type,int cost,int num){
        this.id = id;
        this.spec = spec;
        this.cost = cost;
        this.num = num;
        this.type = type;
    }

    public HSISAShopComm(String id,String spec,Type type,int cost,int num,String globalFlag){
        this.id = id;
        this.spec = spec;
        this.cost = cost;
        this.num = num;
        this.type = type;
        this.globalFlag = globalFlag;
    }
}