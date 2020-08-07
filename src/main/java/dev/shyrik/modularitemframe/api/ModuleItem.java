package dev.shyrik.modularitemframe.api;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ModuleItem extends Item {
    private final Identifier moduleId;

    private static final Map<Identifier, Pair<ModuleItem, Class<? extends ModuleBase>>> MODULES = new HashMap<>();

    public ModuleItem(Settings props, Class<? extends ModuleBase> moduleClass, Identifier moduleId) {
        super(props);
        this.moduleId = moduleId;
        MODULES.put(moduleId, new Pair<>(this, moduleClass));
    }

    public ModuleBase createModule() {
        return createModule(moduleId);
    }

    public static ModuleBase createModule(Identifier id) {
        try {
            Optional<Pair<ModuleItem, Class<? extends ModuleBase>>> set = MODULES.keySet().stream().filter(r -> r.toString().equals(id.toString())).findAny().map(MODULES::get);
            if (set.isPresent()) {
                ModuleBase module = set.get().getRight().newInstance();
                module.parent = set.get().getLeft();
                return module;
            }
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }

        return null;
    }

    public static Set<Identifier> getModuleIds() {
        return MODULES.keySet();
    }
}
