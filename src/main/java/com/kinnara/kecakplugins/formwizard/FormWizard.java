/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kinnara.kecakplugins.formwizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.SubForm;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormButton;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreBinder;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.util.WorkflowUtil;

/**
 *
 * @author Yonathan
 */
public class FormWizard extends FormButton implements FormBuilderPaletteElement, PluginWebSupport, FormContainer {

    private int currentPageNumber;
    private boolean partiallyStoreError = false;

    public String getName() {
        return "Form Wizards";
    }

    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    public String getDescription() {
        return "Form with wizard element";
    }

    public String getLabel() {
        return "Form Wizard";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource((String)this.getClass().getName(), (String) "/properties/formWizard.json", (Object[])null, (boolean)true, null);
    }

    public String getFormBuilderCategory() {
        return "Kecak";
    }

    public int getFormBuilderPosition() {
        return 300;
    }

    public String getFormBuilderIcon() {
        return "/plugin/" + SubForm.class.getName() + "/images/subForm_icon.gif";
    }

    public String getFormBuilderTemplate() {
        return "<label class='label'>Form Wizard</label>";
    }

    @Override
    public Collection<Element> getChildren(FormData formData) {
        Collection children = super.getChildren();
        if (formData != null && (children == null || children.isEmpty())) {
            LogUtil.info(this.getClass().getName(), "[GET CHILDREN] ");
            Object numberOfPageObject = this.getProperty("numberOfPage");
            if (numberOfPageObject instanceof Map) {
                Map temp = (Map) numberOfPageObject;
                int numberOfPage = Integer.parseInt(temp.get("className").toString());
                Map pagesProperties = (Map) temp.get("properties");
                FormWizardKeyContainer container = new FormWizardKeyContainer();
                container.setParent((Element) this);
                int pageCount = 0;
                for (int page = 1; page <= numberOfPage; ++page) {
                    String label = (String) pagesProperties.get("page" + page + "_label");
                    String formDefId = (String) pagesProperties.get("page" + page + "_formDefId");
                    String readonly = (String) pagesProperties.get("page" + page + "_readonly");
                    String parentSubFormId = (String) pagesProperties.get("page" + page + "_parentSubFormId");
                    String subFormParentId = (String) pagesProperties.get("page" + page + "_subFormParentId");
                    String validate = (String) pagesProperties.get("page" + page + "_validate");
                    if (!this.checkForRecursiveForm((Element) this, formDefId)) {
                        continue;
                    }
                    FormWizardChild pageElement = this.getChildPage(label, formDefId, readonly, parentSubFormId, subFormParentId, validate, Integer.toString(++pageCount));
                    children.add(pageElement);
                    container.addKeyElement(parentSubFormId, formData);
                }
                children.add(container);
                this.setProperty("totalPage", Integer.toString(pageCount));
            } else {
                this.setProperty("totalPage", "0");
            }
        }
        return children;
    }

    @Override
    public boolean continueValidation(FormData formData) {
        return true;
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        Integer totalPage;
        Integer pageNum;
        Collection<Element> childs;
        String paramName;
        Iterator i$;
        boolean hasError;
        paramName = FormUtil.getElementParameterName((Element) this);
        pageNum = this.getCurrentPageNumber(formData);
        LogUtil.info(this.getClass().getName(), "[PAGE NUMBER] "+pageNum);
        totalPage = Integer.parseInt((String) this.getProperty("totalPage"));
        hasError = true;
        if ("true".equals(this.getPropertyString("child_validate_page_" + this.currentPageNumber))) {
            childs = this.getChildren(formData);
            i$ = childs.iterator();
            while (i$.hasNext()) {
                Element c = (Element) i$.next();
                if (!c.getPropertyString("pageNum").equals(String.valueOf(currentPageNumber))) {
                    continue;
                }
                if (!c.hasError(formData).booleanValue() && !this.partiallyStoreError) {
                    hasError = false;
                }
                break;
            }
        } else {
            hasError = false;
        }
        if (!hasError && formData.getRequestParameter(paramName + "_next_page") != null) {
            pageNum = pageNum + 1;
            if (pageNum <= totalPage) {
                this.currentPageNumber = pageNum;
            }
        } else if (!hasError && formData.getRequestParameter(paramName + "_prev_page") != null) {
            pageNum = pageNum - 1;
            if (pageNum > 0) {
                this.currentPageNumber = pageNum;
            }
        } else if (!hasError && formData.getRequestParameter(paramName + "_change_page") != null && !formData.getRequestParameter(paramName + "_change_page").isEmpty()) {
            this.currentPageNumber = Integer.parseInt(formData.getRequestParameter(paramName + "_change_page"));
        }
        dataModel.put("cPageNum", Integer.toString(this.getCurrentPageNumber(formData)));
        if ("true".equals(this.getPropertyString("onlyAllowSubmitOnLastPage")) && totalPage != this.getCurrentPageNumber(formData)) {
            this.hideParentFormButton(null);
        }
        String template = "formWizard.ftl";
        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    public FormWizardChild getChildPage(String label, String formDefId, String readonly, String parentSubFormId, String subFormParentId, String validate, String pageNum) {
        FormWizardChild page = new FormWizardChild();
        page.setProperty("label", label);
        page.setProperty("formDefId", formDefId);
        page.setProperty("readonly", readonly);
        page.setProperty("parentSubFormId", parentSubFormId);
        page.setProperty("subFormParentId", subFormParentId);
        page.setProperty("validate", validate);
        this.setProperty("child_validate_page_" + pageNum, validate);
        page.setProperty("pageNum", pageNum);
        page.setProperty("id", this.getPropertyString("id") + "_" + pageNum);
        page.setParent((Element) this);
        return page;
    }

    protected boolean checkForRecursiveForm(Element e, String id) {
        Form form = FormUtil.findRootForm((Element) e);
        if (form != null && form != e) {
            String formId = form.getPropertyString("id");
            if (id.equals(formId)) {
                return false;
            }
            return this.checkForRecursiveForm((Element) form, id);
        }
        return true;
    }

    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isAdmin = WorkflowUtil.isCurrentUserInRole("ROLE_ADMIN");
        if (!isAdmin) {
            response.sendError(401);
            return;
        }
        String action = request.getParameter("action");
        if ("getJson".equals(action)) {
            String value = request.getParameter("value");
            int number = Integer.parseInt(value);
            String output = "[";
            for (int i = 1; i <= number; ++i) {
                String pageNumber = Integer.toString(i);
                Object[] arguments = new String[]{pageNumber, pageNumber, pageNumber, pageNumber, pageNumber, pageNumber, pageNumber, pageNumber, pageNumber};
                output = output + AppUtil.readPluginResource((String) this.getClass().getName(), "/properties/form/formWizardChild.json", arguments, true, "message/formWizard") + ",";
            }
            output = output.substring(0, output.length() - 1) + "]";
            response.getWriter().write(output);
        } else {
            response.setStatus(204);
        }
    }

