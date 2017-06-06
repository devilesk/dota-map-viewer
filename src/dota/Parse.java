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

public class Parse {

    private Integer time = 0;
    final List<Entry> wards = new ArrayList<>();
    Integer gameStartTime = 0;
    Integer gameEndTime = 0;

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
                System.out.printf("ward expire (%s, %s) %s\n", e.x, e.y, e.ehandle);
                o.expireTime = e.time;
            }
            else {
                System.out.printf("ward exists (%s, %s) %s\n", e.x, e.y, e.ehandle);
            }
        }
        else {
            System.out.printf("ward add (%s, %s) %s\n", e.x, e.y, e.ehandle);
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
            Entry entry = new Entry(time);
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
            entry.type = isObserver ? "obs" : "sen";
            entry.key = Arrays.toString(pos);
            entry.entityleft = entityLeft;
            entry.ehandle = e.getHandle();
            //System.err.println(entry.key);
            Integer owner = getEntityProperty(e, "m_hOwnerEntity", null);
            Entity ownerEntity = ctx.getProcessor(Entities.class).getByHandle(owner);
            entry.slot = ownerEntity != null ? (Integer) getEntityProperty(ownerEntity, "m_iPlayerID", null) : null;
            //2/3 radiant/dire
            //entry.team = e.getProperty("m_iTeamNum");
            output(entry);
        }
    }
}