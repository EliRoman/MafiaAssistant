/*
*   Authors: James Zygmont & Eli Roman
*   Company Name: Digital Mantis Games
*   Project: Mafia Party Assistant
*   Date: 12/18/2014
*/

package com.digitalmantis.mafiaassistant;

import java.math.BigInteger;
import java.util.Random;


public class Player {
	private String[] healthTypes = {"Alive", "Dead", "Hit"};
	private String healthStatus;
	private String role, name;
	private String playerID;
	
	Player(String role, String name){
		this.role = role;
		this.name = name;
		healthStatus = healthTypes[0];
		
		Random random = new Random();
		playerID = new BigInteger(130, random).toString(32);
	}

	public String[] getHealthTypes() {
		return healthTypes;
	}

	public void setHealthTypes(String[] healthTypes) {
		this.healthTypes = healthTypes;
	}

	public String getHealthStatus() {
		return healthStatus;
	}

	public void setHealthStatus(String healthStatus) {
		this.healthStatus = healthStatus;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlayerID() {
		return playerID;
	}

	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}	
	
}
