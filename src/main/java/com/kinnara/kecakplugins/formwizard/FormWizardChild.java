package com.kinnara.kecakplugins.formwizard;

import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.HiddenField;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.beans.BeansException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Yonathan
 */
public class FormWizardChild extends AbstractSubForm {
    private boolean skipFormatData = false;
    
    @Override
    public String getName() {
        return AppPluginUtil.getMessage("formWizard.title", getClassName(), "/message/formWizard") + " - Child Page";
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }
    
    @Override
    public Collection<Element> getChildren(@Nonnull FormData formData) {
        Collection<Element> children = super.getChildren();
        if ((children == null || children.isEmpty()) && loadChild(formData)) {
            children = new ArrayList<>();
            Form childForm = loadChildForm(formData);
            if (childForm != null) {
                children.add(childForm);
                setChildren(children);
            }
        }
        return children;
    }
    
    protected Form loadChildForm(@Nonnull FormData formData) throws BeansException {
        Form childForm = null;
        FormService formService = (FormService)FormUtil.getApplicationContext().getBean("formService");
        String json = "";
        String formDefId = getPropertyString("formDefId");
        if (formDefId != null && !formDefId.isEmpty()) {
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            FormDefinitionDao formDefinitionDao = (FormDefinitionDao)FormUtil.getApplicationContext().getBean("formDefinitionDao");
            FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDef);
            if (formDef != null) {
                json = formDef.getJson();
            }
        }
        if (json != null && json.trim().length() > 0) {
            if (formData != null && formData.getProcessId() != null && !formData.getProcessId().isEmpty()) {
                WorkflowManager wm = (WorkflowManager)AppUtil.getApplicationContext().getBean("workflowManager");
                WorkflowAssignment wfAssignment = wm.getAssignmentByProcess(formData.getProcessId());
                json = AppUtil.processHashVariable(json, wfAssignment, "json", null);
            }
            try {
                childForm = (Form)formService.createElementFromJson(json);
                childForm.setParent(this);
                if (!(childForm.getLoadBinder() == null || formData.getLoadBinderData(childForm) != null || getPrimaryKeyValue(formData) == null || getPropertyString("pageNum").equals("1") && getPrimaryKeyValue(formData).isEmpty())) {
                    childForm = formService.loadFormData(childForm, formData);
                } else {
                    formData = formService.executeFormOptionsBinders((Element)childForm, formData);
                }
                Element idElement = FormUtil.findElement("id", childForm, formData);
                if (idElement == null) {
                    Collection subFormElements = childForm.getChildren();
                    idElement = new HiddenField();
                    idElement.setProperty("id", "id");
                    idElement.setParent(childForm);
                    subFormElements.add(idElement);
                }
            }
            catch (Exception e) {
                LogUtil.error(FormWizardChild.class.getName(), e, e.getMessage());
            }
        }
        if (childForm != null) {
            String parentId = FormUtil.getElementParameterName(this);
            updateElementParameterNames(childForm, parentId);
        }
        return childForm;
    }
    
    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String html = "";
        FormWizard parent = (FormWizard)getParent();
        int cPageNum = parent.getCurrentPageNumber(formData);
        html = getPropertyString("pageNum").equals(Integer.toString(cPageNum)) ? getHtml(formData, dataModel) : getDataHtml(formData, dataModel);
        return html;
    }

    protected String getHtml(FormData formData, Map dataModel) {
        String elementMetaData = (Boolean) dataModel.get("includeMetaData") ? FormUtil.generateElementMetaData(this) : "";
        Form childForm = getSubForm(formData);
        String readonly = "true".equalsIgnoreCase(getPropertyString("readonly")) ? " readonly" : "";
        String html = "<div class='subform-cell' " + elementMetaData + "><div class='mpf-container" + readonly + "'>";
        if (childForm != null) {
            String subFormHtml = childForm.render(formData, Boolean.FALSE);
            subFormHtml = subFormHtml.replaceAll("\"form-section", "\"subform-section");
            subFormHtml = subFormHtml.replaceAll("\"form-column", "\"subform-column");
            subFormHtml = subFormHtml.replaceAll("\"form-cell", "\"subform-cell");
            html = html + subFormHtml;
        } else {
            html = html + "Child Form could not be loaded";
        }
        html = html + "<div style='clear:both;'></div></div></div>";
        return html;
    }

    protected String getDataHtml(FormData formData, Map dataModel) {
        String html = "";
        String paramName = FormUtil.getElementParameterName(this);
        Map params = formData.getRequestParams();
        String disabled = "";
        FormWizard parent = (FormWizard)getParent();
        if ("true".equalsIgnoreCase(parent.getPropertyString("partiallyStore"))) {
            disabled = " disabled=\"disabled\"";
        }
        for (Object key : params.keySet()) {
            String[] paramValues;
            String strKey = (String) key;
            if (!strKey.startsWith(paramName)) continue;
            for (String value : paramValues = (String[])params.get(strKey)) {
                html = html + "<input type=\"hidden\" " + disabled + " name=\"" + strKey + "\" value=\"" + value.replaceAll("\"", "&quot;") + "\" />";
            }
        }
        if (html.isEmpty()) {
            html = html + "<input type=\"hidden\" name=\"_" + paramName + "__NO_DATA_LOADED\" value=\"true\" />";
        }
        return html;
    }

    protected boolean loadChild(FormData formData) {
        FormWizard parent = (FormWizard)getParent();
        int cPageNum = parent.getCurrentPageNumber(formData);
        if (getPropertyString("pageNum").equals(Integer.toString(cPageNum))) {
            return true;
        }
        if (FormUtil.isFormSubmitted(this, formData) && !parent.isActive(FormUtil.findRootForm(parent), formData)) {
            return true;
        }
        return formData.getFormResult("FORM_RESULT_LOAD_ALL_DATA") != null;
    }

    @Override
    public String getPrimaryKeyValue(FormData formData) {
        String primaryKeyValue = null;
        String parentSubFormId = getPropertyString("parentSubFormId");
        if (parentSubFormId != null && !parentSubFormId.isEmpty()) {
            Form rootForm = FormUtil.findRootForm(this);
            Element foreignKeyElement = FormUtil.findElement(parentSubFormId, rootForm, formData);
            if (foreignKeyElement != null) {
                primaryKeyValue = FormUtil.getElementPropertyValue(foreignKeyElement, formData);
            }
        } else {
            primaryKeyValue = super.getPrimaryKeyValue(formData);
        }
        return primaryKeyValue;
    }

    @Override
    protected Form getSubForm(FormData formData) {
        Collection<Element> children = getChildren(formData);
        if (children != null && !children.isEmpty()) {
            return (Form)getChildren().iterator().next();
        }
        return null;
    }

    @Override
    public boolean continueValidation(FormData formData) {
        return !("true".equals(getParent().getPropertyString("changePage")) && !"true".equals(getPropertyString("validate")) || "true".equals(getPropertyString("readonly")) || skipFormatData || formData.getFormResult("_PREVIEW_MODE") != null);
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = super.formatData(formData);
        FormWizard parent = (FormWizard)getParent();
        Integer cPageNum = parent.getCurrentPageNumber(formData);
        if (!"true".equals(getParent().getPropertyString("changePage")) && "true".equals(getParent().getPropertyString("partiallyStore")) && !getPropertyString("pageNum").equals(Integer.toString(cPageNum))) {
            skipFormatData = true;
            setProperty("readonly", "true");
        }
        return rowSet;
    }

    @Override
    public Boolean hasError(FormData formData) {
        String paramName = FormUtil.getElementParameterName(this);
        Map errors = formData.getFormErrors();
        for (Object error : errors.keySet()) {
            if (!((String)error).startsWith(paramName + "_")) continue;
            return true;
        }
        return false;
    }
    
}
