package com.g4.capstoneproject.config.thymeleaf;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Processor for <ui:alert> element
 * 
 * Usage:
 * 
 * <pre>
 * {@code
 * <ui:alert type="success" message=
"Operation completed successfully!" dismissible="true" />
 * <ui:alert type="error" message="An error occurred." />
 * <ui:alert type="warning" message="Please review your settings." />
 * }
 * </pre>
 * 
 * Attributes:
 * - type: Alert type (info, success, warning, error) - default: info
 * - message: Alert message - required
 * - dismissible: Whether alert can be dismissed (true/false) - default: false
 * 
 * @author Development Team
 * @version 1.0.0
 * @since January 28, 2026
 */
public class UiAlertElementProcessor extends AbstractElementTagProcessor {

    private static final String TAG_NAME = "alert";
    private static final int PRECEDENCE = 1000;

    public UiAlertElementProcessor(String dialectPrefix) {
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
        String message = tag.getAttributeValue("message");
        String dismissible = tag.getAttributeValue("dismissible");

        if (type == null)
            type = "info";
        if (message == null)
            message = "";
        boolean isDismissible = "true".equalsIgnoreCase(dismissible);

        // Determine alert class and icon based on type
        String alertClass = "flex items-center gap-3 px-4 py-3 rounded-xl ";
        String iconPath;

        switch (type.toLowerCase()) {
            case "success":
                alertClass += "bg-success-50 border border-success-200 text-success-700";
                iconPath = "M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z";
                break;
            case "warning":
                alertClass += "bg-warning-50 border border-warning-200 text-warning-700";
                iconPath = "M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z";
                break;
            case "error":
                alertClass += "bg-error-50 border border-error-200 text-error-700";
                iconPath = "M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z";
                break;
            case "info":
            default:
                alertClass += "bg-info-50 border border-info-200 text-info-700";
                iconPath = "M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z";
                break;
        }

        // Build alert HTML
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"").append(alertClass).append("\">")
                .append("<svg class=\"w-5 h-5 flex-shrink-0\" fill=\"currentColor\" viewBox=\"0 0 20 20\">")
                .append("<path fill-rule=\"evenodd\" d=\"").append(iconPath).append("\" clip-rule=\"evenodd\"/>")
                .append("</svg>")
                .append("<p class=\"flex-1 text-sm font-medium\">").append(message).append("</p>");

        // Add dismiss button if dismissible
        if (isDismissible) {
            html.append(
                    "<button onclick=\"this.parentElement.remove()\" class=\"flex-shrink-0 hover:opacity-70 transition-opacity\">")
                    .append("<svg class=\"w-4 h-4\" fill=\"currentColor\" viewBox=\"0 0 20 20\">")
                    .append("<path fill-rule=\"evenodd\" d=\"M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z\" clip-rule=\"evenodd\"/>")
                    .append("</svg>")
                    .append("</button>");
        }

        html.append("</div>");

        // Replace the tag with our generated HTML
        structureHandler.replaceWith(html.toString(), false);
    }
}
