package com.kamesuta.smashcraft.command;

import com.kamesuta.smashcraft.Config;
import dev.kotx.flylib.command.Command;
import dev.kotx.flylib.command.CommandContext;

public class StartCommand extends Command {
    public StartCommand() {
        super("start");
    }

    @Override
    public void execute(CommandContext ctx) {
        if (Config.isEnabled) {
            ctx.fail("SmashCraftは既に有効です.");
            return;
        }

        Config.isEnabled = true;
        ctx.success("SmashCraftを有効化しました.");
    }
}