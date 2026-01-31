package com.g4.capstoneproject.config.thymeleaf;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Processor for <ui:badge> element
 * 
 * Usage:
 * 
 * <pre>
 * {@code
 * <ui:badge variant="success" text="Active" />
 * <ui:badge variant="warning" text="Pending" />
 * <ui:badge variant="error" text="Failed" />
 * }
 * </pre>
 * 
 * Attributes:
 * - variant: Badge color (primary, secondary, success, warning, error) -
 * default: primary
 * - text: Badge text - required
 * 
 * @author Development Team
 * @version 1.0.0
 * @since January 28, 2026
 */
public class UiBadgeElementProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "badge";
    private static final int PRECEDENCE = 1000;

    public UiBadgeElementProcessor(String dialectPrefix) {
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
        String variant = tag.getAttributeValue("variant");
        String text = tag.getAttributeValue("text");

        if (variant == null)
            variant = "primary";
        if (text == null)
            text = "";

        // Determine badge class based on variant
        String badgeClass = "badge inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ";
        switch (variant.toLowerCase()) {
            case "secondary":
                badgeClass += "bg-secondary-100 text-secondary-700";
                break;
            case "success":
                badgeClass += "bg-success-100 text-success-700";
                break;
            case "warning":
                badgeClass += "bg-warning-100 text-warning-700";
                break;
            case "error":
                badgeClass += "bg-error-100 text-error-700";
                break;
            case "primary":
            default:
                badgeClass += "bg-primary-100 text-primary-700";
                break;
        }

        // Build badge HTML
        String html = "<span class=\"" + badgeClass + "\">" + text + "</span>";

        // Replace the tag with our generated HTML
        structureHandler.replaceWith(html, false);
    }
}
