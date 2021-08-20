package me.allinol.login;

import com.smartfoxserver.v2.core.ISFSEventListener;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.ISFSExtension;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.util.*;

public class CPILoginExtension extends SFSExtension implements ISFSExtension {


    public Hashtable<String, SFSObject> players = new Hashtable<>();
    private final ISFSEventListener loginListener = new CPILoginHandler(this);
    private final ISFSEventListener JoinZoneListener = new CPIJoinZoneListener(this);

    @Override
    public void init() {
        trace("lets a go");
        this.addListeners();
    }

    @Override
    public void handleClientRequest(String cmd, User user, ISFSObject params) {
        if (cmd.equals("time.get")) {
            this.getTime(user);
        }
    }

    private void getTime(User sender) {
        SFSObject response = new SFSObject();
        response.putLong("ct", 0);
        response.putLong("st", System.currentTimeMillis());
        this.send("time.get", response, sender);
    }

    @Override
    public void destroy() {
        this.removeEventListener(SFSEventType.USER_LOGIN, this.loginListener);
        this.removeEventListener(SFSEventType.USER_JOIN_ZONE, this.JoinZoneListener);
    }

    public void addListeners() {
        this.addEventListener(SFSEventType.USER_LOGIN, this.loginListener);
        this.addEventListener(SFSEventType.USER_JOIN_ZONE, this.JoinZoneListener);
    }

}
