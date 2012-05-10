/*
 * Copyright (C) 2011-2012 Keyle
 *
 * This file is part of MyWolf
 *
 * MyWolf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWolf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWolf. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyWolf.util;

import de.Keyle.MyWolf.MyWolfPlugin;
import de.Keyle.MyWolf.entity.types.InactiveMyPet;
import de.Keyle.MyWolf.entity.types.MyPet.PetState;
import de.Keyle.MyWolf.entity.types.wolf.MyWolf;
import de.Keyle.MyWolf.skill.MyWolfGenericSkill;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class MyWolfList
{
    private static final Map<OfflinePlayer, MyWolf> mActiveWolves = new HashMap<OfflinePlayer, MyWolf>();
    private static final List<MyWolf> lActiveWolves = new ArrayList<MyWolf>();

    private static final Map<OfflinePlayer, InactiveMyPet> mInctiveWolves = new HashMap<OfflinePlayer, InactiveMyPet>();
    private static final List<InactiveMyPet> lInactivePets = new ArrayList<InactiveMyPet>();

    // Active -------------------------------------------------------------------

    public static MyWolf getMyWolf(InactiveMyPet IMPet)
    {
        if (IMPet.getOwner().isOnline())
        {
            MyWolf AMWolf = new MyWolf(IMPet.getOwner());
            AMWolf.setHealth(IMPet.getHealth());
            AMWolf.setLocation(IMPet.getLocation());
            AMWolf.Name = IMPet.getName();
            AMWolf.RespawnTime = IMPet.getRespawnTime();

            if (AMWolf.RespawnTime > 0)
            {
                AMWolf.Status = PetState.Dead;
            }
            else
            {
                AMWolf.Status = PetState.Despawned;
            }

            AMWolf.Experience.setExp(IMPet.getExp());
            Collection<MyWolfGenericSkill> Skills = AMWolf.SkillSystem.getSkills();
            if (Skills.size() > 0)
            {
                for (MyWolfGenericSkill Skill : Skills)
                {
                    if (IMPet.getSkills().hasKey(Skill.getName()))
                    {
                        Skill.load(IMPet.getSkills().getCompound(Skill.getName()));
                    }
                }
            }
            return AMWolf;
        }
        return null;
    }

    public static void addMyWolf(MyWolf MW)
    {
        mActiveWolves.put(MW.getOwner(), MW);
        lActiveWolves.add(MW);
    }

    public static void removeMyWolf(MyWolf MW)
    {
        lActiveWolves.remove(MW);
        mActiveWolves.remove(MW.getOwner());
    }

    public static void removeMyWolf(OfflinePlayer Owner)
    {
        lActiveWolves.remove(mActiveWolves.get(Owner));
        mActiveWolves.remove(Owner);
    }

    public static MyWolf getMyWolf(int EntityID)
    {
        for (MyWolf wolf : lActiveWolves)
        {
            if (wolf.Status == PetState.Here && wolf.Wolf.getEntityId() == EntityID)
            {
                return wolf;
            }
        }
        return null;
    }

    public static MyWolf getMyWolf(OfflinePlayer owner)
    {
        if (mActiveWolves.containsKey(owner))
        {
            return mActiveWolves.get(owner);
        }
        return null;
    }

    public static List<MyWolf> getMyWolfList()
    {
        return lActiveWolves;
    }

    public static boolean hasMyWolf(OfflinePlayer player)
    {
        return mActiveWolves.containsKey(player);
    }

    public static boolean isMyWolf(int EnityID)
    {
        return getMyWolf(EnityID) != null;
    }

    // Inactive -----------------------------------------------------------------

    public static List<InactiveMyPet> getInactiveMyWolfList()
    {
        return lInactivePets;
    }

    public static boolean hasInactiveMyWolf(OfflinePlayer player)
    {
        return mInctiveWolves.containsKey(player);
    }

    public static InactiveMyPet getInactiveMyWolf(MyWolf AMWolf)
    {
        InactiveMyPet IAMPet = new InactiveMyPet(MyWolfPlugin.getPlugin().getServer().getOfflinePlayer(AMWolf.getOwner().getName()));
        IAMPet.setName(AMWolf.Name);
        IAMPet.setExp(AMWolf.Experience.getExp());
        IAMPet.setHealth(AMWolf.getHealth());
        IAMPet.setLocation(AMWolf.getLocation());
        IAMPet.setRespawnTime(AMWolf.RespawnTime);
        IAMPet.setSitting(IAMPet.isSitting());
        IAMPet.setSkills(AMWolf.SkillSystem.getSkills());

        return IAMPet;
    }

    public static InactiveMyPet getInactiveMyWolf(OfflinePlayer owner)
    {
        if (mInctiveWolves.containsKey(owner))
        {
            return mInctiveWolves.get(owner);
        }
        return null;
    }

    public static void removeInactiveMyWolf(InactiveMyPet IMPet)
    {
        mInctiveWolves.remove(IMPet.getOwner());
        lInactivePets.remove(IMPet);
    }

    public static void addInactiveMyWolf(InactiveMyPet IMPet)
    {
        mInctiveWolves.put(IMPet.getOwner(), IMPet);
        lInactivePets.add(IMPet);
    }

    // All ----------------------------------------------------------------------

    public static void setMyWolfActive(OfflinePlayer Owner, boolean activate)
    {
        MyWolfUtil.getDebugLogger().info("Set MyWolf active: " + activate);
        if (activate)
        {
            if (mInctiveWolves.containsKey(Owner) && mInctiveWolves.get(Owner).getOwner().isOnline())
            {
                InactiveMyPet IMPet = mInctiveWolves.get(Owner);
                MyWolf AMWolf = getMyWolf(IMPet);
                addMyWolf(AMWolf);
                removeInactiveMyWolf(IMPet);
                MyWolfUtil.getDebugLogger().info("   A: " + AMWolf);
                MyWolfUtil.getDebugLogger().info("   I: " + IMPet);
            }
        }
        else
        {
            if (mActiveWolves.containsKey(Owner))
            {
                MyWolf AMWolf = mActiveWolves.get(Owner);
                InactiveMyPet IMPet = getInactiveMyWolf(AMWolf);
                AMWolf.removeWolf();
                removeMyWolf(AMWolf);
                addInactiveMyWolf(IMPet);
                MyWolfUtil.getDebugLogger().info("   I: " + IMPet);
                MyWolfUtil.getDebugLogger().info("   A: " + AMWolf);
            }
        }
    }

    public static void clearList()
    {
        mActiveWolves.clear();
        lActiveWolves.clear();
        mInctiveWolves.clear();
        lInactivePets.clear();
    }

    public static int getMyWolfCount()
    {
        return mActiveWolves.size() + mInctiveWolves.size();
    }
}