package me.ShermansWorld.raidsperregion.util;

import java.util.UUID;

import org.bukkit.entity.Player;

import fr.mrmicky.fastboard.FastBoard;
import me.ShermansWorld.raidsperregion.raid.Raid;

public class ScoreboardUtil {
	
	
	public static FastBoard createNewScoreboard(Player player, Raid raid, String title) {
		FastBoard scoreboard = new FastBoard(player);
		scoreboard.updateTitle(title);
		raid.getScoreboards().put(player.getUniqueId(), scoreboard);
		return scoreboard;
	}
	
	public static void updateBoard(FastBoard board, String ... lines) {
		
		if (board == null || board.isDeleted()) {
			return;
		}
		
		// update colors
		for (int i = 0; i < lines.length; ++i ) {
			lines[i] = Helper.color(lines[i]);
		}
		
		board.updateLines(lines);
	}
	
	public static FastBoard getScoreboard(Player player, Raid raid) {
		return raid.getScoreboards().get(player.getUniqueId());
	}
	
	public static void clearScoreboards(Raid raid) {
		for (UUID uuid : raid.getScoreboards().keySet()) {
			try {
				raid.getScoreboards().get(uuid).delete();
			} catch(IllegalStateException e) {
			}
		}
	}
	
}
