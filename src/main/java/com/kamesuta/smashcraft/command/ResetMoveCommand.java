package com.kamesuta.smashcraft.command;

import com.kamesuta.smashcraft.SmashCraft;
import dev.kotx.flylib.command.Command;
import dev.kotx.flylib.command.CommandContext;

public class ResetMoveCommand extends Command {
    public ResetMoveCommand() {
        super("reset-move");
    }

    @Override
    public void execute(CommandContext ctx) {
        //Bukkit.getServer().getScheduler().cancelTasks(SmashCraft.instance);
        SmashCraft.instance.velocityGroup++;
        ctx.success("ケツ量をリセットしました。");
    }
}