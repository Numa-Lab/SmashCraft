package com.kamesuta.smashcraft.command;

import com.kamesuta.smashcraft.Config;
import dev.kotx.flylib.command.Command;
import dev.kotx.flylib.command.CommandContext;

public class StopCommand extends Command {
    public StopCommand() {
        super("stop");
    }

    @Override
    public void execute(CommandContext ctx) {
        if (!Config.isEnabled) {
            ctx.fail("SmashCraftは既に無効です.");
            return;
        }

        Config.isEnabled = false;
        ctx.success("SmashCraftを無効化しました.");
    }
}