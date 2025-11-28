package com.korber.inventoryservice.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Factory class that provides the appropriate inventory handler based on type
 */
@Component
@RequiredArgsConstructor
public class InventoryHandlerFactory {
    
    private final List<InventoryHandler> inventoryHandlers;
    private final Map<String, InventoryHandler> handlerMap = new HashMap<>();
    
    /**
     * Returns the appropriate inventory handler based on type
     * @param type The type of inventory handler to get
     * @return The inventory handler
     */
    public InventoryHandler getHandler(String type) {
        if (handlerMap.isEmpty()) {
            // Initialize the map if it's empty
            for (InventoryHandler handler : inventoryHandlers) {
                handlerMap.put(handler.getType(), handler);
            }
        }
        
        // Return the requested handler or the standard one if not found
        return Optional.ofNullable(handlerMap.get(type))
                .orElse(handlerMap.get("STANDARD"));
    }
    
    /**
     * Returns the default inventory handler
     * @return The default inventory handler
     */
    public InventoryHandler getDefaultHandler() {
        return getHandler("STANDARD");
    }
}
