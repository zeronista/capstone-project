package com.g4.capstoneproject.config.thymeleaf;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom Thymeleaf Dialect for UI Components
 * 
 * This dialect provides custom tags and attributes for common UI components,
 * making templates cleaner and more maintainable.
 * 
 * Prefix: "ui"
 * 
 * Usage examples:
 * 
 * <pre>
 * {@code
 * <ui:card title="Dashboard Stats" />
 * <ui:button type="primary" text="Save Changes" />
 * <ui:badge variant="success" text="Active" />
 * }
 * </pre>
 * 
 * @author Development Team
 * @version 1.0.0
 * @since January 28, 2026
 */
public class UiDialect extends AbstractProcessorDialect {

    private static final String DIALECT_NAME = "UI Component Dialect";
    private static final String PREFIX = "ui";
    private static final int PROCESSOR_PRECEDENCE = 1000;

    /**
     * Constructor for the UI Dialect
     */
    public UiDialect() {
        super(DIALECT_NAME, PREFIX, PROCESSOR_PRECEDENCE);
    }

    /**
     * Get all custom processors (tag and attribute processors)
     * 
     * @param dialectPrefix the dialect prefix ("ui")
     * @return Set of custom processors
     */
    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        Set<IProcessor> processors = new HashSet<>();

        // Add element (tag) processors
        processors.add(new UiCardElementProcessor(dialectPrefix));
        processors.add(new UiButtonElementProcessor(dialectPrefix));
        processors.add(new UiBadgeElementProcessor(dialectPrefix));
        processors.add(new UiAlertElementProcessor(dialectPrefix));

        return processors;
    }
}
