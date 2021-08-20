package me.allinol.rewards;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;

public class CPIRewards {

    public boolean handle(String cmd, User user, ISFSObject params) {

        switch (cmd){
            case "rewards.add":

                break;
            default:
                return false;
        }

        return true;
    }

}
