package com.origingame.item.model;

/**
 * User: Liub
 * Date: 2014/11/28
 */
public class BuffItem extends Item {

    private Type type;

    private int effectValue;

    private int liveTime;

    private boolean percent;

    private String buffDesc;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getEffectValue() {
        return effectValue;
    }

    public void setEffectValue(int effectValue) {
        this.effectValue = effectValue;
    }

    public int getLiveTime() {
        return liveTime;
    }

    public void setLiveTime(int liveTime) {
        this.liveTime = liveTime;
    }

    public boolean isPercent() {
        return percent;
    }

    public void setPercent(boolean percent) {
        this.percent = percent;
    }

    public String getBuffDesc() {
        return buffDesc;
    }

    public void setBuffDesc(String buffDesc) {
        this.buffDesc = buffDesc;
    }

    public enum Type {

        /**
         * 积分增加
         */
        SCORE(1),

        /**
         * 金币增加
         */
        GOLD(2);

        public int value;

        Type(int value) {
            this.value = value;
        }

        public static Type valueOf(int value) {
            if(value == 0) {
                return null;
            }
            for(Type enumValue : values()) {
                if(value == enumValue.value) {
                    return enumValue;
                }
            }
            return null;
        }

    }

}
