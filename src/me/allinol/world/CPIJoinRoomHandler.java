package me.allinol.world;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.ISFSEventListener;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class CPIJoinRoomHandler extends BaseServerEventHandler implements ISFSEventListener
{
    private final CPIWorldExtension mainExtension;

    CPIJoinRoomHandler(CPIWorldExtension mainExtension) {
        this.mainExtension = mainExtension;
    }

    @Override
    public void handleServerEvent(ISFSEvent event) {
    	Room room = (Room) event.getParameter(SFSEventParam.ROOM);
    	this.mainExtension.trace(room);
    	//User user = (User) event.getParameter(SFSEventParam.USER);
        //Room room = this.createRoomSession(user);
        //this.sendJoinRoom(user, room);
    }


}