package com.ayalma.mobile_pos_plugin.posSdk;

import java.util.HashMap;
import java.util.Map;

public enum  PcPosType {
    Parsian(4,6),
    Saman(21,26),
    Rahyab(23,29),
    Pne(15,17);

    private int pcPosId;
    private  int creditTypeId;

    private PcPosType(int pcPosId,int creditTypeId) {
        this.pcPosId = pcPosId;
        this.creditTypeId = creditTypeId;
    }

    public int getPcPosId(){
        return pcPosId;
    }
    public  int getCreditTypeId(){
        return creditTypeId;
    }
    public static PcPosType valueOf(int pcPosId,int creditTypeId) throws Exception {
        for (PcPosType item:PcPosType.values()
             ) {
            if(item.creditTypeId == creditTypeId && item.pcPosId == pcPosId) return  item;
        }
        throw new Exception("pcPosType not supported!!");
    }


}