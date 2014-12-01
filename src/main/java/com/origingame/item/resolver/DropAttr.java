package com.origingame.item.resolver;

import com.origingame.util.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Liub
 * Date: 2014/11/28
 */
public class DropAttr implements ItemAttr{

    private List<Entry> entries = new ArrayList<>();

    @Override
    public String getAttrName() {
        return "drops";
    }

    @Override
    public ItemAttr parseAttrValue(String attrValue) throws Exception {
        List<List> dropListJson = JsonUtil.jsonToList(attrValue, List.class);
        for(List<String> drop : dropListJson) {
            entries.add(createEntry(drop));
        }
        return this;
    }

    private Entry createEntry(List<String> drop) {
        Entry entry = new Entry();
        entry.setItemId(drop.get(0));
        entry.setNumber(Integer.parseInt(drop.get(1)));
        if(drop.size() > 2) {
            entry.setProb(Integer.parseInt(drop.get(2)));
        } else {
            //不填写概率默认为必掉
            entry.setProb(ItemSpecManager.MAX_DROP_PROB);
        }
        return entry;
    }

    public List<Entry> getEntries() {
        return entries;
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
