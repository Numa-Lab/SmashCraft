package com.kamesuta.smashcraft.command;

import com.kamesuta.smashcraft.Config;
import dev.kotx.flylib.command.Command;
import dev.kotx.flylib.command.CommandContext;
import dev.kotx.flylib.command.arguments.StringArgument;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ConfigSetCommand extends Command {
    private final List<ConfigItem> configItemList = new ArrayList<>();

    public ConfigSetCommand() {
        super("set");

        try {
            for (Field field : Config.class.getDeclaredFields()) {
                if (!field.getName().equals("isEnabled")) {
                    configItemList.add(new ConfigItem(field));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        usage(builder -> {
            builder.stringArgument("configItem", StringArgument.Type.WORD, suggestionBuilder -> {
                        configItemList.forEach(x -> {
                            suggestionBuilder.suggest(x.name());
                        });
                    })
                    .stringArgument("value")
                    .executes(this::run);
        });
    }

    private void run(CommandContext ctx) {
        String itemName = ctx.getArgs().get(0);
        if (!isCollectItemName(itemName)) {
            ctx.fail(itemName + "は正しいコンフィグ名ではありません.");
            return;
        }

        String value = ctx.getArgs().get(1);
        ConfigItem configItem = configItemList.stream().filter(x -> x.name().equals(itemName)).findFirst().get();
        Object parsedValue = ArgumentType.valueOf(configItem.clazz).parse(value);

        if (parsedValue == null) {
            ctx.fail(value + "は不正な値です.");
            return;
        }

        configItem.setValue(null, parsedValue);
        ctx.success(itemName + "の値を" + parsedValue + "に設定しました.");
    }


    private boolean isCollectItemName(String itemName) {
        return configItemList.stream().anyMatch(x -> x.name().equals(itemName));
    }
}

class ConfigItem {
    Class clazz;
    Field field;

    public ConfigItem(Field field) {
        this.clazz = field.getType();
        this.field = field;
    }

    public String name() {
        return field.getName();
    }

    public Object getValue(Object obj) {
        Object value = null;
        try {
            value = field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    public void setValue(Object obj, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

enum ArgumentType {
    INTEGER(Integer.class, x -> {
        Integer value = null;
        try {
            value = Integer.parseInt(x);
        } catch (Exception ignore) {
        }

        return value;
    }),
    INT(int.class, x -> {
        Integer value = null;
        try {
            value = Integer.parseInt(x);
        } catch (Exception ignore) {
        }

        return value;
    }),
    WRAPPERDOUBLE(Double.class, x -> {
        Double value = null;
        try {
            value = Double.parseDouble(x);
        } catch (Exception ignore) {
        }

        return value;
    }),
    DOUBLE(double.class, x -> {
        Double value = null;
        try {
            value = Double.parseDouble(x);
        } catch (Exception ignore) {
        }

        return value;
    }),
    WRAPPERBOOLEAN(Boolean.class, x -> {
        Boolean value = null;
        try {
            value = Boolean.parseBoolean(x);
        } catch (Exception ignore) {
        }

        return value;
    }),
    BOOLEAN(boolean.class, x -> {
        Boolean value = null;
        try {
            value = Boolean.parseBoolean(x);
        } catch (Exception ignore) {
        }

        return value;
    }),
    STRING(String.class, x -> {
        return x;
    });

    private final Class clazz;
    private final Function parser;

    <R> ArgumentType(Class<R> clazz, Function<String, R> parser) {
        this.clazz = clazz;
        this.parser = parser;
    }

    public Object parse(String value) {
        return parser.apply(value);
    }

    public static ArgumentType valueOf(Class clazz) {
        for (ArgumentType argumentType : values()) {
            if (argumentType.clazz.equals(clazz)) {
                return argumentType;
            }
        }

        return null;
    }
}