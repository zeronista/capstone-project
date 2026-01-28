package com.g4.capstoneproject.config.thymeleaf;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Processor for <ui:card> element
 * 
 * Usage:
 * 
 * <pre>
 * {@code
 * <ui:card title="Card Title" class="custom-class">
 *     Card content goes here
 * </ui:card>
 * }
 * </pre>
 * 
 * Attributes:
 * - title: Optional card title
 * - class: Additional CSS classes
 * 
 * @author Development Team
 * @version 1.0.0
 * @since January 28, 2026
 */
public class UiCardElementProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "card";
    private static final int PRECEDENCE = 1000;

    public UiCardElementProcessor(String dialectPrefix) {
        super(
                TemplateMode.HTML,
                dialectPrefix,
                TAG_NAME,
                true,
                null,
                false,
                PRECEDENCE);
    }

    @Override
    protected void doProcess(
            ITemplateContext context,
            IProcessableElementTag tag,
            IElementTagStructureHandler structureHandler) {

        // Get attributes
        String title = tag.getAttributeValue("title");
        String customClass = tag.getAttributeValue("class");
        String baseClass = "bg-white rounded-2xl border border-surface-200 p-6";
        String fullClass = customClass != null ? baseClass + " " + customClass : baseClass;

        // Build the card HTML as a string
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"").append(fullClass).append("\">");

        // Add title if provided
        if (title != null && !title.isEmpty()) {
            html.append("<h3 class=\"text-lg font-heading font-semibold text-surface-900 mb-4\">")
                    .append(title)
                    .append("</h3>");
        }

        // Add content wrapper div
        html.append("<div>");

        // Set the body to process content and append closing tags
        structureHandler.replaceWith(html.toString(), true);
    }
}
