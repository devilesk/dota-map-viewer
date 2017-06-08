package dota;

import skadistats.clarity.decoder.Util;
import skadistats.clarity.model.Entity;
import skadistats.clarity.model.FieldPath;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.entities.OnEntityEntered;
import skadistats.clarity.processor.entities.OnEntityLeft;
import skadistats.clarity.processor.entities.UsesEntities;
import skadistats.clarity.processor.reader.OnTickStart;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.processor.stringtables.UsesStringTable;
import skadistats.clarity.source.InputStreamSource;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import javafx.scene.control.ProgressBar;

public class Parse {

    private Integer time = 0;
    final List<Entry> wards = new ArrayList<>();
    Integer gameStartTime = 0;
    Integer gameEndTime = 0;
    boolean init = false;
    int numPlayers = 10;
    int[] validIndices = new int[numPlayers];
    HashMap<Integer, Long> slot_to_steamid = new HashMap<>();
    HashMap<Long, Integer> steamid_to_slot = new HashMap<>();
    HashMap<Long, String> steamid_to_playerName = new HashMap<>();

    public Parse(InputStream input) throws IOException
    {
        long tStart = System.currentTimeMillis();
        new SimpleRunner(new InputStreamSource(input)).runWith(this);
        long tMatch = System.currentTimeMillis() - tStart;
        System.err.format("total time taken: %s\n", (tMatch) / 1000.0);
    }

    private static Entry findWard(List<Entry> c, Entry e) {
        for (Entry o : c) {
            if(o != null && o.key.equals(e.key) && o.ehandle.equals(e.ehandle)) {
                return o;
            }
        }
        return null;
    }

    private void output(Entry e) {
        Entry o = findWard(wards, e);
        if (o != null) {
            if (e.isDead()) {
                //System.out.printf("ward expire (%s, %s) %s\n", e.x, e.y, e.ehandle);
                o.expireTime = e.time;
            }
            else {
                //System.out.printf("ward exists (%s, %s) %s\n", e.x, e.y, e.ehandle);
            }
        }
        else {
            //System.out.printf("ward add (%s, %s) %s\n", e.x, e.y, e.ehandle);
            wards.add(e);
        }
    }

    @OnEntityEntered
    public void onEntityEntered(Context ctx, Entity e) {
        processEntity(ctx, e, false);
    }

    @OnEntityLeft
    public void onEntityLeft(Context ctx, Entity e) {
        processEntity(ctx, e, true);
    }

