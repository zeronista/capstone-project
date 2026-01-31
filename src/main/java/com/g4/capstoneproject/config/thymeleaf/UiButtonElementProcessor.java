package com.g4.capstoneproject.config.thymeleaf;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Processor for <ui:button> element
 * 
 * Usage:
 * 
 * <pre>
 * {@code
 * <ui:button type="primary" text="Save" onclick="handleSave()" />
 * <ui:button type="secondary" text="Cancel" />
 * <ui:button type="danger" text="Delete" />
 * }
 * </pre>
 * 
 * Attributes:
 * - type: Button variant (primary, secondary, danger) - default: primary
 * - text: Button text - required
 * - onclick: JavaScript onclick handler
 * - disabled: Whether button is disabled
 * 
 * @author Development Team
 * @version 1.0.0
 * @since January 28, 2026
 */
public class UiButtonElementProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "button";
    private static final int PRECEDENCE = 1000;

    public UiButtonElementProcessor(String dialectPrefix) {
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
        String type = tag.getAttributeValue("type");
        String text = tag.getAttributeValue("text");
        String onclick = tag.getAttributeValue("onclick");
        String disabled = tag.getAttributeValue("disabled");

        if (type == null)
            type = "primary";
        if (text == null)
            text = "Button";

        // Determine button class based on type
        String buttonClass;
        switch (type.toLowerCase()) {
            case "secondary":
                buttonClass = "btn-secondary inline-flex items-center gap-2 px-4 py-2.5 border border-surface-200 rounded-xl text-surface-600 hover:bg-surface-50 transition-colors duration-200";
                break;
            case "danger":
                buttonClass = "inline-flex items-center gap-2 px-5 py-2.5 bg-error-500 text-white font-medium rounded-xl hover:bg-error-600 transition-colors duration-200 cursor-pointer shadow-lg shadow-error-500/25";
                break;
            case "primary":
            default:
                buttonClass = "btn-primary inline-flex items-center gap-2 px-5 py-2.5 bg-primary-500 text-white font-medium rounded-xl hover:bg-primary-600 transition-colors duration-200 cursor-pointer shadow-lg shadow-primary-500/25";
                break;
        }

        boolean isDisabled = disabled != null && (disabled.equals("true") || disabled.equals("disabled"));
        if (isDisabled) {
            buttonClass += " disabled:bg-surface-400 disabled:cursor-not-allowed disabled:shadow-none";
        }

        // Build button HTML
        StringBuilder html = new StringBuilder();
        html.append("<button type=\"button\" class=\"").append(buttonClass).append("\"");

        if (onclick != null) {
            html.append(" onclick=\"").append(onclick).append("\"");
        }

        if (isDisabled) {
            html.append(" disabled=\"disabled\"");
        }

        html.append(">").append(text).append("</button>");

        // Replace the tag with our generated HTML
        structureHandler.replaceWith(html.toString(), false);
    }
}
