package com.fintech.lxf.bean;


public class ConstantAmountDto {
    private int id ;
    private String mchId ;
    private int constantAmount ;
    private int maxNum ;
    private String createTime ;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public int getConstantAmount() {
        return constantAmount;
    }

    public void setConstantAmount(int constantAmount) {
        this.constantAmount = constantAmount;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
