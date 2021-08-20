package me.allinol.world;

import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.api.ISFSApi;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.SFSRoomRemoveMode;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;
import com.smartfoxserver.v2.extensions.SFSExtension;
import me.allinol.rewards.CPIRewards;

import javax.crypto.*;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class CPIWorldExtension extends SFSExtension {
    private CPIJoinRoomHandler JoinRoomListener;
    private CPIRewards RewardsHandler;

    @Override
    public void init() {
        trace("lets a go");
        this.JoinRoomListener = new CPIJoinRoomHandler(this);
        this.RewardsHandler = new CPIRewards();
        this.addListeners();
        
    }

    @Override
    public void handleClientRequest(String cmd, User user, ISFSObject params) {
        switch(cmd) {
            case "l.a":
                this.locomotiveAction(user, params);
                break;
            case "time.get":
                this.getTime(user, params);
                break;
            case "encryption.get":
                this.sendAesKey(user, params);
                break;
            case "zone.logout":
                this.zoneLogout(user);
                break;
            default:
                trace("Handler for", cmd, "doesn't exist!");

        }
    }

    private void zoneLogout(User user) {
	    this.createRoomSession(user);
        this.send("zone.logout", new SFSObject(), user);
    }

    private void createRoomSession(User user) {
        this.trace("Creating Room for", user.getName());

        String roomName = "Alpine:en_US:Diving::1.13.0;66ee6b724b9ae215a0c4f39907ece0fda5c5328a;2018-11-24;NONE";
        
        Zone parentZone = this.getParentZone();
        ISFSApi sfsApi = this.getApi();
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
        trace("Telling ", user.getName(), " to join their room");
        try {

            this.getApi().joinRoom(user, room);
        } catch (SFSJoinRoomException ignored) {}

    }
    
    private void locomotiveAction(User user, ISFSObject params) {
        params.putInt("senderId", user.getId());
        this.send("l.a", params, user.getLastJoinedRoom().getUserList());
    }

    private SecretKey generateAESKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            return generator.generateKey();
        } catch(NoSuchAlgorithmException ignored) {}
        return null;
    }

    private void sendAesKey(User user, ISFSObject params) {
        String modulus = (String) params.getClass("pkm");
        String exponent = (String) params.getClass("pke");

        BigInteger m = new BigInteger(1, Base64.getDecoder().decode(modulus));
        BigInteger e = new BigInteger(1, Base64.getDecoder().decode(exponent));

        SecretKey key = this.generateAESKey();
        user.setProperty("AES_KEY", key);
        SFSObject response = new SFSObject();
        assert key != null;
        response.putUtfString("ek", this.encryptRsa(key.getEncoded(), m, e));

        this.send("encryption.get", response, user);

    }

    private String encryptRsa(byte[] encoded, BigInteger m, BigInteger e) {
        try {
            KeySpec spec = new RSAPublicKeySpec(m, e);

            KeyFactory factory = KeyFactory.getInstance("RSA");
            Key pubKey = factory.generatePublic(spec);

            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, pubKey);

            byte[] cipherText = encryptCipher.doFinal(encoded);
            return Base64.getEncoder().encodeToString(cipherText);
        } catch(InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException ignored) {} catch (BadPaddingException | IllegalBlockSizeException badPaddingException) {
            badPaddingException.printStackTrace();
        }
        return null;
    }

    private void getTime(User sender, ISFSObject params) {
        SFSObject response = new SFSObject();
        response.putLong("ct", params.getLong("ct"));
        response.putLong("st", System.currentTimeMillis());
        this.send("time.get", response, sender);
    }

    @Override
    public void destroy() {
        this.removeEventListener(SFSEventType.USER_JOIN_ROOM, this.JoinRoomListener);
    }

    public void addListeners() {
    	this.addEventListener(SFSEventType.USER_JOIN_ROOM, this.JoinRoomListener);
    }

}