    @UsesStringTable("EntityNames")
    @UsesEntities
    @OnTickStart
    public void onTickStart(Context ctx, boolean synthetic) {
        //TODO check engine to decide whether to use s1 or s2 entities
        //ctx.getEngineType()

        //s1 DT_DOTAGameRulesProxy
        Entity grp = ctx.getProcessor(Entities.class).getByDtName("CDOTAGamerulesProxy");
        Entity pr = ctx.getProcessor(Entities.class).getByDtName("CDOTA_PlayerResource");

        if (grp != null)
        {
            //System.err.println(grp);
            //dota_gamerules_data.m_iGameMode = 22
            //dota_gamerules_data.m_unMatchID64 = 1193091757
            time = Math.round(getEntityProperty(grp, "m_pGameRules.m_fGameTime", null));

            int currGameStartTime = Math.round(grp.getProperty("m_pGameRules.m_flGameStartTime"));
            if (currGameStartTime != gameStartTime) {
                gameStartTime = currGameStartTime;
                System.err.println(gameStartTime);
                System.err.println(time);
            }

            int currGameEndTime = Math.round(grp.getProperty("m_pGameRules.m_flGameEndTime"));
            if (currGameEndTime != gameEndTime) {
                gameEndTime = currGameEndTime;
                System.err.println(gameEndTime);
                System.err.println(time);
            }
        }
        if (pr != null) {
            //Radiant coach shows up in vecPlayerTeamData as position 5
            //all the remaining dire entities are offset by 1 and so we miss reading the last one and don't get data for the first dire player
            //coaches appear to be on team 1, radiant is 2 and dire is 3?
            //construct an array of valid indices to get vecPlayerTeamData from
            if (!init) {
                int added = 0;
                int i = 0;
                //according to @Decoud Valve seems to have fixed this issue and players should be in first 10 slots again
                //sanity check of i to prevent infinite loop when <10 players?
                while (added < numPlayers && i < 100) {
                    try {
                        //check each m_vecPlayerData to ensure the player's team is radiant or dire
                        int playerTeam = getEntityProperty(pr, "m_vecPlayerData.%i.m_iPlayerTeam", i);
                        int teamSlot = getEntityProperty(pr, "m_vecPlayerTeamData.%i.m_iTeamSlot", i);
                        Long steamid = getEntityProperty(pr, "m_vecPlayerData.%i.m_iPlayerSteamID", i);
                        String playerName = getEntityProperty(pr, "m_vecPlayerData.%i.m_iszPlayerName", i);
                        //System.err.format("%s %s %s: %s\n", i, playerTeam, teamSlot, steamid);
                        if (playerTeam == 2 || playerTeam == 3) {
                            //output the player_slot based on team and teamslot
                            //add it to validIndices, add 1 to added
                            validIndices[added] = i;
                            added += 1;
                            int slot = playerTeam == 2 ? teamSlot : teamSlot + 5;
                            slot_to_steamid.put(slot, steamid);
                            steamid_to_slot.put(steamid, slot);
                            steamid_to_playerName.put(steamid, playerName);
                            System.out.printf("%s %s %s\n", slot, steamid, playerName);
                        }
                    } catch (Exception e) {
                        //swallow the exception when an unexpected number of players (!=10)
                        //System.err.println(e);
                    }

                    i += 1;
                }
                init = true;
            }
        }
    }

    private <T> T getEntityProperty(Entity e, String property, Integer idx) {
        try {
            if (e == null) {
                return null;
            }
            if (idx != null) {
                property = property.replace("%i", Util.arrayIdxToString(idx));
            }
            FieldPath fp = e.getDtClass().getFieldPathForName(property);
            return e.getPropertyForFieldPath(fp);
        }
        catch (Exception ex) {
            return null;
        }
    }

    private void processEntity(Context ctx, Entity e, boolean entityLeft)
    {
        //CDOTA_NPC_Observer_Ward
        //CDOTA_NPC_Observer_Ward_TrueSight
        //s1 "DT_DOTA_NPC_Observer_Ward"
        //s1 "DT_DOTA_NPC_Observer_Ward_TrueSight"
        boolean isObserver = e.getDtClass().getDtName().equals("CDOTA_NPC_Observer_Ward");
        boolean isSentry = e.getDtClass().getDtName().equals("CDOTA_NPC_Observer_Ward_TrueSight");
        if (isObserver || isSentry) {
            //System.err.println(e);
            Integer owner = getEntityProperty(e, "m_hOwnerEntity", null);
            Entity ownerEntity = ctx.getProcessor(Entities.class).getByHandle(owner);
            Integer slot = ownerEntity != null ? (Integer) getEntityProperty(ownerEntity, "m_iPlayerID", null) : null;
            Entry entry = new Entry(time, isObserver ? "obs" : "sen", slot, steamid_to_playerName.get(slot_to_steamid.get(slot)));
            Integer x = getEntityProperty(e, "CBodyComponent.m_cellX", null);
            Integer y = getEntityProperty(e, "CBodyComponent.m_cellY", null);
            Integer z = getEntityProperty(e, "CBodyComponent.m_cellZ", null);
            float vecX = getEntityProperty(e, "CBodyComponent.m_vecX", null);
            float vecY = getEntityProperty(e, "CBodyComponent.m_vecY", null);
            Integer[] pos = {x, y};
            entry.x = x;
            entry.y = y;
            entry.z = z;
            entry.vecX = vecX;
            entry.vecY = vecY;
            entry.key = Arrays.toString(pos);
            entry.entityleft = entityLeft;
            entry.ehandle = e.getHandle();
            //System.err.println(entry.key);

            //2/3 radiant/dire
            //entry.team = e.getProperty("m_iTeamNum");
            output(entry);
        }
    }
}