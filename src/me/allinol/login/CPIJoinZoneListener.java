package me.allinol.login;

import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.api.ISFSApi;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.ISFSEventListener;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.SFSRoomRemoveMode;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CPIJoinZoneListener extends BaseServerEventHandler implements ISFSEventListener
{
    private final CPILoginExtension mainExtension;

    CPIJoinZoneListener(CPILoginExtension mainExtension) {
        this.mainExtension = mainExtension;
    }

    @Override
    public void handleServerEvent(ISFSEvent event) {
        User user = (User) event.getParameter(SFSEventParam.USER);
        Room room = this.createRoomSession(user);
        this.sendUserVariables(user);
        this.sendJoinRoom(user, room);
    }

    private void sendJoinRoom(User user, Room room) {
    	this.mainExtension.trace("Telling ", user.getName(), " to join their room");
        try {
            this.mainExtension.getApi().joinRoom(user, room);
        } catch (SFSJoinRoomException ignored) {}
    }

    private void sendUserVariables(User user) {
    	this.mainExtension.trace("Sending ", user.getName(),"'s user variable");
        ISFSApi sfsApi = this.mainExtension.getApi();

        SFSObject player = this.mainExtension.players.get(user.getName());
        SFSObject playerRoomData = (SFSObject) player.getClass("playerRoomData");
        SFSObject outfitData = (SFSObject) playerRoomData.getClass("outfit");
        SFSObject profile = (SFSObject) playerRoomData.getClass("profile");
        
        SFSUserVariable outfit = SFSUserVariable.newFromStringLiteral("outfit", "string", ((SFSObject) outfitData.getClass("parts")).toJson());
        SFSUserVariable colour = SFSUserVariable.newFromStringLiteral("colour", "int", profile.getClass("colour").toString());
        SFSUserVariable tube = SFSUserVariable.newFromStringLiteral("tube", "int", "0");
        SFSUserVariable sess = SFSUserVariable.newFromStringLiteral("sess", "string", player.getClass("sessionId").toString());

        ArrayList<com.smartfoxserver.v2.entities.variables.UserVariable> variables = new ArrayList<>();
        variables.add(outfit);
        variables.add(colour);
        variables.add(tube);
        variables.add(sess);

        sfsApi.setUserVariables(user, variables);
    }

    private Room createRoomSession(User user) {
        this.mainExtension.trace("Creating Room for", user.getName());
        SFSObject player = this.mainExtension.players.get(user.getName());
        String roomName = (String) player.getClass("room");

        Zone parentZone = this.mainExtension.getParentZone();
        ISFSApi sfsApi = this.mainExtension.getApi();
        Room room;
        try {
            CreateRoomSettings cfg = new CreateRoomSettings();
            cfg.setAutoRemoveMode(SFSRoomRemoveMode.WHEN_EMPTY);
            cfg.setDynamic(true);
            cfg.setExtension(new CreateRoomSettings.RoomExtensionSettings("CPIJava", "me.allinol.world.CPIWorldExtension"));
            cfg.setName(roomName);
            room = sfsApi.createRoom(parentZone, cfg, null);
        } catch (SFSCreateRoomException e) {
            e.printStackTrace();
            room = parentZone.getRoomByName(roomName);
        }

        return room;
        
    }


}