package io.github.rypofalem.renewabledragoneggs;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class RenewableDragonEggPlugin extends JavaPlugin implements Listener {
	static final int INITIALDELAY = 5*20;
	static final int PERIODDELAY =  0; //needs to run each tick to prevent exploit
	static final int GIVEUPTIME = 60 * 20;
	public BukkitTask  bukkitTask;
	
	@Override
	 public void onEnable(){
		 getServer().getPluginManager().registerEvents(this, this);
	 }
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDragonDeath(EntityDeathEvent event){
		if(event.getEntityType() != EntityType.ENDER_DRAGON) return;
		if(event.getEntity().getWorld().getEnvironment() != Environment.THE_END) return;
		try{
			new EggTask((EnderDragon)event.getEntity()).runTaskTimer(this, INITIALDELAY, PERIODDELAY);
		} catch (Exception e){
			e.printStackTrace(); //expertly handled
		}

	}
	
	class EggTask extends BukkitRunnable{
		World world;
		EnderDragon dragon;
		int count = 0;
		
		EggTask(EnderDragon dragon){
			this.dragon = dragon;
			world = dragon.getWorld();
		}
		
		@Override
		public void run(){
			if(++count * (PERIODDELAY+1) >= GIVEUPTIME ){ //dragon is taking too long to die, something probably went wrong
				this.cancel();
				return;
			}
			
			for(Entity entity : world.getEntities()){
				if(entity == dragon){
					return;
				}
			}
			Block dragonEgg = world.getHighestBlockAt(0, 0);
			while(dragonEgg.getY() >= 0){ //Search down for end portal
				if(dragonEgg.getType() == Material.BEDROCK){ //Portal found, search up for air block
					while(dragonEgg.getY() < 255){
						dragonEgg = dragonEgg.getRelative(BlockFace.UP);
						if(dragonEgg.getType() == Material.DRAGON_EGG){
							//there is already an egg here, almost always from initial kill
							this.cancel();
							return;
						}
						if(dragonEgg.getType() == Material.AIR){
							dragonEgg.setType(Material.DRAGON_EGG);
							break;
						}
					}
					this.cancel();
					return;
				}
				dragonEgg = dragonEgg.getRelative(BlockFace.DOWN);
			}
		}
	}
}