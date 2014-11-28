package com.origingame.item.resolver;

/**
 * User: Liub
 * Date: 2014/11/28
 */
public class DropAttr implements ItemAttr{

    private Entry[] entries;

    public DropAttr(Entry[] entries) {
        this.entries = entries;
    }

    public Entry[] getEntries() {
        return entries;
    }

    @Override
    public String getAttrName() {
        return "drops";
    }

    @Override
    public ItemAttr parseAttrValue(String attrValue) {




        return this;
    }

    static class Entry {

        private String itemId;

        private int number;

        private int prob;

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public int getProb() {
            return prob;
        }

        public void setProb(int prob) {
            this.prob = prob;
        }
    }
}
