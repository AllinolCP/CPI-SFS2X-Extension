package me.allinol.login;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.ISFSEventListener;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class CPILoginHandler extends BaseServerEventHandler implements ISFSEventListener
{
    private final CPILoginExtension mainExtension;

    CPILoginHandler(CPILoginExtension mainExtension) {
        this.mainExtension = mainExtension;
        
    }

    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException
    {
    	this.mainExtension.trace(event.toString());
        Object name = event.getParameter(SFSEventParam.LOGIN_NAME);
        
        String username = (String) name;
        SFSObject loginInData = (SFSObject) event.getParameter(SFSEventParam.LOGIN_IN_DATA);
        
		SFSObject data = (SFSObject) SFSObject.newFromJsonData((String) loginInData.getClass("joinRoomData"));
        this.mainExtension.players.put(username, (SFSObject) data.getClass("data"));
    }


}