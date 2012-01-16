package com.manymonkeys.controls;

import org.apache.click.Control;
import org.apache.click.control.*;
import org.apache.click.util.ContainerUtils;
import org.apache.click.util.HtmlStringBuffer;

import java.util.List;
import java.util.Map;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class BootstrapForm extends Form {

    public BootstrapForm(String name) {
        super(name);
    }

    @Override
    public void render(HtmlStringBuffer buffer) {

        final boolean process =
            getContext().getRequest().getMethod().equalsIgnoreCase(getMethod());

        List<Field> formFields = ContainerUtils.getInputFields(this);

        renderHeader(buffer, formFields);

        buffer.append("<fieldset>");

        // Render fields, errors and buttons
        if (POSITION_TOP.equals(getErrorsPosition())) {
            renderErrors(buffer, process);
            renderFields(buffer);
            renderButtons(buffer);

        } else if (POSITION_MIDDLE.equals(getErrorsPosition())) {
            renderFields(buffer);
            renderErrors(buffer, process);
            renderButtons(buffer);

        } else if (POSITION_BOTTOM.equals(getErrorsPosition())) {
            renderFields(buffer);
            renderButtons(buffer);
            renderErrors(buffer, process);

        } else {
            String msg = "Invalid errorsPosition:" + getErrorsPosition();
            throw new IllegalArgumentException(msg);
        }

        buffer.append("<fieldset>\n");

        renderTagEnd(formFields, buffer);
    }

    @Override
    protected void renderButtons(HtmlStringBuffer buffer) {

        List<Button> buttons = getButtonList();
        if (!buttons.isEmpty()) {
            buffer.append("<div class=\"actions\">");
            for (Button button : buttons) {
                button.addStyleClass("btn");
                if (button instanceof Submit) {
                    button.addStyleClass("primary");
                }
                button.render(buffer);
                buffer.append(" ");
            }
            buffer.append("</div>\n");
        }

    }

    @Override
    protected void renderFields(HtmlStringBuffer buffer) {

        // If Form contains only the FORM_NAME HiddenField, exit early
        if (getControls().size() == 1) {

            // getControlMap is cheaper than getFieldMap, so check that first
            if (getControlMap().containsKey(FORM_NAME)) {
                return;

            } else {
                Map<String, Field> fieldMap = ContainerUtils.getFieldMap(this);
                if (fieldMap.containsKey(FORM_NAME)) {
                    return;
                }
            }
        }

        renderControls(buffer, this, getControls(), getFieldWidths(), getColumns());

    }

    @Override
    protected void renderErrors(HtmlStringBuffer buffer, boolean processed) {
        if (processed && !isValid()) {

            if (getError() != null) {
                buffer.append("<div class=\"alert-message error\">\n");
                buffer.append("<a class=\"close\" href=\"#\">×</a>\n");
                buffer.append("<p>");
                buffer.append(getError());
                buffer.append("</p>\n");
                buffer.append("</div>\n");
            }

//            for (Field field : getErrorFields()) {
//
//                // Certain fields might be invalid because
//                // one of their contained fields are invalid. However these
//                // fields might not have an error message to display.
//                // If the outer field's error message is null don't render.
//                if (field.getError() == null) {
//                    continue;
//                }
//
//                buffer.append("<div class=\"alert-message error\">\n");
//                buffer.append("<a class=\"close\" href=\"#\">×</a>\n");
//                buffer.append("<p>");
//                buffer.append(field.getError());
//                buffer.append("</p>\n");
//                buffer.append("</div>\n");
//
//            }

        }
    }

    @Override
    protected void renderControls(HtmlStringBuffer buffer, Container container,
            List<Control> controls, Map<String, Integer> fieldWidths, int columns) {

        for (Control control : controls) {

            // Buttons are rendered separately
            if (control instanceof Button) {
                continue;
            }

            if (!isHidden(control)) {

                // Control width
                Integer width = fieldWidths.get(control.getName());

                if (control instanceof FieldSet) {

                    control.render(buffer); //TODO WTF?

                } else if (control instanceof Label) {

                    Label label = (Label) control;
                    buffer.append("<legend align=\"");

                    String cellStyleClass = label.getParentStyleClassHint();
                    if (cellStyleClass != null) {
                        buffer.append(" ");
                        buffer.append(cellStyleClass);
                    }
                    buffer.append("\"");

                    buffer.appendAttribute("style", label.getParentStyleHint());

                    if (label.hasAttributes()) {
                        Map<String, String> labelAttributes = label.getAttributes();
                        for (Map.Entry<String, String> entry : labelAttributes.entrySet()) {
                            String labelAttrName = entry.getKey();
                            if (!labelAttrName.equals("id") && !labelAttrName.equals("style")) {
                                buffer.appendAttributeEscaped(labelAttrName, entry.getValue());
                            }
                        }
                    }
                    buffer.append(">");
                    label.render(buffer);
                    buffer.append("</legend>\n");

                } else if (control instanceof Field) {

                    Field field = (Field) control;
                    
                    String fieldId = field.getId();
                    String fieldLabel = field.getLabel();
                    String fieldError = field.getError();
                    boolean isRequired = field.isRequired();

                    buffer.append("<div class=\"clearfix");
                    if (fieldError != null) {
                        buffer.append(" error");
                    }
                    buffer.append("\">\n");
                    if (fieldId != null && fieldLabel != null) {
                        buffer.append("<label for=\"");
                        buffer.append(fieldId);
                        buffer.append("\">");
                        buffer.append(fieldLabel);
                        buffer.append("</label>\n");

                        buffer.append("<div class=\"input\">\n");

                        field.addStyleClass("xlarge");
                        field.render(buffer);
                        
                        if (fieldError != null) {
                            buffer.append("<span class=\"help-inline\">");
                            buffer.append(fieldError);
                            buffer.append("</span>\n");
                        }
                        if (isRequired) {
                            buffer.append("<span class=\"help-block\">required</span>");
                        }

                        buffer.append("</div>\n");
                    }
                    buffer.append("</div>\n");

                } else {

                    control.render(buffer);

                }

            }
        }

    }

    /**
     * Overriding parent private
     */
    private boolean isHidden(Control control) {
        if (!(control instanceof Field)) {
            // Non-Field Controls can not be hidden
            return false;
        } else {
            return ((Field) control).isHidden();
        }
    }


}
