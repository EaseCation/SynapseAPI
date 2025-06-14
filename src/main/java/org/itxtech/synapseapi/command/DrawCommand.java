package org.itxtech.synapseapi.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandParser;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.exceptions.CommandSyntaxException;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.plugin.Plugin;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.multiprotocol.common.drawer.*;

import java.awt.*;
import java.util.List;

public class DrawCommand extends Command implements PluginIdentifiableCommand {
    private final Plugin plugin;

    public DrawCommand(Plugin plugin) {
        super("draw", "Debug drawer", "/draw <player: target> <shape: Shape> <location: x y z> [rotation: x y z] [scale: float] [timeLeft: float] [color: color] [...]");
        setPermission("synapseapi.command.draw");
        commandParameters.clear();
        commandParameters.put("line", new CommandParameter[]{
                CommandParameter.newType("player", CommandParamType.TARGET),
                CommandParameter.newEnum("shape", new CommandEnum("ShapeLine", "line")),
                CommandParameter.newType("location", CommandParamType.POSITION),
                CommandParameter.newType("rotation", true, CommandParamType.POSITION),
                CommandParameter.newType("scale", true, CommandParamType.FLOAT),
                CommandParameter.newType("timeLeft", true, CommandParamType.FLOAT),
                CommandParameter.newType("color", true, CommandParamType.STRING),
                CommandParameter.newType("endLocation", true, CommandParamType.POSITION),
        });
        commandParameters.put("box", new CommandParameter[]{
                CommandParameter.newType("player", CommandParamType.TARGET),
                CommandParameter.newEnum("shape", new CommandEnum("ShapeBox", "box")),
                CommandParameter.newType("location", CommandParamType.POSITION),
                CommandParameter.newType("rotation", true, CommandParamType.POSITION),
                CommandParameter.newType("scale", true, CommandParamType.FLOAT),
                CommandParameter.newType("timeLeft", true, CommandParamType.FLOAT),
                CommandParameter.newType("color", true, CommandParamType.STRING),
                CommandParameter.newType("bound", true, CommandParamType.POSITION),
        });
        commandParameters.put("shape", new CommandParameter[]{
                CommandParameter.newType("player", CommandParamType.TARGET),
                CommandParameter.newEnum("shape", new CommandEnum("Shape", "sphere", "circle")),
                CommandParameter.newType("location", CommandParamType.POSITION),
                CommandParameter.newType("rotation", true, CommandParamType.POSITION),
                CommandParameter.newType("scale", true, CommandParamType.FLOAT),
                CommandParameter.newType("timeLeft", true, CommandParamType.FLOAT),
                CommandParameter.newType("color", true, CommandParamType.STRING),
        });
        commandParameters.put("text", new CommandParameter[]{
                CommandParameter.newType("player", CommandParamType.TARGET),
                CommandParameter.newEnum("shape", new CommandEnum("ShapeText", "text")),
                CommandParameter.newType("location", CommandParamType.POSITION),
                CommandParameter.newType("rotation", true, CommandParamType.POSITION),
                CommandParameter.newType("scale", true, CommandParamType.FLOAT),
                CommandParameter.newType("timeLeft", true, CommandParamType.FLOAT),
                CommandParameter.newType("color", true, CommandParamType.STRING),
                CommandParameter.newType("text", true, CommandParamType.MESSAGE),
        });
        commandParameters.put("arrow", new CommandParameter[]{
                CommandParameter.newType("player", CommandParamType.TARGET),
                CommandParameter.newEnum("shape", new CommandEnum("ShapeArrow", "arrow")),
                CommandParameter.newType("location", CommandParamType.POSITION),
                CommandParameter.newType("rotation", true, CommandParamType.POSITION),
                CommandParameter.newType("scale", true, CommandParamType.FLOAT),
                CommandParameter.newType("timeLeft", true, CommandParamType.FLOAT),
                CommandParameter.newType("color", true, CommandParamType.STRING),
                CommandParameter.newType("endLocation", true, CommandParamType.POSITION),
                CommandParameter.newType("headLength", true, CommandParamType.FLOAT),
                CommandParameter.newType("headRadius", true, CommandParamType.FLOAT),
                CommandParameter.newType("headSegments", true, CommandParamType.INT),
        });
        commandParameters.put("clear", new CommandParameter[]{
                CommandParameter.newType("player", CommandParamType.TARGET),
                CommandParameter.newEnum("shape", new CommandEnum("ShapeClear", "clear")),
        });
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) {
            return false;
        }