    public FormData actionPerformed(Form form, FormData formData) {
        formData = FormUtil.executeElementFormatDataForValidation((Element) form, (FormData) formData);
        FormUtil.executeValidators((Element) this, (FormData) formData);
        if ("true".equalsIgnoreCase(this.getPropertyString("partiallyStore")) && formData.getFormResult("_PREVIEW_MODE") == null) {
            this.partiallyStoreError = false;
            Integer pageNum = this.getCurrentPageNumber(formData);
            for (Element e : this.getChildren(formData)) {
                if (!pageNum.toString().equals(e.getPropertyString("pageNum")) || e.hasError(formData)) {
                    continue;
                }
                formData = FormUtil.executeElementFormatData((Element) form, (FormData) formData);
                try {
                    if ("true".equalsIgnoreCase(this.getPropertyString("storeMainFormOnPartiallyStore"))) {
                        FormStoreBinder binder = form.getStoreBinder();
                        FormRowSet rows = formData.getStoreBinderData(binder);
                        binder.store((Element) form, rows, formData);
                    }
                    FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");
                    formData = formService.storeElementData(form, e, formData);
                    e.setProperty("validate", (Object) "true");
                } catch (Exception ex) {
                    String formId = FormUtil.getElementParameterName((Element) form);
                    formData.addFormError(formId, "Error storing data: " + ex.getMessage());
                    this.partiallyStoreError = true;
                    LogUtil.error((String) FormService.class.getName(), (Throwable) ex, (String) "Error executing store binder");
                }
                break;
            }
        }
        return formData;
    }

    @Override
    public boolean isActive(Form form, FormData formData) {
        String paramName = FormUtil.getElementParameterName((Element) this);
        Integer pageNum = this.getCurrentPageNumber(formData);
        Integer totalPage = Integer.parseInt((String) this.getProperty("totalPage"));
        Boolean changePage = false;
        if (formData.getRequestParameter(paramName + "_next_page") != null) {
            Integer n = pageNum;
            Integer n2 = pageNum = pageNum + 1;
            if (pageNum <= totalPage) {
                changePage = true;
            }
        } else if (formData.getRequestParameter(paramName + "_prev_page") != null) {
            Integer n = pageNum;
            Integer n3 = pageNum = pageNum - 1;
            if (pageNum > 0) {
                changePage = true;
            }
        } else if (formData.getRequestParameter(paramName + "_change_page") != null && !formData.getRequestParameter(paramName + "_change_page").isEmpty()) {
            changePage = true;
        }
        if (changePage) {
            formData.setStay(true);
            this.setProperty("changePage", (Object) "true");
        }
        return changePage;
    }

    protected Integer getCurrentPageNumber(FormData formData) {
        if(formData != null && this.currentPageNumber == 0) {
            String paramName = FormUtil.getElementParameterName((Element) this);
            String cPageNum = formData.getRequestParameter(paramName + "_c_page_num");
            if (cPageNum == null) {
                cPageNum = "1";
            }
            this.currentPageNumber = Integer.parseInt(cPageNum);
        }
        return this.currentPageNumber;
    }

    protected void hideParentFormButton(Element element) {
        if (element == null) {
            element = FormUtil.findRootForm((Element) this);
        }
        if (element != null && element != this) {
            Collection childs;
            if (element instanceof FormButton) {
                element.setProperty("disabled", (Object) "true");
            }
            if ((childs = element.getChildren()) != null && !childs.isEmpty()) {
                for (Object e : childs) {
                    this.hideParentFormButton((Element)e);
                }
            }
        }
    }

    @Override
    public Collection<String> getDynamicFieldNames() {
        ArrayList<String> fieldNames = new ArrayList<String>();
        Object numberOfPageObject = this.getProperty("numberOfPage");
        if (numberOfPageObject instanceof Map) {
            Map temp = (Map) numberOfPageObject;
            int numberOfPage = Integer.parseInt(temp.get("className").toString());
            Map pagesProperties = (Map) temp.get("properties");
            for (int page = 1; page <= numberOfPage; ++page) {
                String parentSubFormId = (String) pagesProperties.get("page" + page + "_parentSubFormId");
                if (parentSubFormId == null || parentSubFormId.isEmpty()) {
                    continue;
                }
                fieldNames.add(parentSubFormId);
            }
        }
        return fieldNames;
    }
}
