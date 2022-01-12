package com.kamesuta.smashcraft.command;

import com.kamesuta.smashcraft.SmashCraft;
import dev.kotx.flylib.command.Command;
import dev.kotx.flylib.command.CommandContext;

public class ResetCommand extends Command {
    public ResetCommand() {
        super("reset");
    }

    @Override
    public void execute(CommandContext ctx) {
        SmashCraft.instance.registerScoreboard();
        ctx.success("吹っ飛び率をリセットしました。");
    }
}