        CommandParser parser = new CommandParser(this, sender, args);
        try {
            List<Player> players = parser.parseTargetPlayers();
            Shape type = parser.parseEnum(Shape.class);
            Vector3f location = type != Shape.CLEAR ? parser.parseVector3().asVector3f() : null;
            Vector3 rotation = parser.parseVector3OrDefault((Vector3) null);
            float scale = parser.parseFloatOrDefault(Float.NaN);
            float timeLeft = parser.parseFloatOrDefault(0, 0);
            Color color = parser.parseOrDefault((Color) null, Color::decode);

            org.itxtech.synapseapi.multiprotocol.common.drawer.Shape shape;
            switch (type) {
                case LINE -> {
                    Vector3 endLocation = parser.parseVector3OrDefault((Vector3) null);

                    Line line = new Line(location, endLocation == null ? location : endLocation.asVector3f());
                    shape = line;
                }
                case BOX -> {
                    Vector3 bound = parser.parseVector3OrDefault((Vector3) null);

                    Box box = new Box(location);
                    if (bound != null) {
                        box.bound = bound.asVector3f();
                    }
                    shape = box;
                }
                case SPHERE -> {
                    Sphere sphere = new Sphere(location);
                    shape = sphere;
                }
                case CIRCLE -> {
                    Circle circle = new Circle(location);
                    shape = circle;
                }
                case TEXT -> {
                    String text = parser.literalOrDefault((String) null);

                    Text txt = new Text(location, text);
                    shape = txt;
                }
                case ARROW -> {
                    Vector3 endLocation = parser.parseVector3OrDefault((Vector3) null);
                    float headLength = parser.parseFloatOrDefault(Float.NaN);
                    float headRadius = parser.parseFloatOrDefault(Float.NaN);
                    int headSegments = parser.parseIntOrDefault(-1, 0, 0xff);

                    Arrow arrow = new Arrow(location, endLocation == null ? location : endLocation.asVector3f());
                    if (!Float.isNaN(headLength)) {
                        arrow.headLength = headLength;
                    }
                    if (!Float.isNaN(headRadius)) {
                        arrow.headRadius = headRadius;
                    }
                    if (headSegments != -1) {
                        arrow.headSegments = headSegments;
                    }
                    shape = arrow;
                }
                case CLEAR -> {
                    for (Player player : players) {
                        if (player instanceof SynapsePlayer synapsePlayer) {
                            synapsePlayer.removeAllShapes();
                        }
                    }
                    sender.sendMessage("All shapes have been removed.");
                    return true;
                }
                default -> {
                    return true;
                }
            }
            if (rotation != null) {
                shape.rotation = rotation.asVector3f();
            }
            if (!Float.isNaN(scale)) {
                shape.scale = scale;
            }
            shape.totalTimeLeft = timeLeft;
            if (color != null) {
                shape.color = color;
            }
            for (Player player : players) {
                if (player instanceof SynapsePlayer synapsePlayer) {
                    synapsePlayer.addShape(shape);
                }
            }
            sender.sendMessage("The shape have been added: " + shape.id);
        } catch (CommandSyntaxException e) {
            sender.sendMessage(parser.getErrorMessage());
        }
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    private enum Shape {
        LINE,
        BOX,
        SPHERE,
        CIRCLE,
        TEXT,
        ARROW,
        CLEAR,
    }
}

/*
/draw @s line ~ ~ ~ 0 0 0 1 0 #00ffff ~3 ~3 ~3
/draw @s box ~ ~ ~ 0 0 0 1.1 0 #ff0000 3 3 3
/draw @s sphere ~ ~ ~ 0 0 0 3 0 #0000ff
/draw @s circle ~ ~ ~ 0 0 0 2 0 #ffff00
/draw @s text ~ ~ ~ 0 0 0 1 0 #ff00ff Text
/draw @s arrow ~ ~ ~ 0 0 0 1 0 #00ff00 ~-3 ~3 ~-3 0.5 0.25 4
/draw @s clear
*/
