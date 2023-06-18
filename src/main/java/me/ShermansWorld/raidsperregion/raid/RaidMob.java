package me.ShermansWorld.raidsperregion.raid;

public class RaidMob {
	
	private String name;
	private int level;
	private int priority;
	private double chance;
	
	
	public RaidMob(String name, int priority, double chance) {
		this.name = name;
		this.priority = priority;
		this.chance = chance;
	}
	
	public RaidMob(String name, int priority, double chance, int level) {
		this.name = name;
		this.priority = priority;
		this.chance = chance;
		this.level = level;
	}
	
	// Getters and Setters
	
	public String getName() {
		return name;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public double getChance() {
		return chance;
	}
	
	
}
